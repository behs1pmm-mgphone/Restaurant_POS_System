package com.spring.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	private 	Integer payment_id;
	private Integer order_id;
	private BigDecimal final_amount;
    private String payment_method;
    private LocalDateTime transaction_date;
	private String status = "Pending";

}
