package com.spring.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.model.Area;
import com.spring.model.RestaurantTable;
import com.spring.model.UserBean;
import com.spring.service.AreaService;
import com.spring.service.TableService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TableController {

    @Autowired
    private TableService tableService;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // --- ID Masking ---
    private String encodeId(Integer id) {
        if (id == null) return "";
        return Base64.getEncoder().encodeToString(id.toString().getBytes());
    }

    private Integer decodeId(String encodedId) {
        try {
            return Integer.parseInt(new String(Base64.getDecoder().decode(encodedId)));
        } catch (Exception e) { return null; }
    }

    private void broadcast(String destination, Map<String, Object> payload) {
        messagingTemplate.convertAndSend(destination, (Object) payload);
    }

    // ==========================================
    // AREA MANAGEMENT
    // ==========================================

    @GetMapping("/admin/areas")
    public String showAreas(Model model, HttpSession session) {
        UserBean user = (UserBean) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        var areaList = areaService.getAllAreas().stream().map(area -> {
            Map<String, Object> map = new HashMap<>();
            map.put("areaName", area.getAreaName());
            map.put("status", area.getStatus());
            map.put("maskedId", encodeId(area.getAreaId()));
            return map;
        }).toList();

        model.addAttribute("areas", areaList);
        return "admin-areas";
    }

    @PostMapping("/admin/areas/add")
    public String addArea(@RequestParam("areaName") String areaName, HttpSession session, RedirectAttributes ra) {
        try {
            UserBean user = (UserBean) session.getAttribute("loginUser");
            if (user == null) return "redirect:/login";

            Area area = new Area();
            area.setAreaName(areaName.trim());
            area.setStatus("Active");
            Area saved = areaService.saveArea(area, user.getUserId());

            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "ADD");
            msg.put("areaName", saved.getAreaName());
            msg.put("maskedId", encodeId(saved.getAreaId()));
            broadcast("/topic/area-updates", msg);

            ra.addFlashAttribute("success", "Area '" + saved.getAreaName() + "' created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/areas";
    }

    @GetMapping("/admin/areas/toggle-status/{id}")
    public String toggleAreaStatus(@PathVariable("id") String encodedId, HttpSession session, RedirectAttributes ra) {
        try {
            UserBean user = (UserBean) session.getAttribute("loginUser");
            if (user == null) return "redirect:/login";

            Integer areaId = decodeId(encodedId);
            Area area = areaService.getAreaById(areaId);
            if (area != null) {
                String newStatus = area.getStatus().equalsIgnoreCase("Active") ? "Disabled" : "Active";
                area.setStatus(newStatus);
                areaService.saveArea(area, user.getUserId());

                Map<String, Object> msg = new HashMap<>();
                msg.put("action", "TOGGLE");
                msg.put("maskedId", encodedId);
                msg.put("status", newStatus);
                broadcast("/topic/area-updates", msg);

                ra.addFlashAttribute("success", "Area status successfully changed to " + newStatus);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to update area: " + e.getMessage());
        }
        return "redirect:/admin/areas";
    }

    @GetMapping("/admin/areas/delete/{id}")
    public String deleteArea(@PathVariable("id") String encodedId, HttpSession session, RedirectAttributes ra) {
        try {
            UserBean user = (UserBean) session.getAttribute("loginUser");
            if (user == null) return "redirect:/login";

            areaService.deleteArea(decodeId(encodedId), user.getUserId());

            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "DELETE");
            msg.put("maskedId", encodedId);
            broadcast("/topic/area-updates", msg);
            ra.addFlashAttribute("success", "Area removed successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/areas";
    }

    // ==========================================
    // TABLE MANAGEMENT
    // ==========================================

    @GetMapping("/admin/areas/{name}/tables")
    public String showTables(@PathVariable("name") String areaName, Model model, HttpSession session) {
        // Prevents Thymeleaf null error if session expires
        if (session.getAttribute("loginUser") == null) return "redirect:/login";

        Area area = areaService.findByAreaName(areaName);
        if (area == null) return "redirect:/admin/areas";

        List<Map<String, Object>> tableMaps = tableService.getTablesByArea(area.getAreaId()).stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tableNumber", t.getTable_number());
            map.put("status", t.getStatus());
            map.put("tableId", t.getRestaurant_table_id());
            map.put("maskedId", encodeId(t.getRestaurant_table_id()));
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("areaName", area.getAreaName());
        model.addAttribute("maskedAreaId", encodeId(area.getAreaId()));
        model.addAttribute("tables", tableMaps);
        return "area-tables";
    }

    @PostMapping("/admin/tables/add")
    public String addTable(@ModelAttribute RestaurantTable table,
                           @RequestParam("input_area_id") String maskedAreaId, // Matches the new HTML name
                           RedirectAttributes ra) {
        Integer actualAreaId = null;
        try {
            actualAreaId = decodeId(maskedAreaId);

            // 1. Check for duplicates in this area
            boolean isDuplicate = tableService.getTablesByArea(actualAreaId).stream()
                    .anyMatch(t -> t.getTable_number().equalsIgnoreCase(table.getTable_number().trim()));

            if (isDuplicate) {
                ra.addFlashAttribute("error", "Table " + table.getTable_number() + " already exists!");
                Area area = areaService.getAreaById(actualAreaId);
                return "redirect:/admin/areas/" + URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8) + "/tables";
            }

            // 2. Manually set the decoded ID
            table.setArea_id(actualAreaId);
            tableService.saveTable(table);

            ra.addFlashAttribute("success", "Table " + table.getTable_number() + " added!");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "System Error: " + e.getMessage());
        }

        Area area = areaService.getAreaById(actualAreaId);
        return "redirect:/admin/areas/" + URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8) + "/tables";
    }


    @GetMapping("/admin/tables/{tableId}/update-status/{status}/{areaId}")
    public String updateStatus(@PathVariable String tableId,
                               @PathVariable String status,
                               @PathVariable String areaId,
                               RedirectAttributes ra) {
        try {
            Integer tId = decodeId(tableId);
            tableService.updateTableStatus(tId, status);

            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "UPDATE_STATUS");
            msg.put("id", tId);
            msg.put("status", status);
            broadcast("/topic/table-updates", msg);

            ra.addFlashAttribute("success", "Status updated to " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Update failed.");
        }

        Area area = areaService.getAreaById(decodeId(areaId));
        return "redirect:/admin/areas/" + URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8) + "/tables";
    }
}