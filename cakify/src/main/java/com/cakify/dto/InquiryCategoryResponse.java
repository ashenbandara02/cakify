package com.cakify.dto;

import com.cakify.entity.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer inquiryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryCategoryResponse fromEntity(InquiryCategory category) {
        InquiryCategoryResponse response = new InquiryCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setIcon(category.getIcon());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setIsActive(category.getIsActive());
        response.setInquiryCount(category.getInquiryCount());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }

    public static InquiryCategoryResponse fromEntitySimple(InquiryCategory category) {
        InquiryCategoryResponse response = new InquiryCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setDisplayOrder(category.getDisplayOrder());
        return response;
    }
}
