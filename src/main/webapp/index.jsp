<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*,vn.edu.ictu.qlkh.model.Customer" %>
<%!
    private String statusVi(String value) {
        if ("POTENTIAL".equals(value)) return "Tiềm năng";
        if ("IN_PROGRESS".equals(value)) return "Đang chăm sóc";
        if ("CUSTOMER".equals(value)) return "Khách hàng";
        return value == null ? "Chưa xác định" : value;
    }
    private String chartColor(int index) {
        String[] colors = {"#278df0", "#25b89b", "#f7b74c", "#9476ee", "#ef7481", "#5e9fb2"};
        return colors[index % colors.length];
    }
    private String esc(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    if (request.getAttribute("dashboardCustomerCount") == null && request.getAttribute("dashboardError") == null) {
        response.sendRedirect(request.getContextPath() + "/home"); return;
    }
    List<Customer> customers = (List<Customer>) request.getAttribute("dashboardCustomers");
    if (customers == null) customers = Collections.emptyList();
    Map<String,Integer> byStatus = (Map<String,Integer>) request.getAttribute("dashboardByStatus");
    Map<String,Integer> byIndustry = (Map<String,Integer>) request.getAttribute("dashboardByIndustry");
    Map<String,Integer> byMonth = (Map<String,Integer>) request.getAttribute("dashboardByMonth");
    if (byStatus == null) byStatus = Collections.emptyMap();
    if (byIndustry == null) byIndustry = Collections.emptyMap();
    if (byMonth == null) byMonth = Collections.emptyMap();
    String scope = String.valueOf(request.getAttribute("dashboardScope"));
    String scopeLabel = "ALL".equals(scope) ? "Tất cả dữ liệu" : "TEAM".equals(scope) ? "Nhóm quản lý" : "Cá nhân";
    List<Map<String,String>> auditLogs = (List<Map<String,String>>) request.getAttribute("dashboardAuditLogs");
%>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Tổng quan - Hệ thống QLKH</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/session.css">
<style>
*{box-sizing:border-box}html,body{min-height:100%;margin:0}body{display:flex;min-height:100vh;background:#f3f6fb;font-family:Segoe UI,Arial,sans-serif;color:#172b49}main{flex:1;min-width:0;width:100%;max-width:none;padding:26px clamp(16px,2vw,40px)}.eyebrow{color:#1877c9;font-size:11px;font-weight:700;letter-spacing:1.4px;text-transform:uppercase}h1{font-size:28px;margin:8px 0 4px}h2{font-size:17px;margin:0 0 17px}.sub{color:#64748b;font-size:14px}.header{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:22px}.scope{padding:8px 13px;background:#e9f4ff;color:#1468b4;border-radius:7px;font-weight:700;font-size:12px}.stats{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:15px}.stat,.panel{background:white;border:1px solid #e1e8f1;border-radius:12px;padding:20px;box-shadow:0 3px 15px #19395a08}.stat{border-top:4px solid #288cf0}.stat:nth-child(2){border-top-color:#1cbb84}.stat:nth-child(3){border-top-color:#f6ac37}.stat:nth-child(4){border-top-color:#9274eb}.stat label{display:block;color:#526681;font-size:13px;font-weight:600}.stat strong{display:block;font-size:30px;margin-top:11px}.stat small{display:block;color:#7c8ca1;margin-top:6px}.layout{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;margin-top:18px}.muted{color:#7c8ca1;font-size:13px}.empty{padding:30px 14px;background:#f9fbfe;border:1px dashed #d8e2ee;border-radius:8px;text-align:center;color:#7c8ca1}.bars{display:grid;gap:14px}.bar-head{display:flex;justify-content:space-between;gap:10px;margin-bottom:6px;font-size:13px}.track{height:11px;border-radius:9px;background:#edf3fb;overflow:hidden}.fill{height:100%;background:linear-gradient(90deg,#2376cc,#2bb4c6);border-radius:9px}.tablewrap{overflow-x:auto}table{width:100%;border-collapse:collapse;font-size:13px}th{text-align:left;color:#50637b;background:#f5f8fc}th,td{padding:11px;border-bottom:1px solid #edf1f5}a{color:#1975d2;text-decoration:none}.error{padding:13px;background:#fff2f2;color:#b42318;border-radius:8px;margin:12px 0}.foot{margin-top:14px;font-size:13px}@media(max-width:1100px){.stats{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:750px){main{padding:18px}.layout,.stats{grid-template-columns:1fr}.header{align-items:flex-start;flex-direction:column}}
 .donut-layout{display:flex;align-items:center;gap:30px;min-height:210px}.donut{width:188px;height:188px;flex:none;border-radius:50%;display:grid;place-items:center}.donut-center{width:125px;height:125px;border-radius:50%;background:#fff;display:flex;flex-direction:column;align-items:center;justify-content:center;box-shadow:0 0 0 1px #edf1f6}.donut-center strong{font-size:29px}.donut-center small{font-size:12px;color:#718198}.legend{flex:1;display:grid;gap:13px}.legend-item{display:flex;align-items:center;justify-content:space-between;gap:15px;font-size:13px}.legend-name{display:flex;align-items:center;gap:8px}.legend-dot{width:11px;height:11px;border-radius:3px;display:inline-block;flex:none}.column-chart{display:flex;align-items:flex-end;gap:14px;min-height:215px;padding:18px 8px 0;border-bottom:1px solid #dce6f1}.column-item{flex:1;min-width:0;text-align:center;display:flex;flex-direction:column;justify-content:flex-end;align-items:center;gap:8px;height:205px}.column-value{font-weight:700;font-size:12px}.column-bar{width:min(100%,55px);min-height:4px;border-radius:6px 6px 0 0;background:linear-gradient(180deg,#36a0ef,#2388d5)}.column-label{font-size:11px;color:#60718b;overflow-wrap:anywhere;padding-bottom:6px}@media(max-width:950px){.donut-layout{flex-direction:column;align-items:stretch}.donut{align-self:center}.column-chart{gap:6px}}
</style></head><body>
<jsp:include page="/components/sidebar.jsp" />
<main>
<div class="header"><div><div class="eyebrow">Hệ thống quản lý khách hàng</div><h1>Tổng quan</h1><div class="sub">Thống kê dữ liệu thực theo quyền truy cập hiện tại</div></div><span class="scope"><%=esc(scopeLabel)%></span></div>
<% if(request.getAttribute("dashboardError")!=null){ %><div class="error"><%=esc(request.getAttribute("dashboardError"))%></div><% } %>
<div class="stats">
<div class="stat"><label>Tổng khách hàng</label><strong><%=esc(request.getAttribute("dashboardCustomerCount") == null ? "—" : request.getAttribute("dashboardCustomerCount"))%></strong><small>Trong phạm vi được phép xem</small></div>
<div class="stat"><label>Tổng người liên hệ</label><strong><%=esc(request.getAttribute("dashboardContactCount") == null ? "—" : request.getAttribute("dashboardContactCount"))%></strong><small>Thuộc khách hàng được phép xem</small></div>
<div class="stat"><label>Khách hàng tiềm năng</label><strong><%=esc(byStatus.getOrDefault("POTENTIAL",0))%></strong><small>Dữ liệu từ trạng thái khách hàng</small></div>
<div class="stat"><label>Khách hàng đang chăm sóc</label><strong><%=esc(byStatus.getOrDefault("IN_PROGRESS",0))%></strong><small>Dữ liệu từ trạng thái khách hàng</small></div>
</div>
<% if(request.getAttribute("dashboardContactError")!=null){ %><p class="muted"><%=esc(request.getAttribute("dashboardContactError"))%></p><% } %>
<div class="layout">
<section class="panel"><h2>Khách hàng theo trạng thái</h2>
<% if(byStatus.isEmpty()){ %><div class="empty">Chưa có dữ liệu khách hàng trong phạm vi truy cập.</div><% } else {
    StringBuilder slices = new StringBuilder(); double pos = 0; int si=0;
    for(Map.Entry<String,Integer> e:byStatus.entrySet()) {
        double next = pos + (customers.isEmpty()?0:e.getValue()*100.0/customers.size());
        if(si>0) slices.append(", ");
        slices.append(chartColor(si)).append(" ").append(String.format(java.util.Locale.US,"%.3f",pos)).append("% ").append(String.format(java.util.Locale.US,"%.3f",next)).append("%");
        pos=next;si++;
    }
%><div class="donut-layout"><div class="donut" style="background:conic-gradient(<%=slices.toString()%>)"><div class="donut-center"><strong><%=customers.size()%></strong><small>Khách hàng</small></div></div><div class="legend">
<% int li=0;for(Map.Entry<String,Integer> e:byStatus.entrySet()){int pct=customers.isEmpty()?0:(int)Math.round(e.getValue()*100.0/customers.size()); %>
<div class="legend-item"><span class="legend-name"><span class="legend-dot" style="background:<%=chartColor(li++)%>"></span><%=esc(statusVi(e.getKey()))%></span><strong><%=e.getValue()%> (<%=pct%>%)</strong></div>
<% } %></div></div><% } %></section>
<section class="panel"><h2>Khách hàng theo ngành nghề</h2>
<% if(byIndustry.isEmpty()){ %><div class="empty">Chưa có dữ liệu ngành nghề.</div><% } else { for(Map.Entry<String,Integer> e:byIndustry.entrySet()){ int pct=customers.isEmpty()?0:(int)Math.round(e.getValue()*100.0/customers.size()); %>
<div class="bars"><div><div class="bar-head"><span><%=esc(e.getKey())%></span><b><%=e.getValue()%> (<%=pct%>%)</b></div><div class="track"><div class="fill" style="width:<%=pct%>%"></div></div></div></div>
<% }} %></section>
<section class="panel"><h2>Khách hàng mới nhất</h2><div class="tablewrap"><table><thead><tr><th>ID</th><th>Doanh nghiệp</th><th>Trạng thái</th><th>Ngày tạo</th></tr></thead><tbody>
<% int count=0;for(Customer c:customers){if(count++>=8)break; %><tr><td><%=esc(c.getId())%></td><td><%=esc(c.getCompanyName())%></td><td><%=esc(statusVi(c.getStatus()))%></td><td><%=esc(c.getCreatedAt()==null?"":c.getCreatedAt().toLocalDate())%></td></tr><% } %>
<% if(customers.isEmpty()){ %><tr><td colspan="4" class="muted">Chưa có khách hàng trong phạm vi truy cập.</td></tr><% } %>
</tbody></table></div><p class="foot"><a href="${pageContext.request.contextPath}/views/sales/customers.jsp">Xem danh sách khách hàng</a></p></section>
<section class="panel"><h2>Khách hàng mới theo tháng</h2>
<% if(byMonth.isEmpty()){ %><div class="empty">Chưa có dữ liệu ngày tạo khách hàng.</div><% } else { int max=Math.max(1,Collections.max(byMonth.values())); %>
<div class="column-chart" role="img" aria-label="Biểu đồ số khách hàng mới theo tháng">
<% for(Map.Entry<String,Integer> e:byMonth.entrySet()){int pct=(int)Math.round(e.getValue()*100.0/max); %>
<div class="column-item"><span class="column-value"><%=e.getValue()%></span><div class="column-bar" style="height:<%=Math.max(2,pct*1.5)%>px" title="<%=esc(e.getKey())%>: <%=e.getValue()%> khách hàng"></div><span class="column-label"><%=esc(e.getKey())%></span></div>
<% } %></div><% } %></section>
<% if("ALL".equals(scope)){ %>
<section class="panel"><h2>Nhật ký hệ thống gần đây (chỉ quản trị viên)</h2>
<% if(auditLogs==null){ %><div class="empty"><%=esc(request.getAttribute("dashboardAuditError"))%></div><% } else if(auditLogs.isEmpty()){ %><div class="empty">Chưa có hoạt động được ghi nhận.</div><% } else { %>
<div class="tablewrap"><table><thead><tr><th>Thời gian</th><th>Tài khoản</th><th>Hành động</th><th>Đối tượng</th></tr></thead><tbody>
<% for(Map<String,String> log:auditLogs){ %><tr><td><%=esc(log.get("time"))%></td><td><%=esc(log.get("username"))%></td><td><%=esc(log.get("action"))%></td><td><%=esc(log.get("target"))%></td></tr><% } %>
</tbody></table></div><% } %></section>
<% } %>
<section class="panel"><h2>Dữ liệu chưa tích hợp</h2><div class="empty">Cơ hội kinh doanh, báo giá và doanh số chưa có bảng dữ liệu tương ứng trong cơ sở dữ liệu hiện tại. Không hiển thị số liệu giả.</div></section>
</div></main>
<% if(session.getAttribute("userRole")!=null){ %><jsp:include page="/components/session-modal.jsp" /><script src="${pageContext.request.contextPath}/assets/js/session.js"></script><% } %>
</body></html>
