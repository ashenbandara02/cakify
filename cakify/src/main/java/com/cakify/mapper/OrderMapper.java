package com.cakify.mapper;

import com.cakify.dto.OrderDTO;
import com.cakify.dto.OrderItemDTO;
import com.cakify.entity.Order;
import com.cakify.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper utility to convert between Order entities and DTOs
 * Prevents circular reference issues in JSON serialization
 * Provides clean separation between persistence and API layers
 */
@Component
public class OrderMapper {
    
    /**
     * Convert Order entity to OrderDTO
     * @param order The order entity
     * @return OrderDTO with all fields mapped
     */
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setProductId(order.getProductId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCustomerEmail(order.getCustomerEmail());
        dto.setCustomerPhone(order.getCustomerPhone());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setQuantity(order.getQuantity());
        dto.setStatus(order.getStatus());
        dto.setDeliveryDate(order.getDeliveryDate());
        dto.setOrderDate(order.getOrderDate());
        dto.setSpecialNotes(order.getSpecialNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Map order items if present
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            dto.setOrderItems(
                order.getOrderItems().stream()
                    .map(this::itemToDTO)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
    
    /**
     * Convert OrderDTO to Order entity
     * @param dto The order DTO
     * @return Order entity with all fields mapped
     */
    public Order toEntity(OrderDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Order order = new Order();
        
        // Don't set orderId for new orders (will be generated)
        if (dto.getOrderId() != null) {
            order.setOrderId(dto.getOrderId());
        }
        
        order.setUserId(dto.getUserId());
        order.setProductId(dto.getProductId());
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setQuantity(dto.getQuantity());
        order.setDeliveryDate(dto.getDeliveryDate());
        order.setSpecialNotes(dto.getSpecialNotes());
        
        // Status is set by service layer, but allow override for updates
        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
        }
        
        // Map order items if present
        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            dto.getOrderItems().forEach(itemDTO -> {
                OrderItem item = itemToEntity(itemDTO);
                order.addOrderItem(item); // Uses helper method from Order entity
            });
        }
        
        return order;
    }
    
    /**
     * Convert OrderItem entity to OrderItemDTO
     * @param item The order item entity
     * @return OrderItemDTO with all fields mapped
     */
    public OrderItemDTO itemToDTO(OrderItem item) {
        if (item == null) {
            return null;
        }
        
        OrderItemDTO dto = new OrderItemDTO();
        dto.setOrderItemId(item.getOrderItemId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setProductDescription(item.getProductDescription());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setSpecialInstructions(item.getSpecialInstructions());
        
        return dto;
    }
    
    /**
     * Convert OrderItemDTO to OrderItem entity
     * @param dto The order item DTO
     * @return OrderItem entity with all fields mapped
     */
    public OrderItem itemToEntity(OrderItemDTO dto) {
        if (dto == null) {
            return null;
        }
        
        OrderItem item = new OrderItem();
        
        // Don't set orderItemId for new items (will be generated)
        if (dto.getOrderItemId() != null) {
            item.setOrderItemId(dto.getOrderItemId());
        }
        
        item.setProductId(dto.getProductId());
        item.setProductName(dto.getProductName());
        item.setProductDescription(dto.getProductDescription());
        item.setQuantity(dto.getQuantity());
        item.setSpecialInstructions(dto.getSpecialInstructions());
        
        // Unit price and total price are calculated by service layer
        // but allow override for historical data
        if (dto.getUnitPrice() != null) {
            item.setUnitPrice(dto.getUnitPrice());
        }
        if (dto.getTotalPrice() != null) {
            item.setTotalPrice(dto.getTotalPrice());
        }
        
        return item;
    }
    
    /**
     * Update existing Order entity with data from DTO
     * Used for PUT/PATCH operations
     * @param existingOrder The existing order to update
     * @param dto The DTO with new data
     */
    public void updateEntityFromDTO(Order existingOrder, OrderDTO dto) {
        if (existingOrder == null || dto == null) {
            return;
        }
        
        // Update mutable fields only
        if (dto.getCustomerName() != null) {
            existingOrder.setCustomerName(dto.getCustomerName());
        }
        if (dto.getCustomerEmail() != null) {
            existingOrder.setCustomerEmail(dto.getCustomerEmail());
        }
        if (dto.getCustomerPhone() != null) {
            existingOrder.setCustomerPhone(dto.getCustomerPhone());
        }
        if (dto.getDeliveryAddress() != null) {
            existingOrder.setDeliveryAddress(dto.getDeliveryAddress());
        }
        if (dto.getDeliveryDate() != null) {
            existingOrder.setDeliveryDate(dto.getDeliveryDate());
        }
        if (dto.getSpecialNotes() != null) {
            existingOrder.setSpecialNotes(dto.getSpecialNotes());
        }
        if (dto.getQuantity() != null) {
            existingOrder.setQuantity(dto.getQuantity());
        }
        
        // Status updates should go through separate method for validation
        // Don't update totalAmount directly - recalculate from order items
    }
}
