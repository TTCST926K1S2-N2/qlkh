package vn.edu.ictu.qlkh.service;

import vn.edu.ictu.qlkh.dao.CustomerRelationshipDAO;
import vn.edu.ictu.qlkh.model.Customer;
import java.sql.SQLException;
import java.util.*;

public class CustomerRelationshipService {
    private final CustomerService customers=new CustomerService();
    private final CustomerRelationshipDAO dao=new CustomerRelationshipDAO();

    private void requireVisible(long id,long actorId,String role) throws SQLException {
        if(id<=0) throw new IllegalArgumentException("ID khong hop le");
        Customer c=customers.detail(id,actorId,role);
        if(c==null) throw new SecurityException("Cong ty ngoai pham vi truy cap hoac khong ton tai");
    }
    public List<CustomerRelationshipDAO.Link> visible(long actorId,String role) throws SQLException {
        Set<Long> ids=new HashSet<>();
        for(Customer c:customers.list(actorId,role)) ids.add(c.getId());
        List<CustomerRelationshipDAO.Link> result=new ArrayList<>();
        for(CustomerRelationshipDAO.Link link:dao.all()) {
            if(ids.contains(link.id())) result.add(new CustomerRelationshipDAO.Link(
                    link.id(),link.name(),link.parentId()!=null && ids.contains(link.parentId()) ? link.parentId():null));
        }
        return result;
    }
    public void update(long childId,Long parentId,long actorId,String role) throws SQLException {
        requireVisible(childId,actorId,role);
        if(parentId!=null) requireVisible(parentId,actorId,role);
        dao.setParent(childId,parentId);
    }
}