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

    /**
     * အဓိက ပြင်ရမည့်နေရာ- saveTable method တွင် adminId ကိုပါ လက်ခံပြီး
     * repository ထံ ထပ်ဆင့်ပေးပို့ရပါမည်။
     */
    public RestaurantTable saveTable(RestaurantTable t, Integer adminId) {
        // Table နံပါတ် တူနေခြင်း ရှိ/မရှိ အရင်စစ်ဆေးပါသည်
        if (tableRepo.existsByTableNumber(t.getTable_number())) {
            throw new RuntimeException("Table number '" + t.getTable_number() + "' already exists!");
        }

        // Repository ရှိ saveTable(RestaurantTable, Integer) method ကို လှမ်းခေါ်ပါသည်
        tableRepo.saveTable(t, adminId);
        return t;
    }

    public void updateTableStatus(Integer id, String status) {
        tableRepo.updateTableStatus(id, status);
    }

    public void softDeleteTable(Integer tableId, Integer adminId) {
        tableRepo.softDeleteTable(tableId, adminId);
    }
}