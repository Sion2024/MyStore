package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.exception.InsufficientStockException;
import com.softuni.finalexam.exception.OrderNotFoundException;
import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final NotificationClient notificationClient;

    @Transactional
    public Order createOrder(User user, List<CartItemDto> cartItems, String fullName, String address,
                            String phoneNumber, String courier, String paymentMethod) {
        // Validate user
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null when creating an order");
        }

        // Validate cart items
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty when creating an order");
        }

        // Validate order details
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (courier == null || courier.trim().isEmpty()) {
            throw new IllegalArgumentException("Courier is required");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }

        Order order = Order.builder()
                .user(user)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(totalAmount.intValue())
                .status(OrderStatus.APPROVED)
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItemDto cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            if (product == null) {
                throw new IllegalArgumentException("Product cannot be null in cart item");
            }
            
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Product quantity must be greater than zero");
            }
            
            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0) {
                throw new InsufficientStockException("Недостатъчна наличност за продукт: " + product.getName() + 
                        ". Наличност: " + product.getStock() + ", Заявено: " + cartItem.getQuantity());
            }
            product.setStock(newStock);
            productRepository.save(product);
            
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Send notification to admin about new order
        try {
            LocalDateTime orderDate = savedOrder.getDate() != null 
                    ? savedOrder.getDate().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now();
            
            notificationClient.notifyNewOrder(
                    savedOrder.getId(),
                    user.getId(),
                    fullName,
                    user.getEmail(),
                    orderDate,
                    totalAmount
            );
        } catch (Exception e) {
            // Log warning but don't fail order creation if notification fails
            log.warn("Failed to send new order notification for order: {}", savedOrder.getId(), e);
        }

        log.info("Order created: {} with {} items", savedOrder.getId(), cartItems.size());
        return savedOrder;
    }


    @Transactional
    public void shipOrder(UUID orderId, String courier, String address, String paymentMethod) {
        // Validate parameters
        if (courier == null || courier.trim().isEmpty()) {
            throw new IllegalArgumentException("Courier is required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Поръчка с ID " + orderId + " не е намерена"));

        if (order.getStatus() == OrderStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Order is already in transit");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot ship an already delivered order");
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        orderRepository.save(order);

        log.info("Order {} shipped via {} to {}", orderId, courier, address);
        // Notification service only sends welcome emails on user registration
        // Order shipped emails are not currently supported
    }

    @Transactional
    public void markAsDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Поръчка с ID " + orderId + " не е намерена"));

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }
}

