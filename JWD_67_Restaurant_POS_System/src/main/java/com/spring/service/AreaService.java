package com.spring.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.model.Area;
import com.spring.repository.AreaRepository;

@Service
public class AreaService {

    private final AreaRepository areaRepo;

    @Autowired
    private TableService tableService;

    @Autowired
    public AreaService(AreaRepository areaRepo) {
        this.areaRepo = areaRepo;
    }

    public List<Area> getAllAreas() {
        try {
            List<Area> areas = areaRepo.findAllAreas();
            return (areas != null) ? areas : new ArrayList<>();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    public Area getAreaById(Integer id) {
        return getAllAreas().stream().filter(a -> a.getAreaId().equals(id)).findFirst().orElse(null);
    }

    public Area findByAreaName(String areaName) {
        if (areaName == null) return null;
        return getAllAreas().stream().filter(a -> a.getAreaName().equalsIgnoreCase(areaName.trim())).findFirst().orElse(null);
    }

    public Area saveArea(Area area, Integer adminId) {
        if (area.getAreaId() == null && findByAreaName(area.getAreaName()) != null) {
            throw new RuntimeException("Area name already exists!");
        }
        areaRepo.save(area, adminId);
        return findByAreaName(area.getAreaName());
    }

    public void deleteArea(Integer id, Integer adminId) {
        if (!tableService.getTablesByArea(id).isEmpty()) {
            throw new RuntimeException("Cannot delete area: Tables are still active inside.");
        }
        areaRepo.deleteById(id, adminId);
    }
}