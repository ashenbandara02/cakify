package com.cakify.dto;

import com.cakify.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String image;
    private Long categoryId;
    private String categoryName;
    private List<String> sizes;
    private Boolean availability;
    private Boolean featured;
    private Double averageRating;
    private Long reviewCount;

    // Convert from Entity to Response DTO
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(String.valueOf(product.getId()));
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImage(product.getImageUrl() != null ? product.getImageUrl() : "/api/placeholder/400/400");
        response.setCategoryId(product.getCategory().getId());
        response.setCategoryName(product.getCategory().getName());
        response.setSizes(product.getSizeList());
        response.setAvailability(product.getAvailability() != null ? product.getAvailability() : true);
        response.setFeatured(product.getFeatured() != null ? product.getFeatured() : false);
        response.setAverageRating(0.0);
        response.setReviewCount(0L);
        return response;
    }

    // Overloaded method to include ratings
    public static ProductResponse fromEntity(Product product, Double averageRating, Long reviewCount) {
        ProductResponse response = fromEntity(product);
        response.setAverageRating(averageRating != null ? averageRating : 0.0);
        response.setReviewCount(reviewCount != null ? reviewCount : 0L);
        return response;
    }
}