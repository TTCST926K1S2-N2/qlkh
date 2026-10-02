document.addEventListener("DOMContentLoaded", () => {
  const form=document.getElementById("avatarForm"), fileInput=document.getElementById("avatarFile"),
    preview=document.getElementById("avatarPreview"), placeholder=document.getElementById("avatarPlaceholder"),
    fileName=document.getElementById("selectedFileName"), error=document.getElementById("avatarError"),
    message=document.getElementById("avatarMessage"), removeBtn=document.getElementById("removeAvatarButton"),
    uploadBtn=document.getElementById("uploadAvatarButton");
  const MAX=2*1024*1024, context=document.body.dataset.contextPath||"";
  let objectUrl=null;

  function showMessage(text,type){
    message.textContent=text; message.className="avatar-message "+type; message.hidden=false;
  }
  function clearMessage(){ message.hidden=true; message.textContent=""; message.className="avatar-message"; }
  function valid(file){
    const name=file.name.toLowerCase();
    return name.endsWith(".jpg")||name.endsWith(".jpeg")||name.endsWith(".png");
  }
  function clearPreview(){
    fileInput.value=""; fileName.textContent="Chưa chọn ảnh"; error.textContent=""; clearMessage();
    if(objectUrl){URL.revokeObjectURL(objectUrl);objectUrl=null;}
    preview.src=""; preview.hidden=true; placeholder.hidden=false;
    removeBtn.disabled=true; uploadBtn.disabled=true;
  }
  fileInput.addEventListener("change",()=>{
    clearMessage(); error.textContent="";
    const file=fileInput.files[0];
    if(!file){clearPreview();return;}
    fileName.textContent=file.name; removeBtn.disabled=false;
    if(!valid(file)){error.textContent="Chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.";uploadBtn.disabled=true;return;}
    if(file.size>MAX){error.textContent="Ảnh không được vượt quá 2MB.";uploadBtn.disabled=true;return;}
    if(objectUrl)URL.revokeObjectURL(objectUrl);
    objectUrl=URL.createObjectURL(file); preview.src=objectUrl; preview.hidden=false; placeholder.hidden=true; uploadBtn.disabled=false;
  });
  removeBtn.addEventListener("click",clearPreview);

  form.addEventListener("submit",async e=>{
    e.preventDefault(); clearMessage(); error.textContent="";
    const file=fileInput.files[0];
    if(!file){error.textContent="Vui lòng chọn ảnh đại diện.";return;}
    if(!valid(file)){error.textContent="Chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.";return;}
    if(file.size>MAX){error.textContent="Ảnh không được vượt quá 2MB.";return;}
    const data=new FormData(); data.append("avatar",file);
    uploadBtn.disabled=true; removeBtn.disabled=true; uploadBtn.textContent="Đang tải...";
    try{
      const response=await fetch(form.action,{method:"POST",body:data,credentials:"same-origin"});
      const result=await response.json().catch(()=>null);
      if(!response.ok||!result||result.success!==true) throw new Error(result?.message||"Không thể tải ảnh đại diện.");
      const url=result.thumbnailUrl||result.avatarUrl;
      if(url){preview.src=/^https?:\/\//i.test(url)?url:context+(url.startsWith("/")?url:"/"+url);preview.hidden=false;placeholder.hidden=true;}
      if(objectUrl){URL.revokeObjectURL(objectUrl);objectUrl=null;}
      fileInput.value=""; fileName.textContent="Đã tải ảnh thành công"; removeBtn.disabled=true; uploadBtn.disabled=true;
      showMessage("Cập nhật ảnh đại diện thành công.","success");
    }catch(ex){
      error.textContent=ex.message||"Không thể tải ảnh đại diện."; removeBtn.disabled=false; uploadBtn.disabled=false;
      showMessage(error.textContent,"error");
    }finally{uploadBtn.textContent="Tải ảnh đại diện";}
  });
  async function loadCurrentAvatar(){
    try{
      const response=await fetch(form.action,{
        method:"GET",
        credentials:"same-origin"
      });
      const result=await response.json().catch(()=>null);

      if(!response.ok||!result||result.success!==true||!result.avatarUrl){
        return;
      }

      const url=result.avatarUrl;
      preview.src=/^https?:\/\//i.test(url)
        ? url
        : context+(url.startsWith("/")?url:"/"+url);

      preview.hidden=false;
      placeholder.hidden=true;
    }catch(ex){
      console.error("Cannot load avatar",ex);
    }
  }

  loadCurrentAvatar();

  window.addEventListener("beforeunload",()=>{if(objectUrl)URL.revokeObjectURL(objectUrl);});
});