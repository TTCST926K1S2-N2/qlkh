package vn.edu.ictu.qlkh.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import vn.edu.ictu.qlkh.dao.ContractSummaryDAO;
import vn.edu.ictu.qlkh.service.CustomerService;
import java.io.IOException;
import java.util.Locale;

@WebServlet("/api/v1/contracts/summary/*")
public class ContractSummaryServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control","no-store");
        HttpSession session=req.getSession(false);
        if(session==null || session.getAttribute("userId")==null || session.getAttribute("userRole")==null) {
            res.sendError(401);return;
        }
        try {
            String path=req.getPathInfo();
            boolean group = path != null && path.matches("/group/[1-9][0-9]*");
            if(path==null || (!group && !path.matches("/[1-9][0-9]*"))) {res.sendError(400);return;}
            long customerId=Long.parseLong(path.substring(group ? 7 : 1));
            long userId=Long.parseLong(session.getAttribute("userId").toString());
            String role=session.getAttribute("userRole").toString().trim().toUpperCase(Locale.ROOT);
            if(new CustomerService().detail(customerId,userId,role)==null) {res.sendError(404);return;}
            ContractSummaryDAO dao=new ContractSummaryDAO();
            if (group && !"ADMIN".equals(role)) {res.sendError(403);return;}
            var v=group ? dao.forGroup(customerId) : dao.forCustomer(customerId);
            res.getWriter().write("{\"signedValue\":"+v.signed()+",\"unsignedValue\":"+v.unsigned()+
                ",\"signedCount\":"+v.signedCount()+",\"unsignedCount\":"+v.unsignedCount()+
                "}");
        } catch(NumberFormatException ex) {res.sendError(400);}
          catch(SecurityException ex) {res.sendError(403);}
          catch(Exception ex) {getServletContext().log("Contract summary failed",ex);res.sendError(500);}
    }
}
