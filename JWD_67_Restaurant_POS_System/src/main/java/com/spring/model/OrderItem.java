package com.spring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int menuItemId;
    private String note;
    private int quantity;
    private double unitPrice;
    private double total;
    private String itemStatus;

}