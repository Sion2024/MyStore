package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.ProductRepository;
import com.softuni.finalexam.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.scheduling.enabled=false"
})
@Transactional
class OrderControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private User adminUser;
    private Order testOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .role("USER")
                .build();
        testUser = userRepository.save(testUser);

        // Create admin user
        adminUser = User.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password("password")
                .role("ADMIN")
                .build();
        adminUser = userRepository.save(adminUser);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Product Description")
                .price(new BigDecimal("10.00"))
                .stock(100)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test order
        testOrder = Order.builder()
                .user(testUser)
                .date(OffsetDateTime.now(ZoneId.of("Europe/Sofia")))
                .total(100)
                .status(OrderStatus.APPROVED)
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void testGetOrders_AuthenticatedUser() throws Exception {
        // Given - user is authenticated (simulated via session)
        
        // When/Then
        mockMvc.perform(get("/orders")
                        .sessionAttr("userId", testUser.getId().toString())
                        .sessionAttr("userRole", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"));
    }

    @Test
    void testGetOrders_UnauthenticatedUser() throws Exception {
        // Given - no session attributes
        
        // When/Then
        mockMvc.perform(get("/orders"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testGetOrderDetails_AsOwner() throws Exception {
        // Given - user is the owner of the order
        
        // When/Then
        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                        .sessionAttr("userId", testUser.getId().toString())
                        .sessionAttr("userRole", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"));
    }

    @Test
    void testGetOrderDetails_AsAdmin() throws Exception {
        // Given - admin user accessing any order
        
        // When/Then
        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                        .sessionAttr("userId", adminUser.getId().toString())
                        .sessionAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(view().name("order-details"));
    }

    @Test
    void testGetOrderDetails_Unauthorized() throws Exception {
        // Given - different user trying to access another user's order
        User otherUser = User.builder()
                .name("Other User")
                .email("other@example.com")
                .password("password")
                .role("USER")
                .build();
        otherUser = userRepository.save(otherUser);
        
        // When/Then
        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                        .sessionAttr("userId", otherUser.getId().toString())
                        .sessionAttr("userRole", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));
    }

    @Test
    void testGetOrderDetails_Unauthenticated() throws Exception {
        // Given - no session
        
        // When/Then
        mockMvc.perform(get("/orders/{id}", testOrder.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testGetCheckout_AuthenticatedUser() throws Exception {
        // Given - user is authenticated with cart items
        List<CartItemDto> cartItems = new ArrayList<>();
        CartItemDto cartItem = CartItemDto.builder()
                .product(testProduct)
                .quantity(2)
                .build();
        cartItems.add(cartItem);
        
        // When/Then
        mockMvc.perform(get("/checkout")
                        .sessionAttr("userId", testUser.getId().toString())
                        .sessionAttr("userRole", "USER")
                        .sessionAttr("cartItems", cartItems))
                .andExpect(status().isOk())
                .andExpect(view().name("checkout"));
    }

    @Test
    void testGetCheckout_UnauthenticatedUser() throws Exception {
        // Given - no session
        
        // When/Then
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void testGetCheckout_EmptyCart() throws Exception {
        // Given - user is authenticated but cart is empty
        
        // When/Then
        mockMvc.perform(get("/checkout")
                        .sessionAttr("userId", testUser.getId().toString())
                        .sessionAttr("userRole", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart?error=Cart is empty"));
    }
}

