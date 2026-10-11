package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.dao.CustomerRelationshipDAO;
import vn.edu.ictu.qlkh.service.CustomerRelationshipService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/api/v1/customer-relationships/*")
public class CustomerRelationshipServlet extends HttpServlet {
    private final CustomerRelationshipService service=new CustomerRelationshipService();
    private record Actor(long id,String role) {}
    private Actor actor(HttpServletRequest req) {
        HttpSession s=req.getSession(false);
        if(s==null || s.getAttribute("userId")==null || s.getAttribute("userRole")==null)
            throw new SecurityException("Chua dang nhap");
        long id;
        try { id=Long.parseLong(s.getAttribute("userId").toString()); }
        catch(NumberFormatException ex) { throw new SecurityException("Phien khong hop le"); }
        String role=s.getAttribute("userRole").toString().trim().toUpperCase(Locale.ROOT);
        if(id<=0 || !Set.of("SALES","MANAGER","ADMIN").contains(role))
            throw new SecurityException("Khong co quyen");
        return new Actor(id,role);
    }
    private long id(HttpServletRequest req) {
        String p=req.getPathInfo();
        if(p==null || !p.matches("/[1-9][0-9]*")) throw new IllegalArgumentException("ID khong hop le");
        try { return Long.parseLong(p.substring(1)); }
        catch(NumberFormatException ex) { throw new IllegalArgumentException("ID khong hop le"); }
    }
    private String esc(String s) {
        if(s==null) return "null";
        StringBuilder b=new StringBuilder("\"");
        for(char c:s.toCharArray()) {
            switch(c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> { if(c<32) b.append(String.format("\\u%04x",(int)c)); else b.append(c); }
            }
        }
        return b.append('"').toString();
    }
    private void send(HttpServletResponse res,int status,String json) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control","no-store");
        res.getWriter().write(json);
    }
    private void handle(HttpServletResponse res,Exception ex) throws IOException {
        int status=ex instanceof SecurityException?403:
                   ex instanceof IllegalArgumentException?400:500;
        send(res,status,"{\"success\":false,\"message\":"+
                esc(status==500?"Loi he thong hoac co so du lieu":ex.getMessage())+"}");
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
        try {
            Actor a=actor(req);
            String path=req.getPathInfo();
            if(path!=null && !path.equals("/")) throw new IllegalArgumentException("Duong dan khong hop le");
            StringJoiner j=new StringJoiner(",","[","]");
            for(CustomerRelationshipDAO.Link l:service.visible(a.id(),a.role())) {
                j.add("{\"id\":"+l.id()+",\"companyName\":"+esc(l.name())+
                        ",\"parentId\":"+(l.parentId()==null?"null":l.parentId())+"}");
            }
            send(res,200,j.toString());
        } catch(Exception ex) { handle(res,ex); }
    }
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse res) throws IOException {
        try {
            Actor a=actor(req);
            long child=id(req);
            // PUT /api/v1/customer-relationships/{childId}?parentId={parentId}
            // parentId=empty means unlink. No body required.
            String raw=req.getParameter("parentId");
            if(raw==null) throw new IllegalArgumentException("Thieu parentId");
            Long parent=null;
            if(!raw.isBlank()) {
                if(!raw.matches("[1-9][0-9]*")) throw new IllegalArgumentException("parentId khong hop le");
                try { parent=Long.parseLong(raw); }
                catch(NumberFormatException ex) { throw new IllegalArgumentException("parentId khong hop le"); }
            }
            service.update(child,parent,a.id(),a.role());
            send(res,200,"{\"success\":true}");
        } catch(Exception ex) { handle(res,ex); }
    }
}