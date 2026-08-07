package com.softuni.finalexam.service;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.enums.UserRole;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.scheduling.enabled=false"
})
@Transactional
class OrderSchedulerServiceIntegrationTest {

    @Autowired
    private OrderSchedulerService orderSchedulerService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .role(UserRole.USER)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    void testMarkOrdersAsDeliveredCron_WithApprovedOrders() {
        // Given
        Order approvedOrder1 = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(100)
                .status(OrderStatus.APPROVED)
                .build();
        orderRepository.save(approvedOrder1);

        Order approvedOrder2 = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(200)
                .status(OrderStatus.APPROVED)
                .build();
        orderRepository.save(approvedOrder2);

        // When
        orderSchedulerService.markOrdersAsDeliveredCron();

        // Then
        List<Order> deliveredOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .toList();
        assertEquals(2, deliveredOrders.size());
        assertTrue(deliveredOrders.stream().allMatch(order -> order.getStatus() == OrderStatus.DELIVERED));
    }

    @Test
    void testMarkOrdersAsDeliveredCron_WithInTransitOrders() {
        // Given
        Order inTransitOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(150)
                .status(OrderStatus.IN_TRANSIT)
                .build();
        orderRepository.save(inTransitOrder);

        // When
        orderSchedulerService.markOrdersAsDeliveredCron();

        // Then
        Order updatedOrder = orderRepository.findById(inTransitOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    @Test
    void testMarkOrdersAsDeliveredCron_WithMixedStatuses() {
        // Given
        Order approvedOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(100)
                .status(OrderStatus.APPROVED)
                .build();
        orderRepository.save(approvedOrder);

        Order inTransitOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(200)
                .status(OrderStatus.IN_TRANSIT)
                .build();
        orderRepository.save(inTransitOrder);

        Order deliveredOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(300)
                .status(OrderStatus.DELIVERED)
                .build();
        orderRepository.save(deliveredOrder);

        // When
        orderSchedulerService.markOrdersAsDeliveredCron();

        // Then
        List<Order> allOrders = orderRepository.findAll();
        long deliveredCount = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();
        assertEquals(3, deliveredCount); // All orders should be DELIVERED now
    }

    @Test
    void testMarkOrdersAsDeliveredCron_NoOrdersToUpdate() {
        // Given - no orders with APPROVED or IN_TRANSIT status

        // When
        orderSchedulerService.markOrdersAsDeliveredCron();

        // Then - should not throw exception
        List<Order> allOrders = orderRepository.findAll();
        assertTrue(allOrders.isEmpty() || allOrders.stream()
                .noneMatch(order -> order.getStatus() == OrderStatus.APPROVED || order.getStatus() == OrderStatus.IN_TRANSIT));
    }

    @Test
    void testMarkOrdersAsDeliveredFixedRate_WithApprovedOrders() {
        // Given
        Order approvedOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(100)
                .status(OrderStatus.APPROVED)
                .build();
        orderRepository.save(approvedOrder);

        // When
        orderSchedulerService.markOrdersAsDeliveredFixedRate();

        // Then
        Order updatedOrder = orderRepository.findById(approvedOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }

    @Test
    void testMarkOrdersAsDeliveredFixedRate_WithInTransitOrders() {
        // Given
        Order inTransitOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(150)
                .status(OrderStatus.IN_TRANSIT)
                .build();
        orderRepository.save(inTransitOrder);

        // When
        orderSchedulerService.markOrdersAsDeliveredFixedRate();

        // Then
        Order updatedOrder = orderRepository.findById(inTransitOrder.getId()).orElseThrow();
        assertEquals(OrderStatus.DELIVERED, updatedOrder.getStatus());
    }
}

