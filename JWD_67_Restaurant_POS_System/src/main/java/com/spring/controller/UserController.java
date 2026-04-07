package com.spring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.model.UserBean;
import com.spring.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(Model model, HttpSession session) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/login";
        }

        List<UserBean> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
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
            userService.addUser(user, loginUser.getUserId());
        } else {
            // Fallback or error handling if session expired
            return "redirect:/login";
        }

        return "redirect:/admin/users";
    }

    // FIXED: Changed path from "/admin/users/update" to "/users/update"
    // because @RequestMapping("/admin") is already defined at the class level.
    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute("user") UserBean user, HttpSession session) {
        // 1. Get the logged-in Admin from the session
        UserBean loginUser = (UserBean) session.getAttribute("loginUser");

        if (loginUser != null) {
            // 2. Pass the user object AND the Admin's ID to the service
            userService.updateUser(user, loginUser.getUserId());
            return "redirect:/admin/users?success";
        }

        // If session expired, send them back to login
        return "redirect:/login";
    }
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}