package com.spring.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.model.WaiterView;
import com.spring.repository.WaiterViewRepository;

@Service
public class WaiterViewService {

    @Autowired
    private WaiterViewRepository repository;

    /**
     * Pagination ပါဝင်သော Menu Items များကို ဆွဲထုတ်ရန်
     */
    public List<WaiterView> getPaginatedItems(int page, int size, String category, String search) {
        // Validation: Page number should not be less than 1
        int currentPage = Math.max(1, page);
        return repository.findPaginated(currentPage, size, category, search);
    }

    /**
     * စုစုပေါင်း Item အရေအတွက်ကို ယူရန်
     */
    public int getTotalItemCount(String category, String search) {
        return repository.countItems(category, search);
    }

    /**
     * Total Pages ကို တိုက်ရိုက်တွက်ချက်ပေးသော Helper method
     */
    public int getTotalPages(int size, String category, String search) {
        int totalItems = getTotalItemCount(category, search);
        return (int) Math.ceil((double) totalItems / size);
    }

    /**
     * Stock အခြေအနေ ပြောင်းလဲရန်
     * status true ဖြစ်လျှင် "Available"၊ false ဖြစ်လျှင် "Unavailable" သို့မဟုတ် "Sold Out"
     */
    public void toggleStock(int id, boolean isAvailable) {
        String stockStatus = isAvailable ? "Available" : "Unavailable";
        repository.updateStockStatus(id, stockStatus);
    }

    /**
     * အော်ဒါများကို ပယ်ဖျက်ရန်
     */
    public void voidOrder(int orderId) {
        // အနာဂတ်တွင် OrderRepository ကိုသုံး၍ DB status ပြောင်းရန်
        System.out.println("Order ID " + orderId + " has been voided logic triggered.");
    }
}