package com.spring.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.repository.MenuItemRepository;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuRepo;

    // --- NEW METHOD FOR FULL DATA ---
    // This allows the frontend to see all 19+ items for client-side pagination
    public List<Map<String, Object>> getAllItems(Integer catId, String search) {
        // We pass very high limits or call a repo method without LIMIT/OFFSET
        return menuRepo.findMenuItems(catId, search, 1000, 0);
    }

    // --- ORIGINAL METHODS REMAIN UNCHANGED ---

    public List<Map<String, Object>> getPaginatedItems(Integer catId, String search, int limit, int offset) {
        return menuRepo.findMenuItems(catId, search, limit, offset);
    }

    public long getTotalCount(Integer catId, String search) {
        return menuRepo.countMenuItems(catId, search);
    }

    public Map<String, Object> getItemById(int id) { return menuRepo.findById(id); }

    public void createItem(Map<String, Object> data, String img, int adminId) {
        String name = String.valueOf(data.getOrDefault("name", "Unnamed Item"));
        
        if (menuRepo.isNameExists(name)) {
        throw new RuntimeException("Item name '" + name + "' already exists!");
        }
        double price = Double.parseDouble(data.getOrDefault("price", "0").toString());
     
        Object catObj = data.get("categoryId");
        int catId = Integer.parseInt(catObj != null ? catObj.toString() : "1");

        String status = String.valueOf(data.getOrDefault("status", "Available"));
        menuRepo.save(name, price, catId, status, img, adminId);
    }

    public void updateItem(Map<String, Object> data, String img, int adminId) {
        Object idObj = data.get("menu_item_id");
        if (idObj == null) return; 

        int id = Integer.parseInt(idObj.toString());
        String name = String.valueOf(data.getOrDefault("name", ""));
        double price = Double.parseDouble(data.getOrDefault("price", "0").toString());

        Object catIdObj = data.get("categoryId");
        if (catIdObj == null) {
            catIdObj = data.get("category_id");
        }
         int catId = Integer.parseInt(catIdObj != null ? catIdObj.toString() : "1");
        String status = String.valueOf(data.getOrDefault("status", "Available"));
        menuRepo.update(id, name, price, catId, status, img, adminId);
    }

    public void removeRequestedItem(int id, int adminId) { menuRepo.softDelete(id, adminId); }
}