package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class UserController {

    private final UserService userService;
    private final OrderRepository orderRepository;

    public UserController(UserService userService, OrderRepository orderRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
    }

    /**
     * Display registration page
     */
    @GetMapping("/profile-add")
    public String showRegistrationPage() {
        return "profile-add";
    }

    /**
     * Handle user registration
     */
    @PostMapping("/profile/add")
    public String register(
            @RequestParam String firstName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "false") boolean newsletterEnabled,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.registerUser(firstName, email, password, newsletterEnabled);
            
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            log.info("User registered successfully: {}", email);
            return "redirect:/profile?registered=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.error("Registration failed for email: {}", email, e);
            return "redirect:/profile-add?error=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            log.error("Unexpected error during registration", e);
            return "redirect:/profile-add?error=true";
        }
    }

    /**
     * Display profile page if logged in, otherwise show login page
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, @RequestParam(required = false) Boolean registered) {
        Object userIdObj = session.getAttribute("userId");
        
        // If user is logged in, show profile page with purchase history
        if (userIdObj != null) {
            try {
                UUID userId = UUID.fromString(userIdObj.toString());
                User user = userService.getById(userId);
                
                if (user != null) {
                    // Get user orders, sorted by date (most recent first)
                    // Filter out any null orders and ensure we only process valid orders
                    List<Order> orders = orderRepository.findAll().stream()
                            .filter(order -> order != null && 
                                    order.getUser() != null && 
                                    order.getUser().getId() != null &&
                                    order.getUser().getId().equals(userId))
                            .sorted((o1, o2) -> {
                                if (o1 != null && o2 != null) {
                                    if (o1.getDate() != null && o2.getDate() != null) {
                                        return o2.getDate().compareTo(o1.getDate()); // Most recent first
                                    }
                                    if (o1.getDate() != null) return -1;
                                    if (o2.getDate() != null) return 1;
                                }
                                return 0;
                            })
                            .toList();
                    
                    // Calculate statistics with null-safe checks
                    long totalOrders = orders.size();
                    long inTransitOrders = orders.stream()
                            .filter(order -> order != null && 
                                    order.getStatus() != null && 
                                    "IN_TRANSIT".equals(order.getStatus().name()))
                            .count();
                    long deliveredOrders = orders.stream()
                            .filter(order -> order != null && 
                                    order.getStatus() != null && 
                                    "DELIVERED".equals(order.getStatus().name()))
                            .count();
                    
                    model.addAttribute("user", user);
                    model.addAttribute("orders", orders);
                    model.addAttribute("totalOrders", totalOrders);
                    model.addAttribute("inTransitOrders", inTransitOrders);
                    model.addAttribute("deliveredOrders", deliveredOrders);
                    log.info("Showing profile page for user: {}", userId);
                    return "profile-view"; // New template for logged-in users
                }
            } catch (Exception e) {
                log.error("Error loading user profile", e);
                // Fall through to show login page
            }
        }
        
        // If not logged in, show login page
        if (registered != null && registered) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        return "profile"; // Existing login template
    }

    /**
     * Handle user login
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String username, // Form uses "username" field name
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> userOpt = userService.authenticate(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                session.setAttribute("userId", user.getId().toString());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userName", user.getName());
                session.setAttribute("userRole", user.getRole());
                
                log.info("User logged in successfully: {}", username);
                return "redirect:/";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
                log.warn("Login failed for email: {}", username);
                return "redirect:/profile?error=true";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed. Please try again.");
            log.error("Unexpected error during login", e);
            return "redirect:/profile?error=true";
        }
    }

    /**
     * Handle user logout
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        log.info("User logged out");
        return "redirect:/";
    }

    /**
     * Logout GET endpoint (for convenience)
     */
    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        session.invalidate();
        log.info("User logged out");
        return "redirect:/";
    }
}

