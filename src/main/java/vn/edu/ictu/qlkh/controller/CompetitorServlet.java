package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.model.Competitor;
import vn.edu.ictu.qlkh.service.CompetitorService;
import java.io.IOException;

@WebServlet("/competitors/*")
public class CompetitorServlet extends HttpServlet {
    private CompetitorService service;
    @Override public void init(){service=new CompetitorService();}
    private boolean admin(HttpServletRequest r){HttpSession s=r.getSession(false);return s!=null&&"ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("userRole")));}
    private Long id(HttpServletRequest r){try{String p=r.getPathInfo();return p==null||p.length()<2?null:Long.parseLong(p.substring(1));}catch(Exception e){return null;}}
    private void out(HttpServletResponse r,int status,String body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");r.getWriter().write(body);}
    private String esc(String v){if(v==null)return "";return v.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n");}
    private String json(Competitor x){return "{\"id\":"+x.getId()+",\"name\":\""+esc(x.getName())+"\",\"code\":\""+esc(x.getCode())+"\",\"status\":\""+esc(x.getStatus())+"\",\"description\":"+ (x.getDescription()==null?"null":"\""+esc(x.getDescription())+"\"") +"}";}
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}try{Long id=id(r);if(id==null){StringBuilder b=new StringBuilder("[");boolean first=true;for(Competitor x:service.getAllCompetitors()){if(!first)b.append(",");b.append(json(x));first=false;}b.append("]");out(s,200,b.toString());}else{Competitor x=service.getCompetitorById(id);out(s,x==null?404:200,x==null?"{\"error\":\"Not found\"}":json(x));}}catch(Exception e){out(s,500,"{\"error\":\""+esc(e.getMessage())+"\"}");}}
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}try{Competitor x=new Competitor();x.setName(r.getParameter("name"));x.setCode(r.getParameter("code"));x.setStatus(r.getParameter("status"));x.setDescription(r.getParameter("description"));service.addCompetitor(x);out(s,201,"{\"id\":"+x.getId()+"}");}catch(IllegalArgumentException e){out(s,400,"{\"error\":\""+e.getMessage()+"\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
    @Override protected void doPut(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}Long id=id(r);if(id==null){out(s,400,"{\"error\":\"Invalid id\"}");return;}try{Competitor x=new Competitor();x.setId(id);x.setName(r.getParameter("name"));x.setCode(r.getParameter("code"));x.setStatus(r.getParameter("status"));x.setDescription(r.getParameter("description"));service.updateCompetitor(x);out(s,200,"{\"message\":\"updated\"}");}catch(IllegalArgumentException e){out(s,400,"{\"error\":\""+e.getMessage()+"\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
    @Override protected void doDelete(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}Long id=id(r);if(id==null){out(s,400,"{\"error\":\"Invalid id\"}");return;}try{service.deleteCompetitor(id);out(s,200,"{\"message\":\"deleted\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
}
