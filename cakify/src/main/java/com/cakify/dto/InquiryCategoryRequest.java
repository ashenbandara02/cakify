package com.cakify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * InquiryCategoryRequest DTO - Request object for creating/updating inquiry categories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 50, message = "Category name must be less than 50 characters")
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @Size(max = 50, message = "Icon name must be less than 50 characters")
    private String icon;

    private Integer displayOrder = 0;

    private Boolean isActive = true;
}
