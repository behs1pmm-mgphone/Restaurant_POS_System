package com.spring.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.model.UserBean;
import com.spring.service.AreaService;
import com.spring.service.OrderService;
import com.spring.service.OrderWorkflowService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/waiter")
public class WaiterOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AreaService areaService;

    @Autowired
    private OrderWorkflowService orderWorkflowService;


    @PostMapping("/tables/{id}/order")
    public String handleOrderSubmission(@PathVariable("id") String encodedId,
                                        HttpServletRequest request,
                                        HttpSession session) {

        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null) return "redirect:/login";

        int tableId = decodeId(encodedId);

        if (tableId == 0) {
            return "redirect:/waiter/areas?error=invalid_table";
        }

        int orderId = orderService.createNewOrder(tableId, loginUser.getUserId());

        Map<String, String[]> paramMap = request.getParameterMap();
        orderService.processAndSaveOrder(paramMap, orderId);

        return "redirect:/waiter/tables/" + encodedId + "/menu?success=true";
    }

       private int decodeId(String id) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(id);
            String decodedString = new String(decodedBytes);
            return Integer.parseInt(decodedString);
        } catch (Exception e) {
            System.err.println("Error decoding table ID: " + e.getMessage());
            return 0;
        }
    }


       @GetMapping("/tables/{id}/back")
       public String handleGoBack(@PathVariable("id") String encodedId) {
           int tableId = decodeId(encodedId);

           String checkOrderSql = "SELECT COUNT(*) FROM `order` WHERE restaurant_table_id = ? AND status = 'Pending'";

           // jdbcTemplate (j အသေး) ကို သုံးပြီး query လုပ်ပါ
           Integer activeOrders = jdbcTemplate.queryForObject(checkOrderSql, Integer.class, tableId);

           // ၂။ အကယ်၍ order မရှိပါက table status ကို Available ပြန်ပြောင်းပေးခြင်း
           if (activeOrders == null || activeOrders == 0) {
               jdbcTemplate.update("UPDATE restaurant_table SET status = 'Available' WHERE restaurant_table_id = ?", tableId);
           }

           return "redirect:/waiter/areas";
       }

       @GetMapping("/areas") // URL အပြည့်အစုံမှာ /waiter/areas ဖြစ်သွားပါမယ်
       public String showAreas(HttpSession session, Model model) {
           // ၁။ Auth စစ်ဆေးခြင်း
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if(loginUser == null || loginUser.getRoleId() != 2) return "redirect:/login";

           // ၂။ Table Cleanup Logic (မလိုအပ်ဘဲ Occupied ဖြစ်နေတာတွေကို ရှင်းမယ်)
           String cleanupSql = "UPDATE restaurant_table t " +
                               "LEFT JOIN `order` o ON t.restaurant_table_id = o.restaurant_table_id AND o.status = 'Pending' " +
                               "SET t.status = 'Available' " +
                               "WHERE o.order_id IS NULL AND t.status = 'Occupied'";
           jdbcTemplate.update(cleanupSql);

           // ၃။ Areas တွေကို ဆွဲထုတ်ပြီး Masked ID ပြောင်းခြင်း
           List<Map<String, Object>> allAreas = areaService.getAllAreas().stream()
               .map(area -> {
                   Map<String, Object> map = new HashMap<>();
                   map.put("areaName", area.getAreaName());
                   map.put("maskedId", encodeId(area.getAreaId())); // encodeId method ရှိနေဖို့ လိုပါမယ်
                   map.put("status", area.getStatus());
                   return map;
               }).collect(Collectors.toList());

           model.addAttribute("areas", allAreas);

           // ၄။ HTML ကို Return ပြန်ခြင်း
           return "waiter-areas";
       }

       @GetMapping("/my-orders")
       public String viewMyOrders(HttpSession session, Model model) {
           UserBean user = (UserBean) session.getAttribute("loginUser");
           if (user == null) return "redirect:/login";

           // GROUP_CONCAT ထဲမှာ Item အချက်အလက်တွေကို 'Name:Qty:Price' ပုံစံနဲ့ တွဲထုတ်ပါမယ်
           // ';' နဲ့ တစ်ခုချင်းစီကို ခွဲထားပါတယ်
           String sql = "SELECT o.order_id, o.order_date, o.status, o.order_type, t.table_number, " +
                        "(SELECT SUM(total) FROM order_item WHERE order_id = o.order_id) as total_amount, " +
                        "(SELECT GROUP_CONCAT(CONCAT(mi.name, ':', oi.quantity, ':', oi.unit_price) SEPARATOR '; ') " +
                        " FROM order_item oi JOIN menu_item mi ON oi.menu_item_id = mi.menu_item_id " +
                        " WHERE oi.order_id = o.order_id) as item_details " +
                        "FROM `order` o " +
                        "JOIN restaurant_table t ON o.restaurant_table_id = t.restaurant_table_id " +
                        "WHERE o.created_by = ? " +
                        "ORDER BY o.order_date DESC";

           List<Map<String, Object>> orders = jdbcTemplate.queryForList(sql, user.getUserId());
           model.addAttribute("myOrders", orders);

           return "my-orders";
       }

       @GetMapping("/order-items")
       @ResponseBody
       public List<Map<String, Object>> getOrderItemsBoard(HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return List.of();
           }
           return orderWorkflowService.getOrderItemsBoard();
       }

       @GetMapping("/my-orders/data")
       @ResponseBody
       public List<Map<String, Object>> getMyOrdersData(HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return List.of();
           }
           return orderWorkflowService.getWaiterOrdersWithItems(loginUser.getUserId());
       }

       @GetMapping("/menu-options")
       @ResponseBody
       public List<Map<String, Object>> getMenuOptions(HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return List.of();
           }
           return orderWorkflowService.getMenuOptions();
       }

       @PostMapping("/order-items/{id}/serve")
       @ResponseBody
       public Map<String, Object> serveOrderItem(@PathVariable("id") Integer orderItemId, HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return Map.of("success", false, "message", "Unauthorized");
           }
           boolean success = orderWorkflowService.updateItemStatusByRole(2, orderItemId, "Served");
           return Map.of(
                   "success", success,
                   "message", success ? "Item marked as Served." : "Item cannot be served yet."
           );
       }

       @PostMapping("/orders/{id}/items/add")
       @ResponseBody
       public Map<String, Object> addItemToOrder(@PathVariable("id") Integer orderId,
                                                 @RequestParam("menuItemId") Integer menuItemId,
                                                 @RequestParam("quantity") Integer quantity,
                                                 @RequestParam(name = "note", required = false) String note,
                                                 HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return Map.of("success", false, "message", "Unauthorized");
           }
           if (menuItemId == null || quantity == null || quantity <= 0) {
               return Map.of("success", false, "message", "Invalid item or quantity.");
           }
           boolean success = orderWorkflowService.addItemToWaiterOrder(
                   loginUser.getUserId(), orderId, menuItemId, quantity, note
           );
           return Map.of("success", success, "message", success ? "Item added to order." : "Cannot add item to this order.");
       }

       @PostMapping("/order-items/{id}/delete")
       @ResponseBody
       public Map<String, Object> deleteOrderItem(@PathVariable("id") Integer orderItemId, HttpSession session) {
           UserBean loginUser = (UserBean) session.getAttribute("loginUser");
           if (loginUser == null || loginUser.getRoleId() != 2) {
               return Map.of("success", false, "message", "Unauthorized");
           }
           boolean success = orderWorkflowService.deleteOrderItemByWaiter(loginUser.getUserId(), orderItemId);
           return Map.of("success", success, "message", success ? "Item removed from order." : "Cannot remove this item.");
       }

       private String encodeId(Integer id) {
           if (id == null) return "";
           return Base64.getEncoder().encodeToString(id.toString().getBytes());
       }
}