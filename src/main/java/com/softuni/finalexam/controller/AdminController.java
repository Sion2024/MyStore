package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminController(OrderRepository orderRepository, UserRepository userRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
    }

    
    /**
     * Display all registered users (Admin only)
     */
    @GetMapping("/users")
    public String showAllUsers(HttpSession session, Model model) {
        // Check if user is admin
        Object userRoleObj = session.getAttribute("userRole");
        if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
            log.warn("Unauthorized access attempt to admin users");
            return "redirect:/";
        }
        
        List<User> users = userRepository.findAll();
        Object currentUserIdObj = session.getAttribute("userId");
        UUID currentUserId = currentUserIdObj != null ? UUID.fromString(currentUserIdObj.toString()) : null;
        
        model.addAttribute("users", users);
        model.addAttribute("currentUserId", currentUserId);
        log.info("Admin viewing all users. Total users: {}", users.size());
        return "admin/users";
    }
    
    /**
     * Delete a user (Admin only)
     * Admins cannot delete themselves
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Check if user is admin
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
                return "redirect:/";
            }
            
            // Check if trying to delete self
            Object currentUserIdObj = session.getAttribute("userId");
            if (currentUserIdObj != null && id.toString().equals(currentUserIdObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Не можете да изтриете собствения си профил.");
                log.warn("Admin attempted to delete their own account");
                return "redirect:/admin/users";
            }
            
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }
            
            User user = userOpt.get();
            
            // Delete all orders associated with this user first
            List<Order> userOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getUser() != null && order.getUser().getId().equals(id))
                    .toList();
            
            if (!userOrders.isEmpty()) {
                // Delete order items first, then orders
                for (Order order : userOrders) {
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    if (!orderItems.isEmpty()) {
                        orderItemRepository.deleteAll(orderItems);
                    }
                    orderRepository.delete(order);
                }
                log.info("Deleted {} orders and their items for user: {}", userOrders.size(), id);
            }
            
            // Now delete the user
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Потребителят е изтрит успешно!");
            log.info("User deleted successfully: {}", id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при изтриване на потребител: " + e.getMessage());
            log.error("Failed to delete user: {}", id, e);
            return "redirect:/admin/users";
        }
    }
    
    /**
     * Promote user to admin (Admin only)
     */
    @PostMapping("/users/{id}/promote")
    public String promoteUserToAdmin(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Check if user is admin
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
                return "redirect:/";
            }
            
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }
            
            User user = userOpt.get();
            if ("ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Потребителят вече е администратор.");
                return "redirect:/admin/users";
            }
            
            user.setRole("ADMIN");
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Потребителят е повишен до администратор!");
            log.info("User promoted to admin: {}", id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при повишаване на потребител: " + e.getMessage());
            log.error("Failed to promote user: {}", id, e);
            return "redirect:/admin/users";
        }
    }
    
    /**
     * Demote admin to user (Admin only)
     * Admins cannot demote themselves
     */
    @PostMapping("/users/{id}/demote")
    public String demoteAdminToUser(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Check if user is admin
            Object userRoleObj = session.getAttribute("userRole");
            if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Unauthorized access.");
                return "redirect:/";
            }
            
            // Check if trying to demote self
            Object currentUserIdObj = session.getAttribute("userId");
            if (currentUserIdObj != null && id.toString().equals(currentUserIdObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Не можете да понижите собствения си профил.");
                log.warn("Admin attempted to demote their own account");
                return "redirect:/admin/users";
            }
            
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }
            
            User user = userOpt.get();
            if (user.getRole() == null || !"ADMIN".equals(user.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е администратор.");
                return "redirect:/admin/users";
            }
            
            user.setRole("USER");
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Администраторът е понижен до обикновен потребител!");
            log.info("Admin demoted to user: {}", id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при понижаване на администратор: " + e.getMessage());
            log.error("Failed to demote admin: {}", id, e);
            return "redirect:/admin/users";
        }
    }
}

