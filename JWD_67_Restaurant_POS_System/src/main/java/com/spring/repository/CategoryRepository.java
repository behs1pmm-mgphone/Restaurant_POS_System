package com.spring.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.spring.model.Category;

@Repository
public class CategoryRepository {

    @Autowired
    private JdbcTemplate jdbc;

    public List<Category> findAllActive() {

        String sql = "SELECT category_id, category_name FROM category WHERE is_deleted = false";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }
}