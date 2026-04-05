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
        String sql = "SELECT * FROM user WHERE email = ? AND is_deleted = 0";
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
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setCreatedBy(rs.getInt("created_by"));
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
        List<UserBean> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE is_deleted = 0";
        try (Connection con = dataSource.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UserBean user = new UserBean();
                user.setUserId(rs.getInt("user_id"));
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setRoleId(rs.getInt("role_id"));
                user.setDeleted(rs.getBoolean("is_deleted"));
                list.add(user);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void save(UserBean user) {
        String sql = "INSERT INTO user (user_name, password, email, role_id, created_by, created_at, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 0)";
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

    public void delete(int userId) {
        String sql = "UPDATE user SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
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
                    return user;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}