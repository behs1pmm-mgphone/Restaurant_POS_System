package com.spring.model;

import java.security.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Category {

	private Integer categoryId;
	private String categoryName;
	private Timestamp createdAt;
	private Integer createdBy;
	private boolean is_deleted;
	private Timestamp deletedAt;
	private Integer deletedBy;
	private Timestamp updatedAt;
	private Integer updatedBy;


}
