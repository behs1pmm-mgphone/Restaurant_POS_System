package com.spring.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.model.Category;
import com.spring.model.UserBean;
import com.spring.service.CategoryService;
import com.spring.service.MenuItemService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/menu")
public class MenuItemController {

    @Autowired
    private MenuItemService menuService;

    @Autowired
    private CategoryService categoryService;

    private final String UPLOAD_DIR = "C:/pos_images/";

    // --- NEW METHOD FOR POP-UP DATA (JSON) ---
    // This allows JavaScript to fetch item details without changing the URL
    @GetMapping("/edit-data/{id}")
    @ResponseBody
    public Map<String, Object> getMenuItemData(@PathVariable int id) {
        return menuService.getItemById(id);
    }

    @GetMapping
    public String showMenuList(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "search", required = false) String search,
            Model model, HttpSession session) {

        if (session.getAttribute("loginUser") == null) return "redirect:/login";

        // FETCH ALL 19 ITEMS: Use 1000 to ensure nothing is cut off
        List<Map<String, Object>> items = menuService.getPaginatedItems(categoryId, search, 1000, 0);

        model.addAttribute("items", items);
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "menu-list";
    }

    // --- ORIGINAL METHODS REMAIN UNCHANGED ---

    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<Category> categories = categoryService.getAllActiveCategories();
        model.addAttribute("categories", categories);
        return "add-menu";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("item", menuService.getItemById(id));
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        return "edit-menu";
    }

    @GetMapping("/delete/{id}")
    public String softDeleteItem(@PathVariable int id, HttpSession session) {
        UserBean user = (UserBean) session.getAttribute("loginUser");
        if (user != null) {
            menuService.removeRequestedItem(id, user.getUserId());
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/save")
    public String saveItem(@RequestParam Map<String, Object> payload,
                           @RequestParam("file") MultipartFile file,
                           HttpSession session) {
        UserBean user = (UserBean) session.getAttribute("loginUser");

        if (user != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                String imageName = file.getOriginalFilename();
                Path copyLocation = uploadPath.resolve(imageName);
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                menuService.createItem(payload, imageName, user.getUserId());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/update")
    public String updateItem(@RequestParam Map<String, Object> payload,
                             @RequestParam("file") MultipartFile file,
                             HttpSession session) {
        UserBean user = (UserBean) session.getAttribute("loginUser");

        if (user != null) {
            String imageName;

            if (!file.isEmpty()) {
                try {
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    imageName = file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(imageName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    imageName = (String) payload.get("current_image");
                }
            } else {
                imageName = (String) payload.get("current_image");
            }

            menuService.updateItem(payload, imageName, user.getUserId());
        }
        return "redirect:/admin/menu";
    }

    @GetMapping("/display")
    @ResponseBody
    public byte[] displayImage(@RequestParam("img") String fileName) {
        try {
            Path path = Paths.get(UPLOAD_DIR + fileName);
            return Files.readAllBytes(path);
        } catch (Exception e) {
            return null;
        }
    }
}