package com.spring.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.model.UserBean;
import com.spring.repository.UserRepository;
import com.spring.service.OrderService;
import com.spring.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @GetMapping("/users")
    public String listUsers(Model model,
                            HttpSession session,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "8") int size,
                            @RequestParam(name = "q", required = false) String q) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }

        if (page < 1) page = 1;
        // Fixed server-side page size as requested.
        size = 8;

        int totalCount = userService.countActiveUsersByName(q);
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * size;
        List<UserBean> users = userService.getActiveUsersPageByName(q, offset, size);
        model.addAttribute("users", users);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("offset", offset);

        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);
        if (endPage - startPage < 4) {
            // try to keep a 5-page window when possible
            startPage = Math.max(1, endPage - 4);
            endPage = Math.min(totalPages, startPage + 4);
        }
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        UserBean userBean = new UserBean();
        userBean.setPassword(userService.getDefaultPassword());

        model.addAttribute("userBean", userBean);
        return "admin-users";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute("userBean") UserBean user, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");

        // Pass the Admin's ID as the second argument
        if (loginUser != null) {
            String error = userService.addUser(user, loginUser.getUserId());
            if (error != null) {
                return "redirect:/admin/users?error=" + error;
            }
        } else {
            // Fallback or error handling if session expired
            return "redirect:/login";
        }

        return "redirect:/admin/users?success=created";
    }

    // FIXED: Changed path from "/admin/users/update" to "/users/update"
    // because @RequestMapping("/admin") is already defined at the class level.
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute("user") UserBean user, HttpSession session) {
        // 1. Get the logged-in Admin from the session
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");

        if (loginUser != null) {
            // 2. Pass the user object AND the Admin's ID to the service
            String error = userService.updateUser(user, loginUser.getUserId());
            if (error != null) {
                return "redirect:/admin/users?error=" + error;
            }
            return "redirect:/admin/users?success=updated";
        }

        // If session expired, send them back to login
        return "redirect:/login";
    }
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") int id, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }
        userService.deleteUser(id, loginUser.getUserId());
        return "redirect:/admin/users";
    }

    @PostMapping("/users/status")
    public String updateUserStatus(@RequestParam("userId") int userId,
                                   @RequestParam("status") int status,
                                   HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        userService.updateUserStatus(userId, status, loginUser.getUserId());
        return "redirect:/admin/users";
    }

    @GetMapping("/order-management")
    public String viewOrderManagement(Model model, HttpSession session) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/login";
        }

        List<Map<String, Object>> settledOrders = orderService.getSettledOrders();
        model.addAttribute("settledOrders", settledOrders);
        
        return "admin-order-management";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, HttpSession session) {
        // Session ထဲက loginUser ကို ယူတယ်
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");

        // Login မဝင်ထားရင် login page ကို ပို့မယ်
        if (loginUser == null) {
            return "redirect:/login";
        }

        // "user" ဆိုတဲ့ နာမည်နဲ့ profile.html ဆီ ပို့ပေးလိုက်မယ်
        model.addAttribute("user", loginUser);

        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes ra) {

        // 1. Session Check
        UserBean sessionUser = (UserBean) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        // 2. Password Match Check
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/admin/profile";
        }

        // 3. Database ထဲက လက်ရှိ User Data ကို Repository ကနေ တိုက်ရိုက်ယူမယ်
        // (မှတ်ချက် - getUserById method သည်လည်း Repository ထဲမှာ ရှိနေရပါမည်)
        UserBean existingUser = userRepository.findById(sessionUser.getUserId());

        if (existingUser != null) {

            // 4. Old Password မှန်မမှန် BCrypt နဲ့ အရင်စစ်မယ်
            if (!BCrypt.checkpw(oldPassword, existingUser.getPassword())) {
                ra.addFlashAttribute("error", "Current password is incorrect!");
                return "redirect:/admin/profile";
            }

            // 5. Complexity Validation
            if (newPassword.length() < 8) {
                ra.addFlashAttribute("error", "Password must be at least 8 characters long!");
                return "redirect:/admin/profile";
            }
            if (!newPassword.matches(".*[A-Z].*")) {
                ra.addFlashAttribute("error", "Password must contain at least one uppercase letter!");
                return "redirect:/admin/profile";
            }
            if (!newPassword.matches(".*[0-9].*")) {
                ra.addFlashAttribute("error", "Password must contain at least one number!");
                return "redirect:/admin/profile";
            }
            if (!newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                ra.addFlashAttribute("error", "Password must contain at least one special character!");
                return "redirect:/admin/profile";
            }

            try {
                // 6. Password အသစ်ကို Hash လုပ်မယ်
                String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                // 7. Repository Method ကို တိုက်ရိုက်ခေါ်ပြီး Database မှာ သိမ်းမယ်
                int result = userRepository.updatePassword(sessionUser.getUserId(), hashedNewPassword);

                if (result > 0) {
                    // 8. Session Update လုပ်ပြီး Success ပြမယ်
                    existingUser.setPassword(hashedNewPassword);
                    session.setAttribute("loginUser", existingUser);
                    ra.addFlashAttribute("success", "Password changed successfully!");
                } else {
                    ra.addFlashAttribute("error", "Database update failed!");
                }

            } catch (Exception e) {
                ra.addFlashAttribute("error", "Error: " + e.getMessage());
            }

        } else {
            ra.addFlashAttribute("error", "User not found!");
        }

        return "redirect:/admin/profile";
    }
}