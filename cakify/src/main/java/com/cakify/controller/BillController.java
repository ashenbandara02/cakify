package com.cakify.controller;

import com.cakify.entity.Bill;
import com.cakify.enums.PaymentStatus;
import com.cakify.service.BillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "http://localhost:8080")
public class BillController {
    
    @Autowired
    private BillService billService;
    
    /**
     * Get all bills
     * GET /api/bills
     */
    @GetMapping
    public ResponseEntity<List<Bill>> getAllBills() {
        try {
            List<Bill> bills = billService.getAllBills();
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all bills with pagination
     * GET /api/bills/paginated?page=0&size=10
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Bill>> getAllBillsPaginated(Pageable pageable) {
        try {
            Page<Bill> bills = billService.getAllBillsPaginated(pageable);
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bill by ID
     * GET /api/bills/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        try {
            Optional<Bill> bill = billService.getBillById(id);
            return bill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bill by order ID
     * GET /api/bills/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Bill> getBillByOrderId(@PathVariable Long orderId) {
        try {
            Optional<Bill> bill = billService.getBillByOrderId(orderId);
            return bill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bill by bill number
     * GET /api/bills/number/{billNumber}
     */
    @GetMapping("/number/{billNumber}")
    public ResponseEntity<Bill> getBillByBillNumber(@PathVariable String billNumber) {
        try {
            Optional<Bill> bill = billService.getBillByBillNumber(billNumber);
            return bill.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bills by customer email
     * GET /api/bills/customer/{email}
     */
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Bill>> getBillsByCustomerEmail(@PathVariable String email) {
        try {
            List<Bill> bills = billService.getBillsByCustomerEmail(email);
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bills by payment status
     * GET /api/bills/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Bill>> getBillsByPaymentStatus(@PathVariable PaymentStatus status) {
        try {
            List<Bill> bills = billService.getBillsByPaymentStatus(status);
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bills by payment status (paginated)
     * GET /api/bills/status/{status}/paginated?page=0&size=10
     */
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<Page<Bill>> getBillsByPaymentStatusPaginated(
            @PathVariable PaymentStatus status, 
            Pageable pageable) {
        try {
            Page<Bill> bills = billService.getBillsByPaymentStatusPaginated(status, pageable);
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Search bills by customer name
     * GET /api/bills/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<Bill>> searchBillsByCustomerName(@RequestParam String name) {
        try {
            List<Bill> bills = billService.searchBillsByCustomerName(name);
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bills by date range
     * GET /api/bills/date-range?start=2025-10-01T00:00:00&end=2025-10-31T23:59:59
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<Bill>> getBillsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Bill> bills = billService.getBillsByDateRange(start, end);
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get all unpaid bills
     * GET /api/bills/unpaid
     */
    @GetMapping("/unpaid")
    public ResponseEntity<List<Bill>> getUnpaidBills() {
        try {
            List<Bill> bills = billService.getUnpaidBills();
            if (bills.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get recent bills
     * GET /api/bills/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Bill>> getRecentBills() {
        try {
            List<Bill> bills = billService.getRecentBills();
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Mark bill as paid on delivery
     * POST /api/bills/{id}/mark-paid
     * Body: { "collectedBy": "John Doe" }
     */
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<Bill> markAsPaidOnDelivery(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            String collectedBy = request.get("collectedBy");
            Bill updatedBill = billService.markAsPaidOnDelivery(id, collectedBy);
            return new ResponseEntity<>(updatedBill, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update payment status
     * PUT /api/bills/{id}/payment-status
     * Body: { "status": "PAID_ON_DELIVERY" }
     */
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<Bill> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        try {
            PaymentStatus newStatus = PaymentStatus.valueOf(request.get("status"));
            Bill updatedBill = billService.updatePaymentStatus(id, newStatus);
            return new ResponseEntity<>(updatedBill, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Update bill
     * PUT /api/bills/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable Long id, @Valid @RequestBody Bill bill) {
        try {
            Bill updatedBill = billService.updateBill(id, bill);
            return new ResponseEntity<>(updatedBill, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Delete bill
     * DELETE /api/bills/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteBill(@PathVariable Long id) {
        try {
            billService.deleteBill(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get total revenue for period
     * GET /api/bills/revenue?start=2025-10-01T00:00:00&end=2025-10-31T23:59:59
     */
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            BigDecimal revenue = billService.getTotalRevenue(start, end);
            return new ResponseEntity<>(revenue, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get total unpaid amount
     * GET /api/bills/unpaid-total
     */
    @GetMapping("/unpaid-total")
    public ResponseEntity<BigDecimal> getTotalUnpaidAmount() {
        try {
            BigDecimal total = billService.getTotalUnpaidAmount();
            return new ResponseEntity<>(total, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get bill statistics
     * GET /api/bills/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBillStatistics() {
        try {
            Map<String, Object> stats = billService.getBillStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}