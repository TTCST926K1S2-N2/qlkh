document.addEventListener("DOMContentLoaded", () => {
  "use strict";
  const contextPath = window.APP_CONTEXT_PATH || "";
  const $ = (id) => document.getElementById(id);
  const loading = $("loading"), error = $("error"), content = $("customer-content");
  const labels = {POTENTIAL:"Tiềm năng", IN_PROGRESS:"Đang giao dịch", CUSTOMER:"Khách hàng", INACTIVE:"Ngừng hợp tác"};
  const value = (v) => v === null || v === undefined || v === "" ? "—" : String(v);
  function showError(message) {loading.classList.add("hidden");content.classList.add("hidden");error.classList.remove("hidden");$("error-message").textContent=message;}
  function showContent() {loading.classList.add("hidden");error.classList.add("hidden");content.classList.remove("hidden");}
  function setText(id,text){$(id).textContent=value(text);}
  function empty(icon,title,description){const el=document.createElement("div");el.className="empty-state";const i=document.createElement("span");i.className="empty-icon";i.textContent=icon;const h=document.createElement("strong");h.textContent=title;const p=document.createElement("span");p.textContent=description;el.append(i,h,p);return el;}
  function formatDate(input){if(!input)return "Chưa có thời gian";const d=new Date(input);return Number.isNaN(d.getTime())?String(input):new Intl.DateTimeFormat("vi-VN",{day:"2-digit",month:"2-digit",year:"numeric",hour:"2-digit",minute:"2-digit"}).format(d);}
  function setWebsite(website){const el=$("website");el.replaceChildren();if(!website){el.textContent="—";return;}try{const url=new URL(website);if(!["http:","https:"].includes(url.protocol))throw Error();const a=document.createElement("a");a.href=url.href;a.target="_blank";a.rel="noopener noreferrer";a.textContent=website;el.append(a);}catch{el.textContent=website;}}
  function renderCustomer(c){setText("company-name",c.companyName);setText("tax-code",c.taxCode);setText("industry",c.industry);setText("company-size",c.companySize);setText("address",c.address);setText("company-location",c.address);setWebsite(c.website);const s=$("company-status");s.textContent=labels[c.status]||value(c.status);s.dataset.status=c.status||"";}
  function updateStat(key,items,available){const count=$("stat-"+key),note=$("stat-"+key+"-note");count.textContent=available?String(items.length):"—";note.textContent=available?(items.length?"Bản ghi hiện có":"Chưa có bản ghi"):"Chưa tích hợp nguồn dữ liệu";}
  function renderContacts(data){const el=$("contacts-section");el.replaceChildren();const available=data.availability?.contacts===true,items=Array.isArray(data.contacts)?data.contacts:[];updateStat("contacts",items,available);if(!available){el.append(empty("♙","Chưa tích hợp","Nguồn dữ liệu người liên hệ chưa sẵn sàng."));return;}if(!items.length){el.append(empty("♙","Chưa có người liên hệ","Chưa ghi nhận người liên hệ cho khách hàng này."));return;}const list=document.createElement("div");list.className="customer360-contact-list";items.forEach(c=>{const item=document.createElement("article");item.className="customer360-contact-item";const avatar=document.createElement("div");avatar.className="contact-avatar";avatar.textContent=(c.fullName||"?").trim().slice(0,1).toUpperCase();const main=document.createElement("div");main.className="contact-main";const nameLine=document.createElement("div");nameLine.className="contact-name-line";const name=document.createElement("h3");name.textContent=c.fullName||"Chưa có tên";nameLine.append(name);if(c.isPrimary){const badge=document.createElement("span");badge.className="primary-tag";badge.textContent="Liên hệ chính";nameLine.append(badge);}const job=document.createElement("p");job.className="contact-job";job.textContent=c.jobTitle||"Chưa có chức vụ";const details=document.createElement("div");details.className="contact-details";for(const [icon,v] of [["✉",c.email],["☎",c.phone]]){if(v){const span=document.createElement("span");span.textContent=icon+" "+v;details.append(span);}}main.append(nameLine,job,details);item.append(avatar,main);list.append(item);});el.append(list);}
  function renderTimeline(data){const el=$("activities-section");el.replaceChildren();const available=data.availability?.activities===true,items=Array.isArray(data.activities)?data.activities:[];updateStat("activities",items,available);if(!available){el.append(empty("◷","Chưa tích hợp","Nguồn dữ liệu lịch sử hoạt động chưa sẵn sàng."));return;}if(!items.length){el.append(empty("◷","Chưa có hoạt động","Chưa ghi nhận sự kiện cho khách hàng này."));return;}const list=document.createElement("div");list.className="timeline-list";items.forEach(a=>{const item=document.createElement("article");item.className="timeline-event";const type=document.createElement("div");type.className="timeline-type";type.textContent=a.type==="CONTACT_TRANSFER"?"CHUYỂN NGƯỜI LIÊN HỆ":(a.type||"HOẠT ĐỘNG").replaceAll("_"," ");const title=document.createElement("h3");title.textContent=a.title||"Hoạt động khách hàng";const time=document.createElement("time");time.textContent=formatDate(a.occurredAt);item.append(type,title,time);list.append(item);});el.append(list);}
  function renderFutureSection(data,key,sectionId,icon,label){const el=$(sectionId);el.replaceChildren();const available=data.availability?.[key]===true,items=Array.isArray(data[key])?data[key]:[];updateStat(key,items,available);if(!available){el.append(empty(icon,"Chưa tích hợp nguồn dữ liệu",`Phần ${label} chưa có dữ liệu từ Backend.`));return;}if(!items.length){el.append(empty(icon,"Chưa có bản ghi",`Khách hàng chưa có ${label}.`));return;}const list=document.createElement("div");list.className="customer360-contact-list";items.forEach(item=>{const row=document.createElement("div");row.className="customer360-contact-item";const main=document.createElement("div");main.className="contact-main";const title=document.createElement("h3");title.textContent=item.name||item.fileName||item.title||"Bản ghi";main.append(title);const meta=document.createElement("p");meta.className="contact-job";meta.textContent=[item.stage,item.expectedValue!=null?String(item.expectedValue):null,item.updatedAt||item.uploadedAt].filter(Boolean).join(" · ");main.append(meta);if(key==="attachments"&&item.downloadUrl){try{const u=new URL(item.downloadUrl,location.origin);if(u.origin===location.origin&&u.pathname.startsWith(contextPath+"/")){const a=document.createElement("a");a.href=u.href;a.textContent="Xem tệp";main.append(a);}}catch{}}row.append(main);list.append(row);});el.append(list);}
  async function loadCustomer(){const id=new URLSearchParams(location.search).get("id");if(!id||!/^[1-9]\d*$/.test(id)){showError("ID khách hàng không hợp lệ.");return;}try{const res=await fetch(`${contextPath}/api/v1/customers/360/${encodeURIComponent(id)}`,{headers:{Accept:"application/json"},credentials:"same-origin"});if(res.redirected&&new URL(res.url).pathname===contextPath+"/login"){location.replace(contextPath+"/login");return;}if(res.status===401){location.replace(contextPath+"/login");return;}const data=await res.json();if(!res.ok)throw Error(data.message||"Không thể tải thông tin khách hàng.");if(!data.customer)throw Error("API không trả về thông tin khách hàng.");renderCustomer(data.customer);renderContacts(data);renderTimeline(data);renderFutureSection(data,"opportunities","opportunities-section","◎","cơ hội kinh doanh");renderFutureSection(data,"attachments","attachments-section","▤","tệp đính kèm");showContent();}catch(e){console.error("Customer 360:",e);showError(e.message||"Có lỗi khi tải dữ liệu.");}}

  async function loadS308Support(customerId) {
    const list = $("s308-support-list");
    const risk = $("s308-risk");
    const link = $("s308-support-link");

    link.href = `${contextPath}/views/sales/customer-care.jsp?customerId=${encodeURIComponent(customerId)}`;

    const statusNames = {
      OPEN: "Mới",
      IN_PROGRESS: "Đang xử lý",
      RESOLVED: "Đã giải quyết",
      CLOSED: "Đã đóng"
    };

    const priorityNames = {
      LOW: "Thấp",
      MEDIUM: "Trung bình",
      HIGH: "Cao",
      URGENT: "Khẩn cấp"
    };

    try {
      const [listResponse, riskResponse] = await Promise.all([
        fetch(`${contextPath}/api/v1/customer-support?customerId=${encodeURIComponent(customerId)}`, {
          credentials: "same-origin",
          headers: { Accept: "application/json" }
        }),
        fetch(`${contextPath}/api/v1/customer-support/risk?customerId=${encodeURIComponent(customerId)}`, {
          credentials: "same-origin",
          headers: { Accept: "application/json" }
        })
      ]);

      if (!listResponse.ok || !riskResponse.ok) {
        throw new Error("Không thể tải thông tin chăm sóc khách hàng.");
      }

      const requests = await listResponse.json();
      const riskData = await riskResponse.json();

      if (!Array.isArray(requests)) {
        throw new Error("Dữ liệu yêu cầu hỗ trợ không hợp lệ.");
      }

      risk.replaceChildren();

      const riskText = document.createElement("p");
      riskText.style.fontWeight = "600";
      riskText.style.color = riskData.atRisk ? "#b91c1c" : "#166534";

      riskText.textContent = riskData.atRisk
        ? `Cảnh báo nguy cơ mất khách hàng: ${riskData.unresolvedCount} yêu cầu chưa giải quyết.`
        : `Số yêu cầu chưa giải quyết: ${riskData.unresolvedCount || 0}.`;

      risk.append(riskText);

      list.replaceChildren();

      if (requests.length === 0) {
        list.textContent = "Khách hàng chưa có yêu cầu hỗ trợ.";
        return;
      }

      const table = document.createElement("table");
      table.style.width = "100%";
      table.style.borderCollapse = "collapse";

      const thead = document.createElement("thead");
      const headerRow = document.createElement("tr");

      ["Mã", "Tiêu đề", "Ưu tiên", "Trạng thái", "Người phụ trách", "Ngày tạo"].forEach(label => {
        const th = document.createElement("th");
        th.textContent = label;
        th.style.padding = "10px";
        th.style.textAlign = "left";
        th.style.borderBottom = "1px solid #dbe3ee";
        headerRow.append(th);
      });

      thead.append(headerRow);
      table.append(thead);

      const tbody = document.createElement("tbody");

      requests.forEach(item => {
        const row = document.createElement("tr");

        const values = [
          item.id,
          item.title || "",
          priorityNames[item.priority] || item.priority || "",
          statusNames[item.status] || item.status || "",
          item.assignedTo || "Chưa phân công",
          item.createdAt ? formatDate(item.createdAt) : ""
        ];

        values.forEach(value => {
          const td = document.createElement("td");
          td.textContent = String(value);
          td.style.padding = "10px";
          td.style.borderBottom = "1px solid #e5e7eb";
          row.append(td);
        });

        tbody.append(row);
      });

      table.append(tbody);
      list.append(table);

    } catch (error) {
      console.error("S3-08 Customer 360:", error);
      list.textContent = "Không thể tải lịch sử chăm sóc khách hàng.";
      risk.textContent = "";
    }
  }
  const s308CustomerId = new URLSearchParams(location.search).get("id");
  if (s308CustomerId && /^[1-9]\d*$/.test(s308CustomerId)) {
    loadS308Support(s308CustomerId);
  }
  loadCustomer();
});

