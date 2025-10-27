package com.cakify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.cakify.dto.ApiResponse;
import com.cakify.dto.OrderDTO;
import com.cakify.dto.PageResponse;
import com.cakify.entity.Order;
import com.cakify.enums.OrderStatus;
import com.cakify.exception.OrderNotFoundException;
import com.cakify.exception.OrderValidationException;
import com.cakify.exception.InvalidOrderException;
import com.cakify.mapper.OrderMapper;
import com.cakify.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderMapper orderMapper;

    // ============ CREATE ============
    
    /**
     * Create a new order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        try {
            // Convert DTO to Entity
            Order order = orderMapper.toEntity(orderDTO);
            
            // Create order through service
            Order createdOrder = orderService.createOrder(order);
            
            // Convert back to DTO for response
            OrderDTO responseDTO = orderMapper.toDTO(createdOrder);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDTO, "Order created successfully"));
                
        } catch (OrderValidationException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to create order: " + e.getMessage()));
        }
    }

    // ============ READ ALL (with Pagination) ============
    
    /**
     * Get all orders with pagination and sorting
     * GET /api/orders?page=0&size=20&sortBy=orderDate&sortDir=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("asc") 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<Order> orderPage = orderService.getAllOrdersPaginated(pageable);
            Page<OrderDTO> dtoPage = orderPage.map(orderMapper::toDTO);
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    PageResponse.of(dtoPage), 
                    "Orders retrieved successfully"
                )
            );
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to fetch orders: " + e.getMessage()));
        }
    }

    // ============ READ BY ID ============
    
    /**
     * Get a single order by ID
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        try {
            Optional<Order> order = orderService.getOrderById(id);
            if (order.isPresent()) {
                OrderDTO dto = orderMapper.toDTO(order.get());
                return ResponseEntity.ok(
                    ApiResponse.success(dto, "Order retrieved successfully")
                );
            } else {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("NOT_FOUND", "Order not found with ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to fetch order: " + e.getMessage()));
        }
    }

    // Get orders by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<Order> orders = orderService.getOrdersByStatus(status);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get orders by customer email
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Order>> getOrdersByCustomerEmail(@PathVariable String email) {
        try {
            List<Order> orders = orderService.getOrdersByCustomerEmail(email);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ UPDATE STATUS ============
    
    /**
     * Update order status only
     * PATCH /api/orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            OrderStatus status = OrderStatus.valueOf(request.get("status"));
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            OrderDTO dto = orderMapper.toDTO(updatedOrder);
            
            return ResponseEntity.ok(
                ApiResponse.success(dto, "Order status updated successfully")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("INVALID_STATUS", "Invalid order status provided"));
        } catch (OrderNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (InvalidOrderException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("BUSINESS_RULE_VIOLATION", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to update status: " + e.getMessage()));
        }
    }

    // ============ UPDATE FULL ORDER ============
    
    /**
     * Update entire order
     * PUT /api/orders/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable Long id, 
            @Valid @RequestBody OrderDTO orderDTO) {
        try {
            Order order = orderMapper.toEntity(orderDTO);
            Order updatedOrder = orderService.updateOrder(id, order);
            OrderDTO dto = orderMapper.toDTO(updatedOrder);
            
            return ResponseEntity.ok(
                ApiResponse.success(dto, "Order updated successfully")
            );
        } catch (OrderNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (OrderValidationException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to update order: " + e.getMessage()));
        }
    }

    // ============ DELETE ============
    
    /**
     * Delete an order
     * DELETE /api/orders/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(
                ApiResponse.success(null, "Order deleted successfully")
            );
        } catch (OrderNotFoundException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to delete order: " + e.getMessage()));
        }
    }

    // Get recent orders (for dashboard)
    @GetMapping("/recent")
    public ResponseEntity<List<Order>> getRecentOrders() {
        try {
            List<Order> orders = orderService.getRecentOrders();
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Search orders by customer name
    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrdersByCustomerName(@RequestParam String name) {
        try {
            List<Order> orders = orderService.searchOrdersByCustomerName(name);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get order count by status
    @GetMapping("/count/{status}")
    public ResponseEntity<Long> getOrderCountByStatus(@PathVariable OrderStatus status) {
        try {
            Long count = orderService.getOrderCountByStatus(status);
            return new ResponseEntity<>(count, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


/**
 * Get all orders with pagination
 * GET /api/orders/paginated?page=0&size=20&sort=orderDate,desc
 */
@GetMapping("/paginated")
public ResponseEntity<Page<Order>> getAllOrdersPaginated(Pageable pageable) {
    try {
        Page<Order> orders = orderService.getAllOrdersPaginated(pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    } catch (Exception e) {
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

/**
 * Get orders by status with pagination
 * GET /api/orders/status/{status}/paginated?page=0&size=10
 */
@GetMapping("/status/{status}/paginated")
public ResponseEntity<Page<Order>> getOrdersByStatusPaginated(
        @PathVariable OrderStatus status, 
        Pageable pageable) {
    try {
        Page<Order> orders = orderService.getOrdersByStatusPaginated(status, pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    } catch (Exception e) {
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

/**
 * Search orders with pagination
 * GET /api/orders/search/paginated?name=john&page=0&size=5&sort=orderDate,desc
 */
@GetMapping("/search/paginated")
public ResponseEntity<Page<Order>> searchOrdersByCustomerNamePaginated(
        @RequestParam String name, 
        Pageable pageable) {
    try {
        Page<Order> orders = orderService.searchOrdersByCustomerNamePaginated(name, pageable);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    } catch (Exception e) {
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
  
    // ============ STATISTICS ============
    
    /**
     * Get order statistics for dashboard
     * GET /api/orders/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalOrders", orderService.getAllOrders().size());
            stats.put("pendingCount", orderService.getOrderCountByStatus(OrderStatus.PENDING));
            stats.put("confirmedCount", orderService.getOrderCountByStatus(OrderStatus.CONFIRMED));
            stats.put("inProgressCount", orderService.getOrderCountByStatus(OrderStatus.IN_PROGRESS));
            stats.put("readyCount", orderService.getOrderCountByStatus(OrderStatus.READY));
            stats.put("deliveredCount", orderService.getOrderCountByStatus(OrderStatus.DELIVERED));
            stats.put("cancelledCount", orderService.getOrderCountByStatus(OrderStatus.CANCELLED));
            
            return ResponseEntity.ok(
                ApiResponse.success(stats, "Statistics retrieved successfully")
            );
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_ERROR", "Failed to fetch statistics: " + e.getMessage()));
        }
    }
}