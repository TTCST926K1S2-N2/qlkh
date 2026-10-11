package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.dao.ContractDAO;
import vn.edu.ictu.qlkh.service.CustomerService;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/** GET /api/v1/contracts?customerId=N; POST create; PUT /N update. */
@WebServlet("/api/v1/contracts/*")
public class ContractServlet extends HttpServlet {
    private final ContractDAO dao=new ContractDAO();
    private final CustomerService customers=new CustomerService();
    private record Actor(long id,String role) {}
    private Actor actor(HttpServletRequest req) {
        HttpSession session=req.getSession(false);
        if(session==null)return null;
        Object id=session.getAttribute("userId"),role=session.getAttribute("userRole");
        if(!(id instanceof Number) || role==null)return null;
        return new Actor(((Number)id).longValue(),role.toString().trim().toUpperCase(Locale.ROOT));
    }
    private void access(long customerId,Actor a) throws SQLException {
        if(customers.detail(customerId,a.id(),a.role())==null)throw new NoSuchElementException("Khach hang khong ton tai hoac khong co quyen");
    }
    private static String esc(String value) {
        if(value==null)return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }
    private String json(ContractDAO.Contract c) {
        return "{\"id\":"+c.id()+",\"customerId\":"+c.customerId()+
                ",\"contractCode\":\""+esc(c.code())+"\",\"contractName\":\""+esc(c.name())+
                "\",\"contractValue\":"+c.value()+",\"status\":\""+esc(c.status())+
                "\",\"signedDate\":"+(c.signedDate()==null?"null":"\""+c.signedDate()+"\"")+"}";
    }
    private Map<String,String> parameters(HttpServletRequest req, boolean update) throws IOException {
        Map<String,String> result = new HashMap<>();
        if (!update) {
            for (Map.Entry<String,String[]> e : req.getParameterMap().entrySet()) {
                if (e.getValue().length > 0) result.put(e.getKey(), e.getValue()[0]);
            }
            return result;
        }

        String contentType = req.getContentType();
        if (contentType == null ||
            !contentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException("Content-Type khong hop le");
        }

        String body = new String(req.getInputStream().readNBytes(65537),
                java.nio.charset.StandardCharsets.UTF_8);
        if (body.length() > 65536) {
            throw new IllegalArgumentException("Body qua lon");
        }

        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = java.net.URLDecoder.decode(kv[0],
                    java.nio.charset.StandardCharsets.UTF_8);
            String value = java.net.URLDecoder.decode(
                    kv.length > 1 ? kv[1] : "",
                    java.nio.charset.StandardCharsets.UTF_8);
            result.putIfAbsent(key, value);
        }
        return result;
    }

    private void run(HttpServletRequest req,HttpServletResponse res,boolean write,boolean update) throws IOException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");res.setContentType("application/json;charset=UTF-8");res.setHeader("Cache-Control","no-store");
        Actor a=actor(req);if(a==null){res.sendError(401);return;}
        try {
            if(!write){
                long id=Long.parseLong(req.getParameter("customerId"));access(id,a);
                List<ContractDAO.Contract> list=dao.list(id);StringJoiner sj=new StringJoiner(",","[","]");
                for(var c:list)sj.add(json(c));res.getWriter().write(sj.toString());return;
            }
            if(!Set.of("ADMIN","MANAGER").contains(a.role())){res.sendError(403);return;}
            long id=0;
            if(update){String path=req.getPathInfo();if(path==null||!path.matches("/[1-9][0-9]*")){res.sendError(400);return;}
                id=Long.parseLong(path.substring(1));}
            Map<String,String> params=parameters(req,update);
            long customerId=Long.parseLong(params.get("customerId"));access(customerId,a);
            if(update){var old=dao.find(id);if(old==null){res.sendError(404);return;}access(old.customerId(),a);}

            String code=params.get("contractCode"),name=params.get("contractName"),status=params.get("status");
            if(code==null||code.isBlank()||code.length()>80||name==null||name.isBlank()||name.length()>255||
                !Set.of("DRAFT","PENDING","SIGNED","CANCELLED").contains(status))throw new IllegalArgumentException();
            BigDecimal value=new BigDecimal(params.get("contractValue"));
            if(value.signum()<0||value.scale()>2||value.precision()>18)throw new IllegalArgumentException();
            String date=params.get("signedDate");LocalDate signed=(date==null||date.isBlank())?null:LocalDate.parse(date);
            if("SIGNED".equals(status)&&signed==null)throw new IllegalArgumentException();
            var contract=new ContractDAO.Contract(id,code.trim(),customerId,name.trim(),value,status,signed);
            if(update){if(!dao.update(contract)){res.sendError(404);return;}res.getWriter().write(json(contract));}
            else {long newId=dao.create(contract);res.setStatus(201);res.getWriter().write(json(new ContractDAO.Contract(newId,code.trim(),customerId,name.trim(),value,status,signed)));}
        }catch(IllegalArgumentException ex){res.sendError(400,"Du lieu hop dong khong hop le");}
        catch(NoSuchElementException ex){res.sendError(404);}
        catch(SecurityException ex){res.sendError(403);}
        catch(SQLException ex){getServletContext().log("Contract API failed",ex);res.sendError(500);}
    }
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException{run(req,res,false,false);}
    @Override protected void doPut(HttpServletRequest req,HttpServletResponse res)throws IOException{run(req,res,true,true);}
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException{
        String path=req.getPathInfo();
        if(path!=null && !path.isBlank() && !"/".equals(path)) {res.sendError(405);return;}
        run(req,res,true,false);
    }

}