// S3-03 contract totals, loaded independently from the existing 360 endpoint.
document.addEventListener('DOMContentLoaded', async () => {
  const box=document.getElementById('contract-summary');
  const id=new URLSearchParams(location.search).get('id');
  if(!box || !id || !/^[1-9]\d*$/.test(id)) return;
  try {
    const response=await fetch((window.APP_CONTEXT_PATH||'')+'/api/v1/contracts/summary/'+encodeURIComponent(id),{credentials:'same-origin'});
    if(!response.ok) throw new Error('Không thể tải số liệu hợp đồng');
    const data=await response.json();
    const money=v=>Number(v).toLocaleString('vi-VN')+' VNĐ';
    box.textContent='Đã ký: '+data.signedCount+' hợp đồng, '+money(data.signedValue)+
      ' | Chưa ký: '+data.unsignedCount+' hợp đồng, '+money(data.unsignedValue);
  } catch(e) {box.textContent='Chưa thể tải dữ liệu hợp đồng. Kiểm tra migration database.';}
});

// Contracts are stored separately from customer-care tracking.
document.addEventListener('DOMContentLoaded', async () => {
  const id = new URLSearchParams(location.search).get('id');
  const list = document.getElementById('contract-list');
  const form = document.getElementById('contract-form');
  const message = document.getElementById('contract-message');
  if (!list || !form || !id || !/^[1-9]\d*$/.test(id)) return;
  const api = (window.APP_CONTEXT_PATH || '') + '/api/v1/contracts';
  const money = value => Number(value).toLocaleString('vi-VN') + ' VNĐ';
  const canWrite = window.CONTRACT_CAN_WRITE === true;
  form.style.display = canWrite ? 'flex' : 'none';
  let contracts = [];
  async function refresh() {
    const res = await fetch(api + '?customerId=' + encodeURIComponent(id), {credentials:'same-origin'});
    if (!res.ok) throw Error('Không thể tải danh sách hợp đồng');
    contracts = await res.json();
    list.replaceChildren();
    if (!contracts.length) {list.textContent='Chưa có hợp đồng.';return;}
    for (const c of contracts) {
      const row = document.createElement('div');
      row.style.cssText='padding:10px 0;border-bottom:1px solid #ddd';
      const title = document.createElement('span');
      title.textContent = c.contractCode + ' - ' + c.contractName + ' - ' + money(c.contractValue) + ' - ' + c.status;
      row.append(title);
      const edit = document.createElement('button');
      edit.type='button';edit.textContent='Sửa';edit.style.marginLeft='12px';
      edit.addEventListener('click',()=>{
        form.elements.contractId.value=c.id;
        for (const key of ['contractCode','contractName','contractValue','status','signedDate'])
          form.elements[key].value=c[key] ?? '';
        form.scrollIntoView({behavior:'smooth',block:'nearest'});
      });
      if (canWrite) row.append(edit);
      list.append(row);
    }
  }
  document.getElementById('contract-reset').addEventListener('click',()=>{form.reset();form.elements.contractId.value='';message.textContent='';});
  form.addEventListener('submit',async event=>{
    event.preventDefault();
    if (!canWrite) return;
    message.textContent='Đang lưu...';
    const data = new URLSearchParams(new FormData(form));
    data.set('customerId',id);data.delete('contractId');
    const contractId=form.elements.contractId.value;
    const response=await fetch(api+(contractId?'/'+encodeURIComponent(contractId):''),{
      method:contractId?'PUT':'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},
      credentials:'same-origin',body:data.toString()
    }).catch(()=>null);
    if (!response || !response.ok) {message.textContent='Không lưu được hợp đồng. Kiểm tra dữ liệu và quyền truy cập.';return;}
    form.reset();form.elements.contractId.value='';message.textContent='Đã lưu hợp đồng.';
    try {await refresh();const summary=document.getElementById('contract-summary');
      const r=await fetch(api+'/summary/'+encodeURIComponent(id),{credentials:'same-origin'});
      if(r.ok){const x=await r.json();summary.textContent='Đã ký: '+x.signedCount+' hợp đồng, '+money(x.signedValue)+' | Chưa ký: '+x.unsignedCount+' hợp đồng, '+money(x.unsignedValue);}
    }catch(e){message.textContent='Đã lưu, nhưng chưa tải lại được danh sách.';}
  });
  try {
    const res=await fetch(api+'?customerId='+encodeURIComponent(id),{credentials:'same-origin'});
    if (!res.ok) throw Error('Không tải được hợp đồng');
    contracts=await res.json();
    // API returns 403 for writes by roles other than ADMIN and MANAGER.
    // Keep the form hidden until a role signal is available in the page.
    // Showing the form is harmless: server enforces write authorization.
    await refresh();
  }catch(e){list.textContent=e.message;}
});
