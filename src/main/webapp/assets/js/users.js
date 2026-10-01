document.addEventListener("DOMContentLoaded", function () {
    const excelFile = document.getElementById("excelFile");
    const selectedFileName = document.getElementById("selectedFileName");
    const fileError = document.getElementById("fileError");
    const importButton = document.getElementById("importButton");

    if (!excelFile || !selectedFileName || !fileError || !importButton) {
        return;
    }

    importButton.disabled = true;

    excelFile.addEventListener("change", function () {
        fileError.textContent = "";

        const file = excelFile.files[0];

        if (!file) {
            selectedFileName.textContent = "Chưa chọn tệp";
            importButton.disabled = true;
            return;
        }

        // Hiển thị tên file đã chọn
        selectedFileName.textContent = file.name;

        // Kiểm tra định dạng file
        const fileName = file.name.toLowerCase();

        const validFile =
            fileName.endsWith(".xlsx") ||
            fileName.endsWith(".xls");

        if (!validFile) {
            fileError.textContent =
                "Tệp không hợp lệ. Vui lòng chọn file Excel (.xlsx hoặc .xls).";

            importButton.disabled = true;
            return;
        }

        // File hợp lệ
        importButton.disabled = false;
    });
});
document.addEventListener("DOMContentLoaded",()=>{const f=document.getElementById("importForm"),x=document.getElementById("excelFile"),b=document.getElementById("importButton"),c=document.getElementById("confirmImportButton"),r=document.getElementById("importResult"),s=document.getElementById("successCount"),d=document.getElementById("failedCount"),e=document.getElementById("errorList"),m=document.getElementById("fileError");if(!f||!x||!b||!c)return;const q=document.querySelector("script[src*=\"/assets/js/users.js\"]"),p=q?new URL(q.src).pathname.replace("/assets/js/users.js",""):"",u=p+"/users/import";x.addEventListener("change",()=>{c.hidden=true;r.hidden=true});f.addEventListener("submit",async t=>{t.preventDefault();await z(true)});c.addEventListener("click",async()=>{if(confirm("X\u00e1c nh\u1eadn Import c\u00e1c d\u00f2ng h\u1ee3p l\u1ec7 v\u00e0o h\u1ec7 th\u1ed1ng?"))await z(false)});async function z(v){const h=x.files[0];if(!h){m.textContent="Vui l\u00f2ng ch\u1ecdn t\u1ec7p Excel.";return}const a=new FormData();a.append("file",h);if(v)a.append("preview","true");b.disabled=true;c.disabled=true;b.textContent=v?"\u0110ang xem tr\u01b0\u1edbc...":"\u0110ang Import...";try{const o=await fetch(u,{method:"POST",body:a}),j=await o.json();if(!o.ok)throw new Error(j.message||"Kh\u00f4ng th\u1ec3 x\u1eed l\u00fd t\u1ec7p Excel.");s.textContent=j.successCount||0;d.textContent=j.failedCount||0;e.innerHTML="";(j.errors||[]).forEach(i=>{const n=document.createElement("p");n.textContent="D\u00f2ng "+i.row+(i.email?" - "+i.email:"")+": "+i.message;e.appendChild(n)});r.hidden=false;if(v){c.hidden=Number(j.successCount||0)<=0;alert("Xem tr\u01b0\u1edbc ho\u00e0n t\u1ea5t. H\u1ee3p l\u1ec7: "+(j.successCount||0)+" - L\u1ed7i: "+(j.failedCount||0))}else{c.hidden=true;alert("Import ho\u00e0n t\u1ea5t. Th\u00e0nh c\u00f4ng: "+(j.successCount||0)+" - Th\u1ea5t b\u1ea1i: "+(j.failedCount||0))}}catch(k){m.textContent=k.message||"C\u00f3 l\u1ed7i x\u1ea3y ra.";c.hidden=true}finally{b.disabled=false;c.disabled=false;b.textContent="Xem tr\u01b0\u1edbc d\u1eef li\u1ec7u"}}});