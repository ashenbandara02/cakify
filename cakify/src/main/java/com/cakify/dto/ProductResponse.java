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
    private String category;
    private List<String> sizes;
    private Boolean availability;
    private Boolean featured;

    // Convert from Entity to Response DTO
    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(String.valueOf(product.getId()));
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setImage(product.getImageUrl() != null ? product.getImageUrl() : "/api/placeholder/400/400");
        response.setCategory(product.getCategory());
        response.setSizes(product.getSizeList());
        response.setAvailability(product.getAvailability().isAvailable());
        response.setFeatured(product.getFeatured());
        return response;
    }
}