package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final NotificationClient notificationClient;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, NotificationClient notificationClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.notificationClient = notificationClient;
    }

    /**
     * Create a new order with order items from cart and send confirmation email via notification service
     * 
     * @param user The user placing the order
     * @param cartItems List of cart items to convert to order items
     * @param fullName Full name for shipping
     * @param address Shipping address
     * @param phoneNumber Contact phone number
     * @param courier Delivery courier name
     * @param paymentMethod Payment method used
     * @return The created order
     */
    @Transactional
    public Order createOrder(User user, List<CartItemDto> cartItems, String fullName, String address,
                            String phoneNumber, String courier, String paymentMethod) {
        // Calculate total from cart items
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = Order.builder()
                .user(user)
                .date(OffsetDateTime.now())
                .total(totalAmount.intValue())
                .status(OrderStatus.APPROVED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items from cart items
        for (CartItemDto cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Update product stock
            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStock(newStock);
            productRepository.save(product);
            
            // Create order item
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Send order confirmation email via notification service
        notificationClient.sendOrderConfirmationEmail(
                user.getId(),
                fullName,
                address,
                phoneNumber,
                courier,
                paymentMethod
        );

        log.info("Order created successfully with ID: {} and {} items", savedOrder.getId(), cartItems.size());
        return savedOrder;
    }

    /**
     * Update order status to IN_TRANSIT and send shipped email via notification service
     * 
     * @param orderId The order ID
     * @param courier The courier name
     * @param address The shipping address
     * @param paymentMethod The payment method
     */
    @Transactional
    public void shipOrder(UUID orderId, String courier, String address, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.IN_TRANSIT);
        orderRepository.save(order);

        // Send order shipped email via notification service
        notificationClient.sendOrderShippedEmail(
                order.getUser().getId(),
                orderId,
                BigDecimal.valueOf(order.getTotal()),
                paymentMethod,
                courier,
                address
        );

        log.info("Order {} marked as shipped", orderId);
    }

    /**
     * Update order status to DELIVERED
     */
    @Transactional
    public void markAsDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        log.info("Order {} marked as delivered", orderId);
    }
}

