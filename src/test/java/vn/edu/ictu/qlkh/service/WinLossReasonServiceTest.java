package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.WinLossReasonDAO;
import vn.edu.ictu.qlkh.model.WinLossReason;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class WinLossReasonServiceTest {
    static class DAO extends WinLossReasonDAO {
        boolean duplicate;
        @Override public boolean exists(String code,long id)throws SQLException{return duplicate;}
    }
    @Test void normalize()throws Exception{DAO d=new DAO();WinLossReasonService s=new WinLossReasonService(d);WinLossReason x=new WinLossReason();x.setName(" Win ");x.setCode(" won_price ");x.setType("won");x.setStatus("active");s.validate(x);assertEquals("WON_PRICE",x.getCode());assertEquals("WON",x.getType());assertEquals("ACTIVE",x.getStatus());}
    @Test void rejectInvalidType(){WinLossReason x=new WinLossReason();x.setName("x");x.setCode("X");x.setType("OTHER");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new WinLossReasonService(new DAO()).validate(x));}
    @Test void rejectInvalidCode(){WinLossReason x=new WinLossReason();x.setName("x");x.setCode("1BAD");x.setType("WON");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new WinLossReasonService(new DAO()).validate(x));}
    @Test void rejectBlankName(){WinLossReason x=new WinLossReason();x.setName(" ");x.setCode("X");x.setType("WON");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new WinLossReasonService(new DAO()).validate(x));}
    @Test void rejectDuplicateCode()throws Exception{DAO d=new DAO();d.duplicate=true;WinLossReason x=new WinLossReason();x.setName("x");x.setCode("X");x.setType("WON");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new WinLossReasonService(d).addReason(x));}
}
