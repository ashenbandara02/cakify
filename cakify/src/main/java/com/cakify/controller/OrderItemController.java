package com.cakify.controller;

import com.cakify.entity.OrderItem;
import com.cakify.service.OrderItemService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for OrderItem management
 * Provides API endpoints for order item operations
 */
@RestController
@RequestMapping("/api/order-items")
@CrossOrigin(origins = "*")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    /**
     * Add item to an existing order
     * POST /api/order-items/order/{orderId}
     */
    @PostMapping("/order/{orderId}")
    public ResponseEntity<OrderItem> addItemToOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderItem orderItem) {
        try {
            OrderItem savedItem = orderItemService.addItemToOrder(orderId, orderItem);
            return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all items for a specific order
     * GET /api/order-items/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getItemsByOrderId(@PathVariable Long orderId) {
        try {
            List<OrderItem> items = orderItemService.getItemsByOrderId(orderId);
            if (items.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get order item by ID
     * GET /api/order-items/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderItem> getOrderItemById(@PathVariable Long id) {
        try {
            Optional<OrderItem> item = orderItemService.getOrderItemById(id);
            if (item.isPresent()) {
                return new ResponseEntity<>(item.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update order item
     * PUT /api/order-items/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderItem> updateOrderItem(
            @PathVariable Long id, 
            @Valid @RequestBody OrderItem orderItem) {
        try {
            OrderItem updatedItem = orderItemService.updateOrderItem(id, orderItem);
            return new ResponseEntity<>(updatedItem, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete order item
     * DELETE /api/order-items/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        try {
            orderItemService.deleteOrderItem(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get items by product ID
     * GET /api/order-items/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<OrderItem>> getItemsByProductId(@PathVariable Long productId) {
        try {
            List<OrderItem> items = orderItemService.getItemsByProductId(productId);
            if (items.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search items by product name
     * GET /api/order-items/search?productName={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<OrderItem>> searchItemsByProductName(
            @RequestParam String productName) {
        try {
            List<OrderItem> items = orderItemService.searchItemsByProductName(productName);
            if (items.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(items, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get total quantity sold for a product
     * GET /api/order-items/product/{productId}/total-sold
     */
    @GetMapping("/product/{productId}/total-sold")
    public ResponseEntity<Integer> getTotalQuantitySoldByProduct(@PathVariable Long productId) {
        try {
            Integer totalSold = orderItemService.getTotalQuantitySoldByProduct(productId);
            return new ResponseEntity<>(totalSold, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}