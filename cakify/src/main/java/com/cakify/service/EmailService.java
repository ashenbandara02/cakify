package com.cakify.service;

import com.cakify.entity.Order;
import com.cakify.entity.Bill;
import com.cakify.enums.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service class for handling email notifications
 * Sends order status updates and bill notifications to customers
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${cakify.email.from:noreply@cakify.com}")
    private String fromEmail;

    @Value("${cakify.email.fromName:Cakify}")
    private String fromName;

    /**
     * Send order confirmation email (async)
     */
    @Async
    public void sendOrderConfirmationEmail(Order order) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping order confirmation email.");
            return;
        }
        try {
            String subject = "Order Confirmation - Order #" + order.getOrderId();
            String body = buildOrderConfirmationEmail(order);
            sendHtmlEmail(order.getCustomerEmail(), subject, body);
        } catch (Exception e) {
            // Log error but don't fail the order creation
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send order status update email (async)
     */
    @Async
    public void sendOrderStatusUpdateEmail(Order order, OrderStatus newStatus) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping order status update email.");
            return;
        }
        try {
            String subject = "Order Update - " + formatStatus(newStatus) + " - Order #" + order.getOrderId();
            String body = buildOrderStatusUpdateEmail(order, newStatus);
            sendHtmlEmail(order.getCustomerEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send order status update email: " + e.getMessage());
        }
    }

    /**
     * Send bill generated email (async)
     */
    @Async
    public void sendBillGeneratedEmail(Bill bill, Order order) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping bill generated email.");
            return;
        }
        try {
            String subject = "Bill Generated - " + bill.getBillNumber();
            String body = buildBillGeneratedEmail(bill, order);
            sendHtmlEmail(bill.getCustomerEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send bill email: " + e.getMessage());
        }
    }

    /**
     * Send order cancellation email (async)
     */
    @Async
    public void sendOrderCancellationEmail(Order order) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping order cancellation email.");
            return;
        }
        try {
            String subject = "Order Cancelled - Order #" + order.getOrderId();
            String body = buildOrderCancellationEmail(order);
            sendHtmlEmail(order.getCustomerEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send order cancellation email: " + e.getMessage());
        }
    }

    /**
     * Send HTML email using MimeMessage
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML format
        
        mailSender.send(message);
    }

    /**
     * Send simple text email
     */
    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        
        mailSender.send(message);
    }

    // ==================== EMAIL TEMPLATES ====================

    /**
     * Build HTML email for order confirmation
     */
    private String buildOrderConfirmationEmail(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String orderDate = order.getOrderDate().format(formatter);
        String deliveryDate = order.getDeliveryDate() != null ? order.getDeliveryDate().format(formatter) : "TBD";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #ff6b9d; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                    .order-details { background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid #ff6b9d; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    .status-badge { display: inline-block; padding: 5px 10px; background-color: #ffa500; color: white; border-radius: 3px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎂 Order Confirmed!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        <p>Thank you for your order! We're excited to bake something special for you.</p>
                        
                        <div class="order-details">
                            <h3>Order Details</h3>
                            <p><strong>Order ID:</strong> #%s</p>
                            <p><strong>Order Date:</strong> %s</p>
                            <p><strong>Status:</strong> <span class="status-badge">%s</span></p>
                            <p><strong>Delivery Date:</strong> %s</p>
                            <p><strong>Total Amount:</strong> Rs. %.2f</p>
                            <p><strong>Delivery Address:</strong> %s</p>
                        </div>
                        
                        <h3>What's Next?</h3>
                        <ul>
                            <li>✅ Your order has been confirmed</li>
                            <li>🔄 We'll start preparing your order soon</li>
                            <li>📧 You'll receive email updates as your order progresses</li>
                            <li>💰 Payment will be collected on delivery (Cash on Delivery)</li>
                        </ul>
                        
                        <p>If you have any questions, please contact us at <strong>%s</strong></p>
                    </div>
                    <div class="footer">
                        <p>Cakify Bakery - Made with ❤️ by Mrs. Thushani Pigera</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                order.getCustomerName(),
                order.getOrderId(),
                orderDate,
                formatStatus(order.getStatus()),
                deliveryDate,
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                fromEmail
            );
    }

    /**
     * Build HTML email for order status update
     */
    private String buildOrderStatusUpdateEmail(Order order, OrderStatus newStatus) {
        String statusMessage = getStatusMessage(newStatus);
        String statusEmoji = getStatusEmoji(newStatus);
        String statusColor = getStatusColor(newStatus);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: %s; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                    .status-update { background-color: white; padding: 20px; margin: 15px 0; text-align: center; border: 3px solid %s; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s Order Status Update</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <div class="status-update">
                            <h2>Your Order #%s is Now:</h2>
                            <h1 style="color: %s; margin: 20px 0;">%s</h1>
                            <p style="font-size: 16px;">%s</p>
                        </div>
                        
                        <div style="background-color: white; padding: 15px; margin: 15px 0; border-left: 4px solid %s;">
                            <p><strong>Order ID:</strong> #%s</p>
                            <p><strong>Total Amount:</strong> Rs. %.2f</p>
                            <p><strong>Delivery Address:</strong> %s</p>
                        </div>
                        
                        <p>Thank you for choosing Cakify Bakery! 🎂</p>
                    </div>
                    <div class="footer">
                        <p>Cakify Bakery - Made with ❤️</p>
                        <p>Questions? Contact us at %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                statusColor, statusColor,
                statusEmoji,
                order.getCustomerName(),
                order.getOrderId(),
                statusColor, formatStatus(newStatus),
                statusMessage,
                statusColor,
                order.getOrderId(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                fromEmail
            );
    }

    /**
     * Build HTML email for bill generated
     */
    private String buildBillGeneratedEmail(Bill bill, Order order) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                    .bill-details { background-color: white; padding: 20px; margin: 15px 0; border: 2px solid #4CAF50; border-radius: 5px; }
                    .amount-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .total-row { font-size: 20px; font-weight: bold; color: #4CAF50; margin-top: 10px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🧾 Bill Generated</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        <p>Your bill has been generated for Order #%s</p>
                        
                        <div class="bill-details">
                            <h3>Bill Details</h3>
                            <p><strong>Bill Number:</strong> %s</p>
                            <p><strong>Order ID:</strong> #%s</p>
                            
                            <div style="margin-top: 20px;">
                                <div class="amount-row">
                                    <span>Subtotal:</span>
                                    <span>Rs. %.2f</span>
                                </div>
                                <div class="amount-row">
                                    <span>Tax:</span>
                                    <span>Rs. %.2f</span>
                                </div>
                                <div class="amount-row">
                                    <span>Delivery Charge:</span>
                                    <span>Rs. %.2f</span>
                                </div>
                                <div class="amount-row">
                                    <span>Discount:</span>
                                    <span>- Rs. %.2f</span>
                                </div>
                                <div class="amount-row total-row">
                                    <span>Total Amount:</span>
                                    <span>Rs. %.2f</span>
                                </div>
                            </div>
                            
                            <p style="margin-top: 20px; padding: 15px; background-color: #fff3cd; border-left: 4px solid #ffc107;">
                                <strong>💰 Payment Method:</strong> Cash on Delivery (COD)<br>
                                Please keep the exact amount ready when your order is delivered.
                            </p>
                        </div>
                        
                        <p>Thank you for your business!</p>
                    </div>
                    <div class="footer">
                        <p>Cakify Bakery</p>
                        <p>Contact: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                bill.getCustomerName(),
                order.getOrderId(),
                bill.getBillNumber(),
                order.getOrderId(),
                bill.getSubtotal(),
                bill.getTaxAmount(),
                bill.getDeliveryCharges(),
                bill.getDiscountAmount(),
                bill.getTotalAmount(),
                fromEmail
            );
    }

    /**
     * Build HTML email for order cancellation
     */
    private String buildOrderCancellationEmail(Order order) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #dc3545; color: white; padding: 20px; text-align: center; border-radius: 5px; }
                    .content { background-color: #f9f9f9; padding: 20px; margin-top: 20px; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Order Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        <p>Your order #%s has been cancelled.</p>
                        
                        <div style="background-color: white; padding: 20px; margin: 15px 0; border-left: 4px solid #dc3545;">
                            <p><strong>Order ID:</strong> #%s</p>
                            <p><strong>Amount:</strong> Rs. %.2f</p>
                        </div>
                        
                        <p>If you did not request this cancellation or have any questions, please contact us immediately at <strong>%s</strong></p>
                        
                        <p>We hope to serve you again soon! 🎂</p>
                    </div>
                    <div class="footer">
                        <p>Cakify Bakery</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                order.getCustomerName(),
                order.getOrderId(),
                order.getOrderId(),
                order.getTotalAmount(),
                fromEmail
            );
    }

    // ==================== HELPER METHODS ====================

    private String formatStatus(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case CONFIRMED -> "Confirmed";
            case IN_PROGRESS -> "In Progress";
            case READY -> "Ready for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }

    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Your order has been received and is waiting for confirmation.";
            case CONFIRMED -> "Great news! Your order has been confirmed and will be processed soon.";
            case IN_PROGRESS -> "We're currently preparing your delicious order!";
            case READY -> "Your order is ready! We'll deliver it to you soon.";
            case DELIVERED -> "Your order has been delivered successfully. Enjoy!";
            case CANCELLED -> "Your order has been cancelled.";
        };
    }

    private String getStatusEmoji(OrderStatus status) {
        return switch (status) {
            case PENDING -> "⏳";
            case CONFIRMED -> "✅";
            case IN_PROGRESS -> "👨‍🍳";
            case READY -> "📦";
            case DELIVERED -> "🎉";
            case CANCELLED -> "❌";
        };
    }

    private String getStatusColor(OrderStatus status) {
        return switch (status) {
            case PENDING -> "#ffa500";
            case CONFIRMED -> "#4CAF50";
            case IN_PROGRESS -> "#2196F3";
            case READY -> "#9C27B0";
            case DELIVERED -> "#4CAF50";
            case CANCELLED -> "#dc3545";
        };
    }

    // ==================== INQUIRY EMAIL METHODS ====================

    /**
     * Send inquiry auto-response email (async)
     * Sent immediately when customer submits inquiry
     * 
     * Design Pattern: Template Method Pattern
     * - Uses HTML email template structure
     */
    @Async
    public void sendInquiryAutoResponseEmail(String customerEmail, String customerName, Long inquiryId, String categoryName) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping inquiry auto-response email.");
            return;
        }
        try {
            String subject = "We Received Your Inquiry - Cakify #" + inquiryId;
            String body = buildInquiryAutoResponseEmail(customerName, inquiryId, categoryName);
            sendHtmlEmail(customerEmail, subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send inquiry auto-response email: " + e.getMessage());
        }
    }

    /**
     * Send inquiry reply email (async)
     * Sent when admin replies to customer inquiry
     */
    @Async
    public void sendInquiryReplyEmail(String customerEmail, String customerName, Long inquiryId, 
                                     String originalMessage, String replyMessage, List<String> attachmentUrls) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Skipping inquiry reply email.");
            return;
        }
        try {
            String subject = "Response to Your Inquiry - Cakify #" + inquiryId;
            String body = buildInquiryReplyEmail(customerName, inquiryId, originalMessage, replyMessage, attachmentUrls);
            sendHtmlEmail(customerEmail, subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send inquiry reply email: " + e.getMessage());
        }
    }

    /**
     * Build HTML email for inquiry auto-response
     */
    private String buildInquiryAutoResponseEmail(String customerName, Long inquiryId, String categoryName) {
        String categoryInfo = categoryName != null ? 
            "<p><strong>Category:</strong> " + categoryName + "</p>" : "";
            
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px; }
                    .content { background-color: #f9f9f9; padding: 25px; margin-top: 20px; border-radius: 10px; }
                    .inquiry-box { background-color: white; padding: 20px; margin: 15px 0; border-left: 4px solid #667eea; border-radius: 5px; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    .btn { display: inline-block; padding: 12px 30px; background-color: #667eea; color: white; text-decoration: none; border-radius: 5px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✉️ Thank You for Contacting Cakify!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>We have received your inquiry and wanted to let you know that we're on it! 🎂</p>
                        
                        <div class="inquiry-box">
                            <p><strong>Inquiry ID:</strong> #%d</p>
                            %s
                            <p><strong>Status:</strong> <span style="color: #ffa500;">⏳ Pending Review</span></p>
                        </div>
                        
                        <p><strong>What happens next?</strong></p>
                        <ul>
                            <li>Our team will review your inquiry carefully</li>
                            <li>You'll receive a detailed response within 24 hours</li>
                            <li>We'll answer all your questions about our delicious cakes!</li>
                        </ul>
                        
                        <p>In the meantime, feel free to browse our cake collection on our website.</p>
                        
                        <p style="margin-top: 25px;">Thank you for choosing Cakify! 🍰</p>
                    </div>
                    <div class="footer">
                        <p><strong>Cakify Bakery</strong></p>
                        <p>Your satisfaction is our priority!</p>
                        <p style="margin-top: 10px;">📧 %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                customerName,
                inquiryId,
                categoryInfo,
                fromEmail
            );
    }

    /**
     * Build HTML email for inquiry reply
     */
    private String buildInquiryReplyEmail(String customerName, Long inquiryId, String originalMessage, 
                                         String replyMessage, List<String> attachmentUrls) {
        // Build attachments section
        StringBuilder attachmentsHtml = new StringBuilder();
        if (attachmentUrls != null && !attachmentUrls.isEmpty()) {
            attachmentsHtml.append("<div style='margin-top: 20px;'>");
            attachmentsHtml.append("<p><strong>📎 Reference Attachments:</strong></p>");
            attachmentsHtml.append("<ul>");
            for (String url : attachmentUrls) {
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                attachmentsHtml.append("<li><a href='").append(url).append("'>").append(fileName).append("</a></li>");
            }
            attachmentsHtml.append("</ul>");
            attachmentsHtml.append("</div>");
        }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px; }
                    .content { background-color: #f9f9f9; padding: 25px; margin-top: 20px; border-radius: 10px; }
                    .message-box { background-color: white; padding: 20px; margin: 15px 0; border-left: 4px solid #4CAF50; border-radius: 5px; }
                    .original-message { background-color: #f5f5f5; padding: 15px; margin: 15px 0; border-left: 3px solid #999; border-radius: 5px; font-style: italic; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }
                    .status-badge { background-color: #4CAF50; color: white; padding: 5px 15px; border-radius: 20px; display: inline-block; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ We've Responded to Your Inquiry!</h1>
                    </div>
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Thank you for your patience! We have reviewed your inquiry and here's our response:</p>
                        
                        <div class="message-box">
                            <p><strong>Inquiry ID:</strong> #%d</p>
                            <p><strong>Status:</strong> <span class="status-badge">✅ Resolved</span></p>
                        </div>
                        
                        <div style="margin-top: 20px;">
                            <p><strong>📝 Your Original Inquiry:</strong></p>
                            <div class="original-message">
                                %s
                            </div>
                        </div>
                        
                        <div style="margin-top: 20px;">
                            <p><strong>💬 Our Response:</strong></p>
                            <div class="message-box">
                                %s
                            </div>
                        </div>
                        
                        %s
                        
                        <div style="margin-top: 30px; padding: 20px; background-color: #e8f5e9; border-radius: 5px;">
                            <p><strong>Need more help?</strong></p>
                            <p>If you have any additional questions or concerns, feel free to submit another inquiry. We're always here to help! 🎂</p>
                        </div>
                        
                        <p style="margin-top: 25px;">Thank you for choosing Cakify! We look forward to serving you delicious cakes! 🍰</p>
                    </div>
                    <div class="footer">
                        <p><strong>Cakify Bakery</strong></p>
                        <p>Baking happiness, one cake at a time!</p>
                        <p style="margin-top: 10px;">📧 %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                customerName,
                inquiryId,
                originalMessage,
                replyMessage,
                attachmentsHtml.toString(),
                fromEmail
            );
    }
}
