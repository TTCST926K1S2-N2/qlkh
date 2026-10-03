package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;
import vn.edu.ictu.qlkh.dao.CompetitorDAO;
import vn.edu.ictu.qlkh.model.Competitor;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class CompetitorServiceTest {
    static class DAO extends CompetitorDAO {
        boolean duplicate;
        @Override public boolean exists(String code,long id)throws SQLException{return duplicate;}
    }
    @Test void normalize(){Competitor x=new Competitor();x.setName(" ABC ");x.setCode(" competitor_a ");x.setStatus("active");new CompetitorService(new DAO()).validate(x);assertEquals("COMPETITOR_A",x.getCode());assertEquals("ACTIVE",x.getStatus());}
    @Test void rejectInvalidCode(){Competitor x=new Competitor();x.setName("x");x.setCode("1BAD");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new CompetitorService(new DAO()).validate(x));}
    @Test void rejectBlankName(){Competitor x=new Competitor();x.setName(" ");x.setCode("X");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new CompetitorService(new DAO()).validate(x));}
    @Test void rejectInvalidStatus(){Competitor x=new Competitor();x.setName("x");x.setCode("X");x.setStatus("BAD");assertThrows(IllegalArgumentException.class,()->new CompetitorService(new DAO()).validate(x));}
    @Test void rejectDuplicateCode()throws Exception{DAO d=new DAO();d.duplicate=true;Competitor x=new Competitor();x.setName("x");x.setCode("X");x.setStatus("ACTIVE");assertThrows(IllegalArgumentException.class,()->new CompetitorService(d).addCompetitor(x));}
}
