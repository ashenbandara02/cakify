package com.cakify.repository;

import com.cakify.entity.Bill;
import com.cakify.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    // Find bill by order ID
    Optional<Bill> findByOrderId(Long orderId);
    
    // Find bill by bill number
    Optional<Bill> findByBillNumber(String billNumber);
    
    // Find bills by customer email
    List<Bill> findByCustomerEmail(String customerEmail);
    
    // Find bills by payment status
    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);
    
    // Find bills by payment status (paginated)
    Page<Bill> findByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);
    
    // Find bills by date range
    List<Bill> findByBillingDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Search bills by customer name (case insensitive)
    @Query("SELECT b FROM Bill b WHERE LOWER(b.customerName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Bill> findByCustomerNameContainingIgnoreCase(@Param("name") String name);
    
    // Get unpaid bills
    @Query("SELECT b FROM Bill b WHERE b.paymentStatus = 'UNPAID' ORDER BY b.billingDate ASC")
    List<Bill> findUnpaidBills();
    
    // Get total revenue by payment status
    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.paymentStatus = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") PaymentStatus status);
    
    // Get total revenue for date range (paid bills only)
    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.billingDate BETWEEN :start AND :end AND b.paymentStatus = 'PAID_ON_DELIVERY'")
    BigDecimal getTotalRevenueByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // Count bills by payment status
    Long countByPaymentStatus(PaymentStatus paymentStatus);
    
    // Get bills by customer and status
    List<Bill> findByCustomerEmailAndPaymentStatus(String customerEmail, PaymentStatus paymentStatus);
    
    // Get latest bills
    List<Bill> findTop10ByOrderByBillingDateDesc();
}