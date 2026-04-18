package com.spring.dto;

import java.util.List;

public class OrderEditRequest {
    private List<OrderItemEdit> items;

    public List<OrderItemEdit> getItems() {
        return items;
    }

    public void setItems(List<OrderItemEdit> items) {
        this.items = items;
    }

    public static class OrderItemEdit {
        private Integer orderItemId;
        private Integer menuItemId;
        private Integer quantity;
        private String note;
        private Double unitPrice;
        private Double total;

        public Integer getOrderItemId() {
            return orderItemId;
        }

        public void setOrderItemId(Integer orderItemId) {
            this.orderItemId = orderItemId;
        }

        public Integer getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(Integer menuItemId) {
            this.menuItemId = menuItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Double getTotal() {
            return total;
        }

        public void setTotal(Double total) {
            this.total = total;
        }
    }
}
