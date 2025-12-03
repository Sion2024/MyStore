package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.dto.CreateOrderDto;
import com.softuni.finalexam.models.dto.ShipOrderDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.service.CartService;
import com.softuni.finalexam.service.OrderService;
import com.softuni.finalexam.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/profile";
        }

        // admins can't checkout
        Object userRoleObj = session.getAttribute("userRole");
        if (userRoleObj != null && "ADMIN".equals(userRoleObj.toString())) {
            return "redirect:/admin/orders?error=Admins cannot create orders";
        }

        UUID userId = UUID.fromString(userIdObj.toString());
        User user = userService.getById(userId);
        if (user == null) {
            return "redirect:/profile";
        }

        List<CartItemDto> cartItems = cartService.getCartItems(session);
        BigDecimal subtotal = cartService.calculateSubtotal(session);

        if (cartItems.isEmpty()) {
            return "redirect:/cart?error=Cart is empty";
        }

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        if (!model.containsAttribute("createOrderDto")) {
            CreateOrderDto createOrderDto = new CreateOrderDto();
            createOrderDto.setFullName(user.getName());
            model.addAttribute("createOrderDto", createOrderDto);
        }
        return "checkout";
    }


    @PostMapping("/orders/create")
    public String createOrder(
            @Valid @ModelAttribute("createOrderDto") CreateOrderDto createOrderDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                try {
                    UUID userId = UUID.fromString(userIdObj.toString());
                    User user = userService.getById(userId);
                    if (user != null) {
                        List<CartItemDto> cartItems = cartService.getCartItems(session);
                        BigDecimal subtotal = cartService.calculateSubtotal(session);
                        model.addAttribute("user", user);
                        model.addAttribute("cartItems", cartItems);
                        model.addAttribute("subtotal", subtotal);
                    }
                } catch (Exception e) {
                    log.error("Error loading checkout data", e);
                }
            }
            model.addAttribute("createOrderDto", createOrderDto);
            return "checkout";
        }

        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to create an order.");
                return "redirect:/profile";
            }

            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj != null && "ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Admins cannot create orders. Please use a regular user account.");
                return "redirect:/admin/orders";
            }

            UUID userId = UUID.fromString(userIdObj.toString());
            User user = userService.getById(userId);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "User not found. Please login again.");
                return "redirect:/profile";
            }

            List<CartItemDto> cartItems = cartService.getCartItems(session);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Your cart is empty. Please add items before checkout.");
                return "redirect:/cart";
            }

            Order order = orderService.createOrder(user, cartItems, 
                    createOrderDto.getFullName(), 
                    createOrderDto.getAddress(), 
                    createOrderDto.getPhoneNumber(), 
                    createOrderDto.getCourier(), 
                    createOrderDto.getPaymentMethod());
            cartService.clearCart(session);

            redirectAttributes.addFlashAttribute("success", "Order created successfully! Order ID: " + order.getId());
            return "redirect:/orders?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create order: " + e.getMessage());
            log.error("Error creating order", e);
            return "redirect:/checkout?error=true";
        }
    }


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

        // Calculate totals for each order from order items
        Map<UUID, BigDecimal> orderTotals = new HashMap<>();
        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            BigDecimal total = orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderTotals.put(order.getId(), total);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderTotals", orderTotals);
        return "orders";
    }

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

        Object userRoleObj = session.getAttribute("userRole");
        boolean isAdmin = userRoleObj != null && "ADMIN".equals(userRoleObj.toString());
        boolean isOwner = order.getUser() != null && order.getUser().getId().equals(userId);

        if (!isAdmin && !isOwner) {
            return "redirect:/orders";
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        
        // Calculate total from order items (more accurate than stored order.total)
        BigDecimal calculatedTotal = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("calculatedTotal", calculatedTotal);
        if (!model.containsAttribute("shipOrderDto")) {
            model.addAttribute("shipOrderDto", new ShipOrderDto());
        }
        return "order-details";
    }


    @PatchMapping("/orders/{id}/ship")
    public String shipOrder(
            @PathVariable UUID id,
            @Valid @ModelAttribute("shipOrderDto") ShipOrderDto shipOrderDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                try {
                    UUID userId = UUID.fromString(userIdObj.toString());
                    Order order = orderRepository.findById(id).orElse(null);
                    if (order != null) {
                        Object userRoleObj = session.getAttribute("userRole");
                        boolean isAdmin = userRoleObj != null && "ADMIN".equals(userRoleObj.toString());
                        boolean isOwner = order.getUser() != null && order.getUser().getId().equals(userId);
                        
                        if (isAdmin || isOwner) {
                            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                            BigDecimal calculatedTotal = orderItems.stream()
                                    .map(OrderItem::getTotalPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                            model.addAttribute("order", order);
                            model.addAttribute("orderItems", orderItems);
                            model.addAttribute("isAdmin", isAdmin);
                            model.addAttribute("calculatedTotal", calculatedTotal);
                            model.addAttribute("shipOrderDto", shipOrderDto);
                            return "order-details";
                        }
                    }
                } catch (Exception e) {
                    log.error("Error loading order details", e);
                }
            }
            redirectAttributes.addFlashAttribute("error", "Invalid shipping data. Please check all fields.");
            return "redirect:/orders/" + id + "?error=true";
        }

        try {
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access. Only administrators can ship orders.");
                return "redirect:/orders";
            }

            orderService.shipOrder(id, shipOrderDto.getCourier(), shipOrderDto.getAddress(), shipOrderDto.getPaymentMethod());
            redirectAttributes.addFlashAttribute("success", "Order shipped successfully!");
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to ship order: " + e.getMessage());
            log.error("Error shipping order", e);
            return "redirect:/orders/" + id + "?error=true";
        }
    }

    @PatchMapping("/orders/{id}/deliver")
    public String deliverOrder(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object userRoleObj = session.getAttribute("userRole");
        if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
            redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
            return "redirect:/orders";
        }

        try {
            orderService.markAsDelivered(id);
            redirectAttributes.addFlashAttribute("success", "Order marked as delivered!");
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to mark order as delivered.");
            log.error("Error marking delivered", e);
            return "redirect:/orders/" + id + "?error=true";
        }
    }
}

