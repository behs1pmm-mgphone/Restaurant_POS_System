package com.spring.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuItem {

  private Integer menu_item_id;
    private String name;
    private Double price;
    private Integer stock_quantity;
    private String status;
    private String image;
    private Integer category_id;



}