package com.spring.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.model.Area;
import com.spring.model.UserBean;
import com.spring.model.WaiterView;
import com.spring.repository.RestaurantTableRepository;
import com.spring.service.AreaService;
import com.spring.service.WaiterViewService;

import jakarta.servlet.http.HttpSession;

@Controller
public class WaiterController {

    @Autowired
    private AreaService areaService;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private WaiterViewService waiterViewService;

	/*
	 * @Autowired private SimpMessagingTemplate messagingTemplate;
	 */

    // --- ID Masking Helpers (Original Logic kept intact) ---
    private String encodeId(Integer id) {
        if (id == null) return "";
        return Base64.getEncoder().encodeToString(id.toString().getBytes());
    }

    private Integer decodeId(String encodedId) {
        try {
            return Integer.parseInt(new String(Base64.getDecoder().decode(encodedId)));
        } catch (Exception e) { return null; }
    }

    // ==========================================
    // 1. DASHBOARD & AREA SELECTION
    // ==========================================

    @GetMapping("/waiter/dashboard")
    public String showWaiterDashboard(HttpSession session, Model model) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if(loginUser == null || loginUser.getRoleId() != 2) return "redirect:/login";

        model.addAttribute("activeTablesCount", restaurantTableRepository.countByStatus("Occupied"));
        return "waiter-dashboard";
    }

    @GetMapping("/waiter/areas/{id}/tables")
    public String showTablesByArea(@PathVariable("id") String encodedAreaId, HttpSession session, Model model) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if(loginUser == null || loginUser.getRoleId() != 2) return "redirect:/login";

        Integer areaId = decodeId(encodedAreaId);
        Area currentArea = areaService.getAreaById(areaId);

        if(currentArea == null || !"Active".equalsIgnoreCase(currentArea.getStatus())) {
            return "redirect:/waiter/areas";
        }

        List<Map<String, Object>> tables = restaurantTableRepository.findByAreaAreaId(areaId).stream()
            .map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("tableNumber", t.getTable_number());
                map.put("status", t.getStatus());
                map.put("tableId", t.getRestaurant_table_id());
                map.put("maskedId", encodeId(t.getRestaurant_table_id()));
                return map;
            }).collect(Collectors.toList());

        model.addAttribute("tables", tables);
        model.addAttribute("areaName", currentArea.getAreaName());
        model.addAttribute("maskedAreaId", encodedAreaId);

        return "waiter-tables";
    }

    // ==========================================
    // 2. MENU SELECTION & AJAX SEARCH
    // ==========================================

    @GetMapping("/waiter/tables/{id}/menu")
    public String showTableMenu(
            @PathVariable("id") String encodedTableId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "category", defaultValue = "ALL") String category,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "ajax", defaultValue = "false") boolean isAjax,
            HttpSession session,
            Model model) {

        // 1. Validate User
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if(loginUser == null || loginUser.getRoleId() != 2) return "redirect:/login";

        // 2. Pagination Fix: Ensure page is never less than 1
        int currentPage = Math.max(1, page);
        int pageSize = 8;

        // 3. Fetch Data
        List<WaiterView> items = waiterViewService.getPaginatedItems(currentPage, pageSize, category, search);
        int totalItems = waiterViewService.getTotalItemCount(category, search);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // 4. Send UI context
        model.addAttribute("items", items);
        model.addAttribute("maskedTableId", encodedTableId);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentCat", category);
        model.addAttribute("searchQuery", search);

        // 5. AJAX Fragment return
        if (isAjax) {
            return "waiter_view :: menuSection"; // Ensure this matches th:fragment="menuSection" in HTML
        }

        return "waiter_view";
    }

    // ==========================================
    // 3. ACTIONS
    // ==========================================

    @PostMapping("/waiter/void-order")
    @ResponseBody
    public String voidOrder(@RequestParam("orderId") String orderId) {
        waiterViewService.voidOrder(Integer.parseInt(orderId));
        return "success";
    }

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @GetMapping("/waiter/tables/{id}/open")
    public String openOrder(@PathVariable("id") String encodedTableId, HttpSession session) {
        // 1. Security Check
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if(loginUser == null || loginUser.getRoleId() != 2) return "redirect:/login";

        Integer tableId = decodeId(encodedTableId);

        if (tableId != null) {
            // 2. Update Database via JdbcTemplate
            restaurantTableRepository.updateTableStatus(tableId, "Occupied");

            // 3. BROADCAST via WebSocket
            // This sends a message to everyone subscribed to /topic/table-updates
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", tableId);
            payload.put("status", "Occupied");
            payload.put("action", "UPDATE_STATUS");

            messagingTemplate.convertAndSend("/topic/table-updates", (Object) payload);
        }

        // 4. Redirect to menu
        return "redirect:/waiter/tables/" + encodedTableId + "/menu";
    }
}