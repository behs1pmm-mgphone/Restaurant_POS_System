package com.spring.model;

import java.sql.Timestamp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserBean {

    private Integer userId;

    // Optional for login, but kept for registration/display
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one number")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
    private String password;

    // Database metadata fields preserved for your JDBC logic
    private Integer roleId;
    // 0 = active, 1 = suspended
    private Integer status;
    private Integer failedLoginAttempts;
    private Timestamp createdAt;
    private Integer createdBy;
    private String createdByName;
    private boolean isDeleted;
    private Timestamp deletedAt;
    private Integer deletedBy;
    private String deletedByName;
    private Integer updatedBy;
    private Timestamp updatedAt;
    private String updatedByName;
   
}