package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.service.CartService;
import com.softuni.finalexam.service.OrderService;
import com.softuni.finalexam.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final CartService cartService;

    public OrderController(OrderService orderService, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserService userService, CartService cartService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
        this.cartService = cartService;
    }

    /**
     * Display checkout page
     */
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/profile";
        }

        UUID userId = UUID.fromString(userIdObj.toString());
        User user = userService.getById(userId);
        
        if (user == null) {
            return "redirect:/profile";
        }

        // Get cart items and calculate total
        List<CartItemDto> cartItems = cartService.getCartItems(session);
        BigDecimal subtotal = cartService.calculateSubtotal(session);

        if (cartItems.isEmpty()) {
            return "redirect:/cart?error=Cart is empty";
        }

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        return "checkout";
    }

    /**
     * Handle order creation (checkout)
     */
    @PostMapping("/orders/create")
    public String createOrder(
            @RequestParam String fullName,
            @RequestParam String address,
            @RequestParam String phoneNumber,
            @RequestParam String courier,
            @RequestParam String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return "redirect:/profile";
            }

            UUID userId = UUID.fromString(userIdObj.toString());
            User user = userService.getById(userId);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found. Please login again.");
                return "redirect:/profile";
            }

            // Get cart items
            List<CartItemDto> cartItems = cartService.getCartItems(session);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty. Please add items before checkout.");
                return "redirect:/cart";
            }

            // Create order with cart items - this will automatically send confirmation email via notification service
            Order order = orderService.createOrder(
                    user,
                    cartItems,
                    fullName,
                    address,
                    phoneNumber,
                    courier,
                    paymentMethod
            );

            // Clear cart after successful order
            cartService.clearCart(session);

            redirectAttributes.addFlashAttribute("success", "Order created successfully! Order ID: " + order.getId());
            log.info("Order created successfully: {}", order.getId());
            return "redirect:/orders?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            log.error("Failed to create order", e);
            return "redirect:/checkout?error=true";
        }
    }

    /**
     * Display user orders
     */
    @GetMapping("/orders")
    public String showOrders(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/profile";
        }

        UUID userId = UUID.fromString(userIdObj.toString());
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getUser() != null && order.getUser().getId().equals(userId))
                .toList();

        model.addAttribute("orders", orders);
        return "orders";
    }

    /**
     * Display order details
     */
    @GetMapping("/orders/{id}")
    public String showOrderDetails(@PathVariable UUID id, HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/profile";
        }

        UUID userId = UUID.fromString(userIdObj.toString());
        Order order = orderRepository.findById(id).orElse(null);

        if (order == null) {
            return "redirect:/orders";
        }

        // Check if user is admin or order owner
        Object userRoleObj = session.getAttribute("userRole");
        boolean isAdmin = userRoleObj != null && "ADMIN".equals(userRoleObj.toString());
        boolean isOwner = order.getUser() != null && order.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            return "redirect:/orders";
        }

        // Get order items
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        return "order-details";
    }

    /**
     * Ship order (Admin functionality)
     * This will automatically send shipped email via notification service
     */
    @PostMapping("/orders/{id}/ship")
    public String shipOrder(
            @PathVariable UUID id,
            @RequestParam String courier,
            @RequestParam String address,
            @RequestParam String paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
                return "redirect:/orders";
            }

            orderService.shipOrder(id, courier, address, paymentMethod);
            
            redirectAttributes.addFlashAttribute("success", "Order shipped successfully!");
            log.info("Order {} shipped successfully", id);
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to ship order.");
            log.error("Failed to ship order: {}", id, e);
            return "redirect:/orders/" + id + "?error=true";
        }
    }

    /**
     * Mark order as delivered
     */
    @PostMapping("/orders/{id}/deliver")
    public String deliverOrder(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
                return "redirect:/orders";
            }

            orderService.markAsDelivered(id);
            
            redirectAttributes.addFlashAttribute("success", "Order marked as delivered!");
            log.info("Order {} marked as delivered", id);
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark order as delivered.");
            log.error("Failed to mark order as delivered: {}", id, e);
            return "redirect:/orders/" + id + "?error=true";
        }
    }
}

