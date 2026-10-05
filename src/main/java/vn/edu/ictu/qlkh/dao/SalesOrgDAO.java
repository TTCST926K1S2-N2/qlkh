package vn.edu.ictu.qlkh.dao;

import vn.edu.ictu.qlkh.model.SalesOrganization;
import vn.edu.ictu.qlkh.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesOrgDAO {

    private static final String BASE_SELECT = """
            SELECT
                bg.id,
                bg.code,
                bg.name,
                bg.parent_id,
                bg.leader_id,
                bg.region_id,
                bg.status,
                bg.created_at,
                u.full_name AS leader_name,
                r.name AS region_name
            FROM business_groups bg
            LEFT JOIN users u
                ON u.id = bg.leader_id
            LEFT JOIN regions r
                ON r.id = bg.region_id
            """;


    public List<SalesOrganization> findAll()
            throws SQLException {

        String sql =
                BASE_SELECT +
                " ORDER BY bg.name ASC";

        List<SalesOrganization> result =
                new ArrayList<>();

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet rs =
                     statement.executeQuery()) {

            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }


    public SalesOrganization findById(long id)
            throws SQLException {

        String sql =
                BASE_SELECT +
                " WHERE bg.id = ? LIMIT 1";

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                return rs.next()
                        ? map(rs)
                        : null;
            }
        }
    }


    public boolean existsById(long id)
            throws SQLException {

        return findById(id) != null;
    }


    public boolean existsByCode(
            String code,
            Long excludeId
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM business_groups
                WHERE UPPER(code) = UPPER(?)
                  AND (? IS NULL OR id <> ?)
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, code);

            if (excludeId == null) {
                statement.setNull(
                        2,
                        Types.BIGINT
                );
                statement.setNull(
                        3,
                        Types.BIGINT
                );
            } else {
                statement.setLong(
                        2,
                        excludeId
                );
                statement.setLong(
                        3,
                        excludeId
                );
            }

            try (ResultSet rs =
                         statement.executeQuery()) {

                return rs.next();
            }
        }
    }


    public SalesOrganization insert(
            SalesOrganization organization
    ) throws SQLException {

        String sql = """
                INSERT INTO business_groups (
                    code,
                    name,
                    parent_id,
                    leader_id,
                    region_id,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            bind(statement, organization);

            statement.executeUpdate();

            try (ResultSet keys =
                         statement.getGeneratedKeys()) {

                if (!keys.next()) {
                    throw new SQLException(
                            "Không lấy được ID nhóm vừa tạo."
                    );
                }

                organization.setId(
                        keys.getLong(1)
                );
            }
        }

        return findById(
                organization.getId()
        );
    }


    public SalesOrganization update(
            SalesOrganization organization
    ) throws SQLException {

        String sql = """
                UPDATE business_groups
                SET
                    code = ?,
                    name = ?,
                    parent_id = ?,
                    leader_id = ?,
                    region_id = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bind(statement, organization);

            statement.setLong(
                    7,
                    organization.getId()
            );

            int affected =
                    statement.executeUpdate();

            if (affected == 0) {
                return null;
            }
        }

        return findById(
                organization.getId()
        );
    }


    public boolean deactivate(long id)
            throws SQLException {

        String sql = """
                UPDATE business_groups
                SET status = 'INACTIVE'
                WHERE id = ?
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }


    public boolean hasChildren(long id)
            throws SQLException {

        String sql = """
                SELECT 1
                FROM business_groups
                WHERE parent_id = ?
                LIMIT 1
                """;

        try (Connection connection =
                     DBConnection.getConnection();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet rs =
                         statement.executeQuery()) {

                return rs.next();
            }
        }
    }


    public boolean isDescendant(
            long candidateParentId,
            long groupId
    ) throws SQLException {

        Long currentId =
                candidateParentId;

        while (currentId != null) {

            if (currentId == groupId) {
                return true;
            }

            SalesOrganization current =
                    findById(currentId);

            if (current == null) {
                return false;
            }

            currentId =
                    current.getParentId();
        }

        return false;
    }


    public List<vn.edu.ictu.qlkh.model.User> findMembersByGroupId(
            long groupId
    ) throws SQLException {
        String sql = """
                SELECT u.id, u.full_name, u.email,
                       u.role, u.status
                FROM user_business_groups ubg
                JOIN users u ON u.id = ubg.user_id
                WHERE ubg.group_id = ?
                ORDER BY u.full_name, u.id
                """;

        List<vn.edu.ictu.qlkh.model.User> members =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, groupId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vn.edu.ictu.qlkh.model.User user =
                            new vn.edu.ictu.qlkh.model.User();

                    user.setId(rs.getLong("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setStatus(rs.getString("status"));

                    members.add(user);
                }
            }
        }

        return members;
    }

    private void bind(
            PreparedStatement statement,
            SalesOrganization organization
    ) throws SQLException {

        statement.setString(
                1,
                organization.getCode()
        );

        statement.setString(
                2,
                organization.getName()
        );

        setNullableLong(
                statement,
                3,
                organization.getParentId()
        );

        setNullableLong(
                statement,
                4,
                organization.getLeaderId()
        );

        setNullableLong(
                statement,
                5,
                organization.getRegionId()
        );

        statement.setString(
                6,
                organization.getStatus()
        );
    }


    private void setNullableLong(
            PreparedStatement statement,
            int index,
            Long value
    ) throws SQLException {

        if (value == null) {
            statement.setNull(
                    index,
                    Types.BIGINT
            );
        } else {
            statement.setLong(
                    index,
                    value
            );
        }
    }


    private SalesOrganization map(
            ResultSet rs
    ) throws SQLException {

        SalesOrganization result =
                new SalesOrganization();

        result.setId(
                rs.getLong("id")
        );

        result.setCode(
                rs.getString("code")
        );

        result.setName(
                rs.getString("name")
        );

        result.setParentId(
                getNullableLong(
                        rs,
                        "parent_id"
                )
        );

        result.setLeaderId(
                getNullableLong(
                        rs,
                        "leader_id"
                )
        );

        result.setRegionId(
                getNullableLong(
                        rs,
                        "region_id"
                )
        );

        result.setStatus(
                rs.getString("status")
        );

        result.setLeaderName(
                rs.getString("leader_name")
        );

        result.setRegionName(
                rs.getString("region_name")
        );

        result.setCreatedAt(
                rs.getTimestamp("created_at")
        );

        return result;
    }


    private Long getNullableLong(
            ResultSet rs,
            String column
    ) throws SQLException {

        long value =
                rs.getLong(column);

        return rs.wasNull()
                ? null
                : value;
    }
}