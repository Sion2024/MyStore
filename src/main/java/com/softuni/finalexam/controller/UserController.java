package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.dto.UpdateEmailDto;
import com.softuni.finalexam.models.dto.UserRegistrationDto;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class UserController {

    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public UserController(UserService userService, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }


    @GetMapping("/profile-add")
    public String showRegistrationPage() {
        return "profile-add";
    }

    @PostMapping("/profile/add")
    public String register(
            @Valid @ModelAttribute UserRegistrationDto registrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please fill in all required fields.");
            return "redirect:/profile-add?error=true";
        }

        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/profile?registered=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.error("Registration failed: {}", registrationDto.getEmail(), e);
            return "redirect:/profile-add?error=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed. Please try again.");
            log.error("Registration error", e);
            return "redirect:/profile-add?error=true";
        }
    }


    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model, @RequestParam(required = false) Boolean registered) {
        Object userIdObj = session.getAttribute("userId");
        
        if (userIdObj != null) {
            try {
                UUID userId = UUID.fromString(userIdObj.toString());
                User user = userService.getById(userId);
                
                if (user != null) {
                    // TODO: optimize this query - should use repository method instead of filtering all orders
                    List<Order> orders = orderRepository.findAll().stream()
                            .filter(order -> order.getUser() != null && order.getUser().getId().equals(userId))
                            .sorted((o1, o2) -> {
                                if (o1.getDate() != null && o2.getDate() != null) {
                                    return o2.getDate().compareTo(o1.getDate());
                                }
                                return 0;
                            })
                            .toList();
                    
                    long totalOrders = orders.size();
                    long inTransitOrders = orders.stream()
                            .filter(order -> order.getStatus() != null && order.getStatus().name().equals("IN_TRANSIT"))
                            .count();
                    long deliveredOrders = orders.stream()
                            .filter(order -> order.getStatus() != null && order.getStatus().name().equals("DELIVERED"))
                            .count();
                    
                    // Calculate totals for each order from order items
                    Map<UUID, BigDecimal> orderTotals = new HashMap<>();
                    for (Order order : orders) {
                        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                        BigDecimal total = orderItems.stream()
                                .map(OrderItem::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        orderTotals.put(order.getId(), total);
                    }
                    
                    model.addAttribute("user", user);
                    model.addAttribute("orders", orders);
                    model.addAttribute("orderTotals", orderTotals);
                    model.addAttribute("totalOrders", totalOrders);
                    model.addAttribute("inTransitOrders", inTransitOrders);
                    model.addAttribute("deliveredOrders", deliveredOrders);
                    return "profile-view";
                }
            } catch (Exception e) {
                log.error("Error loading profile", e);
            }
        }
        
        if (registered != null && registered) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        return "profile";
    }


    @PostMapping("/login")
    public String login(
            @RequestParam String username, // form field is named "username" but it's actually email
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userService.authenticate(username, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("userId", user.getId().toString());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userRole", user.getRole());
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password.");
            return "redirect:/profile?error=true";
        }
    }


    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            return "redirect:/profile";
        }

        try {
            UUID userId = UUID.fromString(userIdObj.toString());
            User user = userService.getById(userId);
            
            if (user == null) {
                return "redirect:/profile";
            }

            model.addAttribute("user", user);
            model.addAttribute("updateEmailDto", new UpdateEmailDto());
            return "profile-edit";
        } catch (Exception e) {
            log.error("Error loading edit profile", e);
            return "redirect:/profile";
        }
    }

    @PutMapping("/profile/email")
    public String updateEmail(
            @Valid @ModelAttribute UpdateEmailDto updateEmailDto,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Object userIdObj = session.getAttribute("userId");
        if (userIdObj == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to update your email.");
            return "redirect:/profile";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please provide a valid email address.");
            return "redirect:/profile/edit?error=true";
        }

        try {
            UUID userId = UUID.fromString(userIdObj.toString());
            User updatedUser = userService.updateEmail(userId, updateEmailDto.getEmail());
            
            // Update session email attribute
            session.setAttribute("userEmail", updatedUser.getEmail());
            
            redirectAttributes.addFlashAttribute("success", "Email updated successfully!");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.error("Email update failed for user: {}", userIdObj, e);
            return "redirect:/profile/edit?error=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update email. Please try again.");
            log.error("Error updating email", e);
            return "redirect:/profile/edit?error=true";
        }
    }
}

