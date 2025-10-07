package com.cakify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String category;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availability = AvailabilityStatus.IN_STOCK;

    @Column(nullable = false)
    private Boolean featured = false;

    // Store sizes as comma-separated string for simplicity
    @Column(columnDefinition = "TEXT")
    private String sizes;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for sizes
    public List<String> getSizeList() {
        if (sizes == null || sizes.isEmpty()) {
            return List.of();
        }
        return List.of(sizes.split(","));
    }

    public void setSizeList(List<String> sizeList) {
        if (sizeList == null || sizeList.isEmpty()) {
            this.sizes = "";
        } else {
            this.sizes = String.join(",", sizeList);
        }
    }
}