package com.cakify.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Generic Page Response wrapper for paginated data
 * Wraps Spring Data Page object into a cleaner API response
 * 
 * Example response:
 * {
 *   "content": [ ... ],
 *   "currentPage": 0,
 *   "totalPages": 5,
 *   "totalElements": 100,
 *   "pageSize": 20,
 *   "hasNext": true,
 *   "hasPrevious": false
 * }
 */
public class PageResponse<T> {
    
    private List<T> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
    private boolean first;
    private boolean last;
    
    // Private constructor
    private PageResponse() {}
    
    /**
     * Static factory method to create PageResponse from Spring Data Page
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.content = page.getContent();
        response.currentPage = page.getNumber();
        response.totalPages = page.getTotalPages();
        response.totalElements = page.getTotalElements();
        response.pageSize = page.getSize();
        response.hasNext = page.hasNext();
        response.hasPrevious = page.hasPrevious();
        response.first = page.isFirst();
        response.last = page.isLast();
        return response;
    }
    
    // Getters and Setters
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    @Override
    public String toString() {
        return "PageResponse{" +
                "currentPage=" + currentPage +
                ", totalPages=" + totalPages +
                ", totalElements=" + totalElements +
                ", pageSize=" + pageSize +
                ", hasNext=" + hasNext +
                ", hasPrevious=" + hasPrevious +
                '}';
    }
}
