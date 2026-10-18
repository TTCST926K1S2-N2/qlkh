package vn.edu.ictu.qlkh.service;

import org.junit.jupiter.api.Test;

import vn.edu.ictu.qlkh.dao.CustomFieldDAO;
import vn.edu.ictu.qlkh.model.CustomField;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class CustomFieldServiceTest {

    @Test
    void constructorRejectsNullDao() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CustomFieldService(null));
    }

    @Test
    void getFieldByIdRejectsInvalidId() {
        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getFieldById(0));
    }

    @Test
    void addFieldRejectsInvalidEntityType() {
        CustomField field = createValidField();
        field.setEntityType("PRODUCT");

        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addField(field));
    }

    @Test
    void addFieldRejectsInvalidFieldKey() {
        CustomField field = createValidField();
        field.setFieldKey("123-invalid");

        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addField(field));
    }

    @Test
    void addFieldRejectsUnsupportedFieldType() {
        CustomField field = createValidField();
        field.setFieldType("FILE");

        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addField(field));
    }

    @Test
    void selectFieldRequiresConfig() {
        CustomField field = createValidField();
        field.setFieldType("SELECT");
        field.setConfig(null);

        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addField(field));
    }

    @Test
    void addValidFieldSucceeds() throws SQLException {
        FakeCustomFieldDAO dao = new FakeCustomFieldDAO();

        CustomFieldService service =
                new CustomFieldService(dao);

        CustomField field = createValidField();

        service.addField(field);

        assertTrue(dao.insertCalled);
        assertEquals("CUSTOMER", field.getEntityType());
        assertEquals("TEXT", field.getFieldType());
    }

    @Test
    void duplicateFieldIsRejected() {
        FakeCustomFieldDAO dao = new FakeCustomFieldDAO();
        dao.existsResult = true;

        CustomFieldService service =
                new CustomFieldService(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.addField(createValidField()));
    }

    @Test
    void deleteInvalidIdIsRejected() {
        CustomFieldService service =
                new CustomFieldService(new FakeCustomFieldDAO());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteField(-1));
    }

    private CustomField createValidField() {
        CustomField field = new CustomField();

        field.setEntityType("customer");
        field.setFieldKey("customer_note");
        field.setFieldName("Ghi chú khách hàng");
        field.setFieldType("text");
        field.setRequired(false);
        field.setStatus(true);
        field.setDisplayOrder(1);
        field.setConfig(null);

        return field;
    }

    static class FakeCustomFieldDAO extends CustomFieldDAO {

        boolean existsResult;
        boolean insertCalled;

        @Override
        public boolean exists(
                String entityType,
                String fieldKey,
                long excludeId) {
            return existsResult;
        }

        @Override
        public boolean insert(CustomField field) {
            insertCalled = true;
            return true;
        }

        @Override
        public CustomField findById(long id) {
            CustomField field = createFakeField();
            field.setId(id);
            return field;
        }

        @Override
        public boolean update(CustomField field) {
            return true;
        }

        @Override
        public boolean delete(long id) {
            return true;
        }

        private CustomField createFakeField() {
            CustomField field = new CustomField();

            field.setEntityType("CUSTOMER");
            field.setFieldKey("customer_note");
            field.setFieldName("Ghi chú");
            field.setFieldType("TEXT");
            field.setRequired(false);
            field.setStatus(true);
            field.setDisplayOrder(1);

            return field;
        }
    }
}
