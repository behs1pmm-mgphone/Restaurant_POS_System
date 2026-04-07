package com.spring.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.spring.model.UserBean;

@Repository
public class UserRepository {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate; // Added to fix the save method error

    public UserBean findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE LOWER(email) = ? AND is_deleted = 0";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserBean user = new UserBean();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRoleId(rs.getInt("role_id"));
                    user.setStatus(rs.getInt("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setCreatedBy(rs.getObject("created_by", Integer.class));
                    user.setDeleted(rs.getBoolean("is_deleted"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Error (findByEmail): " + e.getMessage());
        }
        return null;
    }

    public List<UserBean> findAll() {
        return findAll(false);
    }

    public List<UserBean> findAll(boolean includeDeleted) {
        List<UserBean> list = new ArrayList<>();

        String sql =
                "SELECT u.*, " +
                "       c.user_name  AS created_by_name, " +
                "       up.user_name AS updated_by_name, " +
                "       d.user_name  AS deleted_by_name " +
                "FROM user u " +
                "LEFT JOIN user c  ON u.created_by = c.user_id " +
                "LEFT JOIN user up ON u.updated_by = up.user_id " +
                "LEFT JOIN user d  ON u.deleted_by = d.user_id " +
                (includeDeleted ? "" : "WHERE u.is_deleted = 0 ") +
                "ORDER BY u.user_id DESC";

        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UserBean user = new UserBean();
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id"));
                user.setStatus(rs.getInt("status"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setCreatedBy(rs.getObject("created_by", Integer.class));
                user.setCreatedByName(rs.getString("created_by_name"));
                user.setUpdatedAt(rs.getTimestamp("updated_at"));
                user.setUpdatedBy(rs.getObject("updated_by", Integer.class));
                user.setUpdatedByName(rs.getString("updated_by_name"));
                user.setDeleted(rs.getBoolean("is_deleted"));
                user.setDeletedAt(rs.getTimestamp("deleted_at"));
                user.setDeletedBy(rs.getObject("deleted_by", Integer.class));
                user.setDeletedByName(rs.getString("deleted_by_name"));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public int countActiveUsersByName(String keyword) {
        String base = "SELECT COUNT(*) FROM user u WHERE u.is_deleted = 0";
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String sql = hasKeyword ? base + " AND u.user_name LIKE ?" : base;

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (hasKeyword) {
                ps.setString(1, "%" + keyword.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<UserBean> findActiveUsersPageByName(String keyword, int offset, int limit) {
        List<UserBean> list = new ArrayList<>();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        String sql =
                "SELECT u.*, " +
                "       c.user_name  AS created_by_name, " +
                "       up.user_name AS updated_by_name " +
                "FROM user u " +
                "LEFT JOIN user c  ON u.created_by = c.user_id " +
                "LEFT JOIN user up ON u.updated_by = up.user_id " +
                "WHERE u.is_deleted = 0 " +
                (hasKeyword ? "AND u.user_name LIKE ? " : "") +
                "ORDER BY u.user_id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int idx = 1;
            if (hasKeyword) {
                ps.setString(idx++, "%" + keyword.trim() + "%");
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserBean user = new UserBean();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRoleId(rs.getInt("role_id"));
                    user.setStatus(rs.getInt("status"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setCreatedBy(rs.getObject("created_by", Integer.class));
                    user.setCreatedByName(rs.getString("created_by_name"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    user.setUpdatedBy(rs.getObject("updated_by", Integer.class));
                    user.setUpdatedByName(rs.getString("updated_by_name"));
                    user.setDeleted(false);
                    list.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void save(UserBean user) {
        String sql = "INSERT INTO user (user_name, password, email, role_id, status, created_by, created_at, is_deleted) " +
                     "VALUES (?, ?, ?, ?, 0, ?, CURRENT_TIMESTAMP, 0)";
        try {
            // FIXED: Using the injected jdbcTemplate instance instead of static call
            jdbcTemplate.update(sql,
                user.getUserName(),
                user.getPassword(),
                user.getEmail(),
                user.getRoleId(),
                user.getCreatedBy()
            );
        } catch (Exception e) {
            System.out.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(UserBean user) {
        // FIXED: Added updated_by and updated_at to match your DB screenshot
        String sql = "UPDATE user SET user_name = ?, email = ?, password = ?, role_id = ?, " +
                     "updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getUserName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());

            if (user.getRoleId() != null) ps.setInt(4, user.getRoleId());
            else ps.setNull(4, java.sql.Types.INTEGER);
         // Set the Admin ID who performed the update
            if (user.getUpdatedBy() != null) ps.setInt(5, user.getUpdatedBy());
            else ps.setNull(5, java.sql.Types.INTEGER);

            if (user.getUserId() != null) ps.setInt(6, user.getUserId());
            else return;

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int userId, Integer deletedBy) {
        String sql = "UPDATE user " +
                     "SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP, deleted_by = ?, " +
                     "    updated_at = CURRENT_TIMESTAMP, updated_by = ? " +
                     "WHERE user_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (deletedBy != null) ps.setInt(1, deletedBy);
            else ps.setNull(1, java.sql.Types.INTEGER);

            if (deletedBy != null) ps.setInt(2, deletedBy);
            else ps.setNull(2, java.sql.Types.INTEGER);

            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public UserBean findById(int id) {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserBean user = new UserBean();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUserName(rs.getString("user_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setRoleId(rs.getInt("role_id"));
                    user.setStatus(rs.getInt("status"));
                    return user;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updateStatus(int userId, int status, Integer updatedBy) {
        String sql = "UPDATE user SET status = ?, updated_at = CURRENT_TIMESTAMP, updated_by = ? WHERE user_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, status);
            if (updatedBy != null) ps.setInt(2, updatedBy);
            else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}