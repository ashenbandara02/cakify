package com.cakify.exception;

/**
 * Exception thrown when image upload/processing fails
 * HTTP Status: 400 BAD REQUEST
 * Covers scenarios like invalid file format, size limit exceeded, upload failure
 */
public class ImageUploadException extends ProductException {
    
    public ImageUploadException(String message) {
        super(message, "IMAGE_UPLOAD_ERROR");
    }
    
    public ImageUploadException(String message, Throwable cause) {
        super(message, "IMAGE_UPLOAD_ERROR", cause);
    }
}
