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
import com.spring.repository.AreaRepository;
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
    private AreaRepository areaRepository;

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
    public String deleteArea(@PathVariable("id") String encodedId,
                             HttpSession session,
                             RedirectAttributes ra) {
        try {
            // 1. Session Check
            UserBean user = (UserBean) session.getAttribute("loginUser");
            if (user == null) return "redirect:/login";

            // 2. Decode and Execute
            int actualId = decodeId(encodedId);
            int rowsAffected = areaRepository.areaSoftDelete(actualId, user.getUserId());

            if (rowsAffected > 0) {
                // 3. Success Logic & WebSocket
                Map<String, Object> msg = new HashMap<>();
                msg.put("action", "DELETE");
                msg.put("maskedId", encodedId);
                broadcast("/topic/area-updates", msg);

                ra.addFlashAttribute("success", "Area removed successfully.");
            } else {
                ra.addFlashAttribute("error", "Area not found or already deleted.");
            }

        } catch (Exception e) {
            ra.addFlashAttribute("error", "An error occurred: " + e.getMessage());
        }
        return "redirect:/admin/areas";
    }



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
                           @RequestParam("input_area_id") String maskedAreaId,
                           RedirectAttributes ra) {
        Integer actualAreaId = null;
        try {
            actualAreaId = decodeId(maskedAreaId);
            Area area = areaService.getAreaById(actualAreaId);

            if (area == null) {
                ra.addFlashAttribute("error", "Area not found.");
                return "redirect:/admin/areas";
            }


            boolean isDuplicate = tableService.getTablesByArea(actualAreaId).stream()
                    .anyMatch(t -> t.getTable_number().equalsIgnoreCase(table.getTable_number().trim()));

            if (isDuplicate) {
                ra.addFlashAttribute("error", "Table " + table.getTable_number() + " already exists in " + area.getAreaName() + "!");


                String encodedName = URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8).replace("+", "%20");
                return "redirect:/admin/areas/" + encodedName + "/tables";
            }


            table.setArea_id(actualAreaId);
            table.setStatus("Available");
            tableService.saveTable(table);


            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "SHOW_NOTIFICATION");
            msg.put("type", "SUCCESS");
            msg.put("message", "Table " + table.getTable_number() + " was added to " + area.getAreaName() + "!");
            msg.put("maskedAreaId", maskedAreaId);
            msg.put("action_type", "ADD");

            broadcast("/topic/table-updates", msg);


            ra.addFlashAttribute("success", "Table " + table.getTable_number() + " added!");


            String encodedName = URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8).replace("+", "%20");
            return "redirect:/admin/areas/" + encodedName + "/tables";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "System Error: " + e.getMessage());
            return "redirect:/admin/areas";
        }
    }

    @GetMapping("/admin/tables/{tableId}/update-status/{status}/{areaId}")
    public String updateStatus(@PathVariable String tableId,
                               @PathVariable String status,
                               @PathVariable String areaId,
                               RedirectAttributes ra) {
        Integer aId = null;
        try {
            Integer tId = decodeId(tableId);
            aId = decodeId(areaId);

            tableService.updateTableStatus(tId, status);


            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "SHOW_NOTIFICATION");
            msg.put("type", "SUCCESS");
            msg.put("message", "Table status changed to " + status);
            broadcast("/topic/table-updates", msg);


            ra.addFlashAttribute("success", "Status updated to " + status);

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Update failed.");
            return "redirect:/admin/areas";
        }

        Area area = areaService.getAreaById(aId);


        String encodedName = URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8)
                             .replace("+", "%20");

        return "redirect:/admin/areas/" + encodedName + "/tables";
    }
    @GetMapping("/admin/tables/{maskedAreaId}/delete/{maskedTableId}")
    public String deleteTable(@PathVariable("maskedAreaId") String maskedAreaId,
                              @PathVariable("maskedTableId") String maskedTableId,
                              HttpSession session,
                              RedirectAttributes ra) {
        try {
            UserBean user = (UserBean) session.getAttribute("loginUser");
            if (user == null) return "redirect:/login";


            Integer areaId = decodeId(maskedAreaId);
            Integer tableId = decodeId(maskedTableId);


            tableService.softDeleteTable(tableId, user.getUserId());


            Map<String, Object> msg = new HashMap<>();
            msg.put("action", "SHOW_NOTIFICATION");
            msg.put("type", "SUCCESS");
            msg.put("message", "A table has been removed.");

            msg.put("id", tableId);
            msg.put("action_type", "DELETE");
            broadcast("/topic/table-updates", msg);

            // 4. Get Area for Redirect
            Area area = areaService.getAreaById(areaId);
            if (area == null) {
                ra.addFlashAttribute("success", "Table removed.");
                return "redirect:/admin/areas";
            }

            ra.addFlashAttribute("success", "Table removed successfully.");


            String encodedName = URLEncoder.encode(area.getAreaName(), StandardCharsets.UTF_8)
                                 .replace("+", "%20");

            return "redirect:/admin/areas/" + encodedName + "/tables";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/admin/areas";
        }
    }

    @PostMapping("/admin/areas/update")
    public String updateArea(@RequestParam("areaId") String maskedAreaId,
                             @RequestParam("areaName") String areaName,
                             HttpSession session, // Session ကို ယူဖို့ ထည့်ပါ
                             RedirectAttributes ra) {
        try {
            Integer actualId = decodeId(maskedAreaId);
            Area area = areaService.getAreaById(actualId);

            if (area != null) {
                area.setAreaName(areaName.trim());

                // --- ပြင်ဆင်ရန် နေရာ ---
                // Session ထဲကနေ loginUser ရဲ့ ID ကို ယူမယ်
                // (သင့်ရဲ့ Session key နာမည် 'loginUser' မဟုတ်ရင် ပြန်စစ်ပေးပါ)
                UserBean loginUser = (UserBean) session.getAttribute("loginUser");
                Integer adminId = loginUser.getUserId();

                // saveArea ကို parameter ၂ ခုနဲ့ ခေါ်ပါ
                areaService.saveArea(area, adminId);
                // ---------------------

             // String အစား Map ထဲထည့်ပြီး ပို့ရပါမယ်
                Map<String, Object> areaUpdateMsg = new HashMap<>();
                areaUpdateMsg.put("action", "REFRESH_AREAS");

                // အခုနက error တက်နေတဲ့ နေရာမှာ ဒါလေးနဲ့ အစားထိုးပါ
                broadcast("/topic/area-updates", areaUpdateMsg);
                ra.addFlashAttribute("success", "Area updated successfully!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Update failed.");
        }
        return "redirect:/admin/areas";
    }

}