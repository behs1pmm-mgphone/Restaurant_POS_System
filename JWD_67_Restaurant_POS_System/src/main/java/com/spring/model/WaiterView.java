package com.spring.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaiterView {
    private Integer id;
    private String name;
    private double price;
    private String category;
    private String image;
    private Integer stockQuantity;
}