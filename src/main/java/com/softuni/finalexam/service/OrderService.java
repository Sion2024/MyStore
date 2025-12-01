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

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }


    @Transactional
    public Order createOrder(User user, List<CartItemDto> cartItems, String fullName, String address,
                            String phoneNumber, String courier, String paymentMethod) {
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .date(OffsetDateTime.now())
                .total(totalAmount.intValue())
                .status(OrderStatus.APPROVED)
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItemDto cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            int newStock = product.getStock() - cartItem.getQuantity();
            if (newStock < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
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

        log.info("Order created: {} with {} items", savedOrder.getId(), cartItems.size());
        return savedOrder;
    }


    @Transactional
    public void shipOrder(UUID orderId, String courier, String address, String paymentMethod) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.IN_TRANSIT);
        orderRepository.save(order);
    }

    @Transactional
    public void markAsDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }
}

