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
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.model.UserBean;
import com.spring.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class UserController {

    @Autowired
    private UserService userService;

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
}