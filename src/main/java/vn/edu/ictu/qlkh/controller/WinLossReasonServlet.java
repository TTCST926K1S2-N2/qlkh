package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.model.WinLossReason;
import vn.edu.ictu.qlkh.service.WinLossReasonService;
import java.io.IOException;

@WebServlet("/win-loss-reasons/*")
public class WinLossReasonServlet extends HttpServlet {
    private WinLossReasonService service;
    @Override public void init(){service=new WinLossReasonService();}
    private boolean admin(HttpServletRequest r){HttpSession s=r.getSession(false);return s!=null&&"ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("userRole")));}
    private Long id(HttpServletRequest r){try{String p=r.getPathInfo();return p==null||p.length()<2?null:Long.parseLong(p.substring(1));}catch(Exception e){return null;}}
    private void out(HttpServletResponse r,int status,String body)throws IOException{r.setStatus(status);r.setContentType("application/json;charset=UTF-8");r.getWriter().write(body);}
    private String esc(String v){if(v==null)return "";return v.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n");}
    private String json(WinLossReason x){return "{\"id\":"+x.getId()+",\"name\":\""+esc(x.getName())+"\",\"code\":\""+esc(x.getCode())+"\",\"type\":\""+esc(x.getType())+"\",\"status\":\""+esc(x.getStatus())+"\",\"description\":"+ (x.getDescription()==null?"null":"\""+esc(x.getDescription())+"\"") +"}";}
    @Override protected void doGet(HttpServletRequest r,HttpServletResponse s)throws IOException{try{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}Long id=id(r);if(id==null){StringBuilder b=new StringBuilder("[");boolean first=true;for(WinLossReason x:service.getAllReasons()){if(!first)b.append(",");b.append(json(x));first=false;}b.append("]");out(s,200,b.toString());}else{WinLossReason x=service.getReasonById(id);out(s,x==null?404:200,x==null?"{\"error\":\"Not found\"}":json(x));}}catch(Exception e){out(s,500,"{\"error\":\""+esc(e.getMessage())+"\"}");}}
    @Override protected void doPost(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}try{WinLossReason x=new WinLossReason();x.setName(r.getParameter("name"));x.setCode(r.getParameter("code"));x.setType(r.getParameter("type"));x.setStatus(r.getParameter("status"));x.setDescription(r.getParameter("description"));service.addReason(x);out(s,201,"{\"id\":"+x.getId()+"}");}catch(IllegalArgumentException e){out(s,400,"{\"error\":\""+e.getMessage()+"\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
    @Override protected void doPut(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}Long id=id(r);if(id==null){out(s,400,"{\"error\":\"Invalid id\"}");return;}try{WinLossReason x=new WinLossReason();x.setId(id);x.setName(r.getParameter("name"));x.setCode(r.getParameter("code"));x.setType(r.getParameter("type"));x.setStatus(r.getParameter("status"));x.setDescription(r.getParameter("description"));service.updateReason(x);out(s,200,"{\"message\":\"updated\"}");}catch(IllegalArgumentException e){out(s,400,"{\"error\":\""+e.getMessage()+"\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
    @Override protected void doDelete(HttpServletRequest r,HttpServletResponse s)throws IOException{if(!admin(r)){out(s,403,"{\"error\":\"Forbidden\"}");return;}Long id=id(r);if(id==null){out(s,400,"{\"error\":\"Invalid id\"}");return;}try{service.deleteReason(id);out(s,200,"{\"message\":\"deleted\"}");}catch(Exception e){out(s,500,"{\"error\":\""+e.getMessage()+"\"}");}}
}
