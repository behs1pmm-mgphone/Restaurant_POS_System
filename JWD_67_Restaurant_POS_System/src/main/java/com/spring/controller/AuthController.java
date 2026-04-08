package com.spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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

        UserService.LoginResult loginResult = userService.login(userBean.getEmail(), userBean.getPassword());
        UserBean user = loginResult.getUser();

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
        } else if ("suspended".equals(loginResult.getStatus())) {
            model.addAttribute("error", "Your account is suspended. Please contact admin.");
            return "login";
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

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute("loginUser");
            session.invalidate();
        }
        return "redirect:/login";
    }
}