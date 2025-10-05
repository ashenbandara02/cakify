package com.cakify.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cakify.entity.Order;
import com.cakify.enums.OrderStatus;
import com.cakify.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Create new order
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            if (orders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        try {
            Optional<Order> order = orderService.getOrderById(id);
            if (order.isPresent()) {
                return new ResponseEntity<>(order.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
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

    // Update order status
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatus status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update entire order
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @Valid @RequestBody Order order) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete order
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
}