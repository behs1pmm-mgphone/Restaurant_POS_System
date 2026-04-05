package com.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.model.UserBean;
import com.spring.repository.MenuItemRepository;
import com.spring.repository.RestaurantTableRepository;
import com.spring.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantTableRepository restaurantTableRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new UserBean());
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute("user") UserBean userBean,
                              BindingResult result,
                              HttpSession session,
                              Model model) {

        // Validates Email format and Password policy defined in UserBean
        if (result.hasErrors()) {
            return "login";
        }

        // Updated: Only passing email and password to the service for BCrypt verification
        UserBean user = userService.login(userBean.getEmail(), userBean.getPassword());

        if (user != null) {
            session.setAttribute("loginUser", user);
            Integer roleId = user.getRoleId();

            if (roleId == null) {
                model.addAttribute("error", "User role not assigned!");
                return "login";
            }

            // Standard workflow redirects
            switch (roleId) {
                case 1: // Admin
                    return "redirect:/admin/dashboard";
                case 2: // Waiter
                    return "redirect:/waiter/dashboard";
                case 3: // Cashier
                    return "redirect:/cashier/dashboard";
                case 4: // Kitchen
                    return "redirect:/kitchen/dashboard";
                default:
                    return "redirect:/home";
            }
        } else {
            model.addAttribute("error", "Invalid Email or Password!");
            return "login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getRoleId() != 1) {
            return "redirect:/login";
        }

        model.addAttribute("totalTables", restaurantTableRepository.countTables());
        model.addAttribute("totalMenuItems", menuItemRepository.countAllMenuItems());

        return "admin-dashboard";
    }

    @PostMapping("/admin/update-profile")
    public String updateAdminProfile(
            @RequestParam("userName") String userName,
            @RequestParam("email") String email,
            @RequestParam(required = false) String password,
            HttpSession session,
            Model model) {

        UserBean sessionUser = (UserBean) session.getAttribute("loginUser");
        if (sessionUser == null) return "redirect:/login";

        // --- MANUAL VALIDATION ---
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "redirect:/admin/dashboard?error=Invalid+Email";
        }

        if (password != null && !password.trim().isEmpty()) {
            boolean hasUppercase = !password.equals(password.toLowerCase());
            boolean hasNumber = password.matches(".*\\d.*");
            // Added special character check to match your earlier security rules
            boolean hasSpecial = password.matches(".*[!@#$%^&*()_+].*");

            if (password.length() < 8 || !hasUppercase || !hasNumber || !hasSpecial) {
                return "redirect:/admin/dashboard?error=Weak+Password";
            }
        }

        // --- PROCEED TO UPDATE ---
        UserBean existingUser = userService.getUserById(sessionUser.getUserId());

        if (existingUser != null) {
            existingUser.setUserName(userName);
            existingUser.setEmail(email);

            // Handle Password Change
            if (password != null && !password.trim().isEmpty()) {
                // Hash the password here before passing to updateUser
                // OR let updateUser handle it (if you modify the service)
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
                existingUser.setPassword(hashed);
            }

            // FIXED: Added the second argument (sessionUser.getUserId())
            // to tell the service WHO is making this update.
            userService.updateUser(existingUser, sessionUser.getUserId());

            // Refresh session with updated data
            session.setAttribute("loginUser", existingUser);
        }

        return "redirect:/admin/dashboard?success=Profile+Updated";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("loginUser");
            session.invalidate();
        }
        return "redirect:/login";
    }
}