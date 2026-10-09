(function(){
  'use strict';
  const context = document.body.dataset.contextPath || (function(){
    const script = document.currentScript;
    const src = script && script.src ? new URL(script.src).pathname : '';
    return src.replace(/\/assets\/js\/shared-header\.js$/, '');
  })();
  function avatarUrl(url){
    if(!url || typeof url!=='string') return null;
    if(/^https?:\/\//i.test(url)) return url;
    if(!url.startsWith('/') || url.startsWith('//')) return null;
    return context+url;
  }
  function updateAvatars(url){
    const safe=avatarUrl(url);
    document.querySelectorAll('.qlkh-header-avatar, .sidebar .user-avatar').forEach(el=>{
      let img=el.querySelector('img.qlkh-synced-avatar');
      if(!safe){if(img)img.remove();el.querySelectorAll('.qlkh-avatar-initial').forEach(x=>x.hidden=false);return;}
      if(!img){img=document.createElement('img');img.className='qlkh-synced-avatar';img.alt='Ảnh đại diện';img.addEventListener('error',()=>{img.remove();el.querySelectorAll('.qlkh-avatar-initial').forEach(x=>x.hidden=false);});el.appendChild(img);}
      img.onload=()=>el.querySelectorAll('.qlkh-avatar-initial').forEach(x=>x.hidden=true);
      img.src=safe;
    });
  }
  async function refreshAvatar(){
    try{
      const response=await fetch(context+'/api/users/avatar',{credentials:'same-origin',cache:'no-store'});
      if(!response.ok)return;
      const result=await response.json();
      if(result && result.success===true) updateAvatars(result.thumbnailUrl||result.avatarUrl);
    }catch(e){console.warn('Avatar sync:',e);}
  }
  window.qlkhRefreshAvatar=refreshAvatar;
  window.qlkhSetAvatar=updateAvatars;
  document.addEventListener('DOMContentLoaded',()=>{
    const search=document.getElementById('qlkhHeaderSearch');
    const input=document.getElementById('qlkhHeaderSearchInput');
    if(search&&input)search.addEventListener('submit',e=>{e.preventDefault();const q=input.value.trim();if(q)location.href=context+'/customers?search='+encodeURIComponent(q);});
    const pairs=[['qlkhNotificationButton','qlkhNotificationPanel'],['qlkhUserButton','qlkhUserPanel']];
    pairs.forEach(([bid,pid])=>{const b=document.getElementById(bid),p=document.getElementById(pid);if(!b||!p)return;b.addEventListener('click',()=>{const open=p.hidden;pairs.forEach(([x,y])=>{const xb=document.getElementById(x),yp=document.getElementById(y);if(yp)yp.hidden=true;if(xb)xb.setAttribute('aria-expanded','false');});p.hidden=!open;b.setAttribute('aria-expanded',String(open));});});
    document.addEventListener('click',e=>{if(e.target.closest('.qlkh-header-popover-wrap'))return;pairs.forEach(([x,y])=>{const b=document.getElementById(x),p=document.getElementById(y);if(p)p.hidden=true;if(b)b.setAttribute('aria-expanded','false');});});
    const logout=document.getElementById('qlkhHeaderLogout');
    if(logout)logout.addEventListener('click',()=>{const f=document.getElementById('sidebar-logout-form');if(f)f.submit();});
    const role=document.getElementById('qlkhHeaderUserRole'),sideRole=document.querySelector('.sidebar .user-role');
    if(role&&sideRole)role.textContent=sideRole.textContent.trim();
    refreshAvatar();
  });
})();
