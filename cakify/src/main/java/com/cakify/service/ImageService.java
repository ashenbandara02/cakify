package com.cakify.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageService {

    @Value("${file.upload.dir:uploads/products}")
    private String uploadDir;

    @Value("${file.upload.base-url:http://localhost:9090}")
    private String baseUrl;

    // Allowed image types
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    // Allowed extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp"
    );

    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private Path fileStorageLocation;

    /**
     * Initialize storage directory on application startup
     */
    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
            System.out.println("✅ Upload directory created at: " + this.fileStorageLocation);
        } catch (IOException ex) {
            System.err.println("❌ Could not create upload directory: " + ex.getMessage());
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    /**
     * Save product image to disk
     * @param file - MultipartFile from request
     * @param productId - Product ID for filename
     * @return Relative path to saved file (e.g., "products/product_1_123456.jpg")
     */
    public String saveProductImage(MultipartFile file, Long productId) {
        // Validate file
        String validationError = validateImageFile(file);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename(), productId);

        try {
            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(filename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("✅ Image saved: " + filename);

            // Return relative path for database storage
            return "products/" + filename;

        } catch (IOException ex) {
            System.err.println("❌ Failed to save image: " + ex.getMessage());
            throw new RuntimeException("Failed to store file: " + filename, ex);
        }
    }

    /**
     * Delete product image from disk
     * @param imageUrl - Relative path from database (e.g., "products/product_1_123.jpg")
     */
    public void deleteProductImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from path
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                System.out.println("✅ Deleted image: " + filename);
            } else {
                System.out.println("⚠️ Image not found (may already be deleted): " + filename);
            }

        } catch (IOException ex) {
            System.err.println("❌ Failed to delete file: " + imageUrl + " - " + ex.getMessage());
            // Don't throw exception - file might already be deleted
        }
    }

    /**
     * Load image as resource for serving to frontend
     * @param filename - Image filename (e.g., "product_1_123456.jpg")
     * @return Resource object for response
     */
    public Resource loadImageAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + filename, ex);
        }
    }

    /**
     * Validate image file
     * @return null if valid, error message if invalid
     */
    private String validateImageFile(MultipartFile file) {
        // Check if file is empty
        if (file == null || file.isEmpty()) {
            return "Cannot upload empty file";
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            double sizeMB = file.getSize() / (1024.0 * 1024.0);
            return String.format("File size %.2f MB exceeds maximum limit of 5MB", sizeMB);
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return "Invalid file type. Allowed types: JPEG, PNG, WebP";
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "Invalid filename - no extension found";
        }

        String extension = originalFilename
                .substring(originalFilename.lastIndexOf("."))
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return "Invalid file extension. Allowed: .jpg, .jpeg, .png, .webp";
        }

        return null; // Valid file
    }

    /**
     * Generate unique filename
     * Format: product_{productId}_{timestamp}.{extension}
     * Example: product_5_1729267890123.jpg
     */
    private String generateUniqueFilename(String originalFilename, Long productId) {
        String cleanFilename = StringUtils.cleanPath(originalFilename);
        String extension = cleanFilename.substring(cleanFilename.lastIndexOf("."));
        long timestamp = System.currentTimeMillis();

        return String.format("product_%d_%d%s", productId, timestamp, extension);
    }

    /**
     * Get full image URL for frontend
     * @param relativePath - Path stored in database
     * @return Full URL (e.g., "http://localhost:9090/api/images/product_1_123.jpg")
     */
    public String getImageUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Extract filename from path
        String filename = relativePath.substring(relativePath.lastIndexOf("/") + 1);
        return baseUrl + "/api/images/" + filename;
    }
}