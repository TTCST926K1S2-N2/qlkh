package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.WinLossReasonDAO;
import vn.edu.ictu.qlkh.model.WinLossReason;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class WinLossReasonService {
    private final WinLossReasonDAO dao;
    public WinLossReasonService(){this(new WinLossReasonDAO());}
    public WinLossReasonService(WinLossReasonDAO dao){if(dao==null)throw new IllegalArgumentException("DAO is required");this.dao=dao;}

    public List<WinLossReason> getAllReasons()throws SQLException{return dao.findAll();}
    public WinLossReason getReasonById(long id)throws SQLException{
        if(id<=0)throw new IllegalArgumentException("Invalid id");
        return dao.findById(id);
    }
    public void addReason(WinLossReason x)throws SQLException{validate(x);if(dao.exists(x.getCode(),0))throw new IllegalArgumentException("Duplicate code");dao.insert(x);}
    public void updateReason(WinLossReason x)throws SQLException{if(x==null||x.getId()<=0)throw new IllegalArgumentException("Invalid reason");validate(x);if(dao.exists(x.getCode(),x.getId()))throw new IllegalArgumentException("Duplicate code");dao.update(x);}
    public void deleteReason(long id)throws SQLException{if(id<=0)throw new IllegalArgumentException("Invalid id");dao.delete(id);}

    public void validate(WinLossReason x){
        if(x==null)throw new IllegalArgumentException("Reason is required");
        x.setName(trim(x.getName()));x.setCode(trim(x.getCode()).toUpperCase(Locale.ROOT));x.setType(trim(x.getType()).toUpperCase(Locale.ROOT));x.setStatus(trim(x.getStatus()).toUpperCase(Locale.ROOT));x.setDescription(trimNullable(x.getDescription()));
        if(x.getName().isEmpty()||x.getName().length()>255)throw new IllegalArgumentException("Invalid name");
        if(x.getCode().isEmpty()||x.getCode().length()>100||!x.getCode().matches("[A-Z][A-Z0-9_]*"))throw new IllegalArgumentException("Invalid code");
        if(!"WON".equals(x.getType())&&! "LOST".equals(x.getType()))throw new IllegalArgumentException("Invalid type");
        if(!"ACTIVE".equals(x.getStatus())&&! "INACTIVE".equals(x.getStatus()))throw new IllegalArgumentException("Invalid status");
        if(x.getDescription()!=null&&x.getDescription().length()>1000)throw new IllegalArgumentException("Invalid description");
    }
    private String trim(String s){return s==null?"":s.trim();}
    private String trimNullable(String s){return s==null||s.trim().isEmpty()?null:s.trim();}
}
