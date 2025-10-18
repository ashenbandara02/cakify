package com.cakify.service;

import com.cakify.entity.Bill;
import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import com.cakify.enums.PaymentStatus;
import com.cakify.exception.OrderNotFoundException;
import com.cakify.exception.OrderValidationException;
import com.cakify.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillService {
    
    @Autowired
    private BillRepository billRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Generate bill for an order
     */
    @Transactional
    public Bill generateBillForOrder(Order order) {
        // Check if bill already exists for this order
        Optional<Bill> existingBill = billRepository.findByOrderId(order.getOrderId());
        if (existingBill.isPresent()) {
            return existingBill.get();
        }
        
        // Create new bill
        Bill bill = new Bill();
        bill.setOrderId(order.getOrderId());
        bill.setBillNumber(generateBillNumber());
        
        // Copy customer details from order
        bill.setCustomerName(order.getCustomerName());
        bill.setCustomerEmail(order.getCustomerEmail());
        bill.setCustomerPhone(order.getCustomerPhone());
        bill.setDeliveryAddress(order.getDeliveryAddress());
        
        // Calculate amounts
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal taxAmount = calculateTax(subtotal);
        BigDecimal deliveryCharges = calculateDeliveryCharges(order.getDeliveryAddress());
        BigDecimal discountAmount = BigDecimal.ZERO; // Can be enhanced later
        
        bill.setSubtotal(subtotal);
        bill.setTaxAmount(taxAmount);
        bill.setDeliveryCharges(deliveryCharges);
        bill.setDiscountAmount(discountAmount);
        
        // Calculate total
        bill.calculateTotal();
        
        // Set payment info
        bill.setPaymentMethod("CASH_ON_DELIVERY");
        bill.setPaymentStatus(PaymentStatus.UNPAID);
        bill.setBillingDate(LocalDateTime.now());
        
        // Save and return
        Bill savedBill = billRepository.save(bill);
        
        // ⭐ Send bill generated email
        try {
            emailService.sendBillGeneratedEmail(savedBill, order);
        } catch (Exception e) {
            System.err.println("Failed to send bill email: " + e.getMessage());
        }
        
        return savedBill;
    }
    
    /**
     * Get all bills
     */
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }
    
    /**
     * Get all bills with pagination
     */
    public Page<Bill> getAllBillsPaginated(Pageable pageable) {
        return billRepository.findAll(pageable);
    }
    
    /**
     * Get bill by ID
     */
    public Optional<Bill> getBillById(Long billId) {
        return billRepository.findById(billId);
    }
    
    /**
     * Get bill by order ID
     */
    public Optional<Bill> getBillByOrderId(Long orderId) {
        return billRepository.findByOrderId(orderId);
    }
    
    /**
     * Get bill by bill number
     */
    public Optional<Bill> getBillByBillNumber(String billNumber) {
        return billRepository.findByBillNumber(billNumber);
    }
    
    /**
     * Get bills by customer email
     */
    public List<Bill> getBillsByCustomerEmail(String email) {
        return billRepository.findByCustomerEmail(email);
    }
    
    /**
     * Get bills by payment status
     */
    public List<Bill> getBillsByPaymentStatus(PaymentStatus status) {
        return billRepository.findByPaymentStatus(status);
    }
    
    /**
     * Get bills by payment status (paginated)
     */
    public Page<Bill> getBillsByPaymentStatusPaginated(PaymentStatus status, Pageable pageable) {
        return billRepository.findByPaymentStatus(status, pageable);
    }
    
    /**
     * Search bills by customer name
     */
    public List<Bill> searchBillsByCustomerName(String name) {
        return billRepository.findByCustomerNameContainingIgnoreCase(name);
    }
    
    /**
     * Get bills by date range
     */
    public List<Bill> getBillsByDateRange(LocalDateTime start, LocalDateTime end) {
        return billRepository.findByBillingDateBetween(start, end);
    }
    
    /**
     * Get all unpaid bills
     */
    public List<Bill> getUnpaidBills() {
        return billRepository.findUnpaidBills();
    }
    
    /**
     * Mark bill as paid on delivery
     */
    @Transactional
    public Bill markAsPaidOnDelivery(Long billId, String collectedBy) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        if (!billOpt.isPresent()) {
            throw new OrderNotFoundException("Bill not found with ID: " + billId);
        }
        
        Bill bill = billOpt.get();
        bill.setPaymentStatus(PaymentStatus.PAID_ON_DELIVERY);
        bill.setPaymentCollectedDate(LocalDateTime.now());
        bill.setCollectedBy(collectedBy);
        
        return billRepository.save(bill);
    }
    
    /**
     * Update payment status
     */
    @Transactional
    public Bill updatePaymentStatus(Long billId, PaymentStatus newStatus) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        if (!billOpt.isPresent()) {
            throw new OrderNotFoundException("Bill not found with ID: " + billId);
        }
        
        Bill bill = billOpt.get();
        bill.setPaymentStatus(newStatus);
        
        // If marking as paid, record the date
        if (newStatus == PaymentStatus.PAID_ON_DELIVERY && bill.getPaymentCollectedDate() == null) {
            bill.setPaymentCollectedDate(LocalDateTime.now());
        }
        
        return billRepository.save(bill);
    }
    
    /**
     * Update bill
     */
    @Transactional
    public Bill updateBill(Long billId, Bill updatedBill) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        if (!billOpt.isPresent()) {
            throw new OrderNotFoundException("Bill not found with ID: " + billId);
        }
        
        Bill bill = billOpt.get();
        
        // Update fields
        bill.setCustomerName(updatedBill.getCustomerName());
        bill.setCustomerEmail(updatedBill.getCustomerEmail());
        bill.setCustomerPhone(updatedBill.getCustomerPhone());
        bill.setDeliveryAddress(updatedBill.getDeliveryAddress());
        bill.setSubtotal(updatedBill.getSubtotal());
        bill.setTaxAmount(updatedBill.getTaxAmount());
        bill.setDeliveryCharges(updatedBill.getDeliveryCharges());
        bill.setDiscountAmount(updatedBill.getDiscountAmount());
        bill.setNotes(updatedBill.getNotes());
        
        // Recalculate total
        bill.calculateTotal();
        
        return billRepository.save(bill);
    }
    
    /**
     * Delete bill
     */
    @Transactional
    public void deleteBill(Long billId) {
        if (!billRepository.existsById(billId)) {
            throw new OrderNotFoundException("Bill not found with ID: " + billId);
        }
        billRepository.deleteById(billId);
    }
    
    /**
     * Get total revenue for period (paid bills only)
     */
    public BigDecimal getTotalRevenue(LocalDateTime start, LocalDateTime end) {
        BigDecimal revenue = billRepository.getTotalRevenueByPeriod(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    /**
     * Get total unpaid amount
     */
    public BigDecimal getTotalUnpaidAmount() {
        BigDecimal total = billRepository.getTotalAmountByStatus(PaymentStatus.UNPAID);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    /**
     * Get bill statistics
     */
    public Map<String, Object> getBillStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Long unpaidCount = billRepository.countByPaymentStatus(PaymentStatus.UNPAID);
        Long paidCount = billRepository.countByPaymentStatus(PaymentStatus.PAID_ON_DELIVERY);
        
        BigDecimal unpaidAmount = getTotalUnpaidAmount();
        BigDecimal paidAmount = billRepository.getTotalAmountByStatus(PaymentStatus.PAID_ON_DELIVERY);
        
        stats.put("unpaidBillCount", unpaidCount);
        stats.put("paidBillCount", paidCount);
        stats.put("totalUnpaidAmount", unpaidAmount);
        stats.put("totalPaidAmount", paidAmount != null ? paidAmount : BigDecimal.ZERO);
        stats.put("totalBills", unpaidCount + paidCount);
        
        return stats;
    }
    
    /**
     * Get recent bills
     */
    public List<Bill> getRecentBills() {
        return billRepository.findTop10ByOrderByBillingDateDesc();
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Generate unique bill number
     */
    private String generateBillNumber() {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // Find last bill for this month
        List<Bill> recentBills = billRepository.findTop10ByOrderByBillingDateDesc();
        
        int sequence = 1;
        String prefix = "BILL-" + yearMonth + "-";
        
        for (Bill bill : recentBills) {
            if (bill.getBillNumber().startsWith(prefix)) {
                String lastNumber = bill.getBillNumber().substring(prefix.length());
                try {
                    int lastSeq = Integer.parseInt(lastNumber);
                    sequence = lastSeq + 1;
                    break;
                } catch (NumberFormatException e) {
                    // Continue with default sequence
                }
            }
        }
        
        return String.format("BILL-%s-%05d", yearMonth, sequence);
    }
    
    /**
     * Calculate subtotal from order
     */
    private BigDecimal calculateSubtotal(Order order) {
        // If order has items, sum their totals
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            return order.getOrderItems().stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        // Otherwise use order total amount
        return order.getTotalAmount();
    }
    
    /**
     * Calculate tax (0% for now, can be enhanced)
     */
    private BigDecimal calculateTax(BigDecimal subtotal) {
        // No tax for now
        return BigDecimal.ZERO;
        
        // If you want 10% tax:
        // return subtotal.multiply(new BigDecimal("0.10"));
    }
    
    /**
     * Calculate delivery charges
     */
    private BigDecimal calculateDeliveryCharges(String deliveryAddress) {
        // Simple flat rate for now
        return new BigDecimal("500.00");
        
        // Can be enhanced with location-based pricing:
        // if (deliveryAddress.toLowerCase().contains("colombo")) {
        //     return new BigDecimal("500.00");
        // } else if (deliveryAddress.toLowerCase().contains("kandy")) {
        //     return new BigDecimal("800.00");
        // } else {
        //     return new BigDecimal("1500.00");
        // }
    }
    
    /**
     * Validate bill
     */
    private void validateBill(Bill bill) {
        if (bill.getTotalAmount() == null || bill.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderValidationException("totalAmount", "Total amount must be greater than zero");
        }
        
        if (bill.getCustomerEmail() == null || bill.getCustomerEmail().trim().isEmpty()) {
            throw new OrderValidationException("customerEmail", "Customer email is required");
        }
    }
}