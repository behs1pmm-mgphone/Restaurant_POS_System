package com.spring.model;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantTable {
    private Integer restaurant_table_id;

    @NotBlank(message = "Table number is required")
    private String table_number;

    private String status = "Available"; // Default status
    private Integer area_id;
    private Integer reservation_id;
    private String areaName;
    private Integer is_deleted;
    private LocalDateTime deleted_at;
    private Integer deleted_by;
    private LocalDateTime created_at;
    private Integer created_by;
}