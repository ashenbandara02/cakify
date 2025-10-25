package com.cakify.controller;

import com.cakify.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "http://localhost:8080")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Serve/Display image file
     * GET /api/images/{filename}
     * Example: GET /api/images/product_1_1729267890123.jpg
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable String filename,
            HttpServletRequest request) {

        try {
            // Load file as Resource
            Resource resource = imageService.loadImageAsResource(filename);

            // Determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext()
                        .getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                System.out.println("Could not determine file type for: " + filename);
            }

            // Fallback to default content type
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("❌ Error serving image: " + filename + " - " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}