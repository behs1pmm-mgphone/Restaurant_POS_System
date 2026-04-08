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
            // === ၁။ Page နဲ့ Size parameter များ လက်ခံရန် ထည့်သွင်းခြင်း ===
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size, 
            Model model, HttpSession session) {

        if (session.getAttribute("loginUser") == null) return "redirect:/login";

        // === ၂။ Offset ကို တွက်ချက်ခြင်း (ဥပမာ- Page 1 ဆိုရင် ၁၀ ခု ကျော်ရမယ်) ===
        int offset = page * size;

        // === ၃။ Repository ကို ခေါ်တဲ့အခါ 1000 အစား variable များ ပြောင်းလဲအသုံးပြုခြင်း ===
     // Controller ထဲမှာ size ကို အများကြီးပေးထားလိုက်ပါ
        List<Map<String, Object>> items = menuService.getPaginatedItems(categoryId, search, 1000, 0);
        model.addAttribute("items", items);
        
        // === ၄။ UI မှာ စာမျက်နှာအရေအတွက် တွက်ချက်ပြသရန် လိုအပ်သည့် Data များ ပို့ပေးခြင်း ===
        long totalItems = menuService.getTotalCount(categoryId, search);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        model.addAttribute("items", items);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
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
                           HttpSession session,
                           Model model) {
        UserBean user = (UserBean) session.getAttribute("loginUser");

        if (user != null) {
            String imageName = "default.png"; 

            try {
                // ၁။ ပုံ သိမ်းဆည်းခြင်း Logic
                if (file != null && !file.isEmpty()) {
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                    
                    imageName = file.getOriginalFilename();
                    Path copyLocation = uploadPath.resolve(imageName);
                    Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
                }
                
                // ၂။ Service ကို ခေါ်ပြီး Database သို့ သိမ်းခြင်း
                // ဒီနေရာမှာ နာမည်တူနေရင် Service ကနေ RuntimeException ပစ်ပါလိမ့်မယ်
                menuService.createItem(payload, imageName, user.getUserId());
                
                // အောင်မြင်ရင် success message နဲ့ ပြန်သွားမယ်
                return "redirect:/admin/menu?success=Item added successfully";

            } catch (IOException e) { 
                e.printStackTrace();
                return "redirect:/admin/menu?error=File upload failed";
            } catch (RuntimeException e) {
                // ၃။ နာမည်တူခြင်း သို့မဟုတ် အခြား Business Logic Error များကို ဖမ်းယူခြင်း
                // Error message ကို URL parameter အဖြစ် ပို့ပေးလိုက်ပါတယ်
                return "redirect:/admin/menu?error=" + e.getMessage();
            }
        }
        
        return "redirect:/login";
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