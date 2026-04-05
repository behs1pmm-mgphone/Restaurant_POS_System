package com.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.model.RestaurantTable;
import com.spring.repository.RestaurantTableRepository;

@Service
public class TableService {

    @Autowired
    private RestaurantTableRepository tableRepo;

    public List<RestaurantTable> getTablesByArea(Integer areaId) {
        List<RestaurantTable> tables = tableRepo.findTablesByArea(areaId);
        return (tables != null) ? tables : new ArrayList<>();
    }

    public RestaurantTable saveTable(RestaurantTable t) {
        if (tableRepo.existsByTableNumber(t.getTable_number())) {
            throw new RuntimeException("Table number '" + t.getTable_number() + "' already exists!");
        }
        tableRepo.saveTable(t);
        return t;
    }

    public void updateTableStatus(Integer id, String status) {
        tableRepo.updateTableStatus(id, status);
    }

    public void updateTableStatusByArea(Integer areaId, String status) {
        List<RestaurantTable> tables = tableRepo.findTablesByArea(areaId);
        if (tables != null) {
            for (RestaurantTable table : tables) {
                tableRepo.updateTableStatus(table.getRestaurant_table_id(), status);
            }
        }
    }

    public void deleteTable(Integer id) {
        tableRepo.deleteTable(id);
    }
}