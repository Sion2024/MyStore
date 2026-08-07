package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.enums.UserRole;
import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.OrderRepository;
import com.softuni.finalexam.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;



    @GetMapping("/users")
    public String showAllUsers(HttpSession session, Model model) {
        List<User> users = userRepository.findAll();
        Object currentUserIdObj = session.getAttribute("userId");
        UUID currentUserId = currentUserIdObj != null ? UUID.fromString(currentUserIdObj.toString()) : null;

        model.addAttribute("users", users);
        model.addAttribute("currentUserId", currentUserId);
        return "admin/users";
    }

    @GetMapping("/orders")
    public String showAllOrders(Model model) {
        List<Order> orders = orderRepository.findAll().stream()
                .sorted((o1, o2) -> {
                    if (o1.getDate() != null && o2.getDate() != null) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                    return 0;
                })
                .toList();

        Map<UUID, BigDecimal> orderTotals = new HashMap<>();
        long approvedCount = 0;
        long inTransitCount = 0;
        long deliveredCount = 0;

        for (Order order : orders) {
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            BigDecimal total = orderItems.stream()
                    .map(OrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            orderTotals.put(order.getId(), total);

            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case APPROVED -> approvedCount++;
                    case IN_TRANSIT -> inTransitCount++;
                    case DELIVERED -> deliveredCount++;
                }
            }
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderTotals", orderTotals);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("inTransitCount", inTransitCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("totalOrders", orders.size());
        return "admin/orders";
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object currentUserIdObj = session.getAttribute("userId");
            if (currentUserIdObj != null && id.toString().equals(currentUserIdObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Не можете да изтриете собствения си профил.");
                return "redirect:/admin/users";
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }

            List<Order> userOrders = orderRepository.findAll().stream()
                    .filter(order -> order.getUser() != null && order.getUser().getId().equals(id))
                    .toList();

            for (Order order : userOrders) {
                List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                if (!orderItems.isEmpty()) {
                    orderItemRepository.deleteAll(orderItems);
                }
                orderRepository.delete(order);
            }

            userRepository.deleteById(id);
            log.info("Admin deleted user: {}", id);
            redirectAttributes.addFlashAttribute("success", "Потребителят е изтрит успешно!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при изтриване на потребител: " + e.getMessage());
            log.error("Error deleting user", e);
            return "redirect:/admin/users";
        }
    }

    @PatchMapping("/users/{id}/promote")
    public String promoteUserToAdmin(
            @PathVariable UUID id,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }

            User user = userOpt.get();
            if (user.getRole() == UserRole.ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Потребителят вече е администратор.");
                return "redirect:/admin/users";
            }

            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
            log.info("User promoted to admin: {}", id);
            redirectAttributes.addFlashAttribute("success", "Потребителят е повишен до администратор!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при повишаване на потребител: " + e.getMessage());
            log.error("Error promoting user", e);
            return "redirect:/admin/users";
        }
    }

    @PatchMapping("/users/{id}/demote")
    public String demoteAdminToUser(
            @PathVariable UUID id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Object currentUserIdObj = session.getAttribute("userId");
            if (currentUserIdObj != null && id.toString().equals(currentUserIdObj.toString())) {
                redirectAttributes.addFlashAttribute("error", "Не можете да понижите собствения си профил.");
                return "redirect:/admin/users";
            }

            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е намерен.");
                return "redirect:/admin/users";
            }

            User user = userOpt.get();
            if (user.getRole() != UserRole.ADMIN) {
                redirectAttributes.addFlashAttribute("error", "Потребителят не е администратор.");
                return "redirect:/admin/users";
            }

            user.setRole(UserRole.USER);
            userRepository.save(user);
            log.info("Admin demoted to user: {}", id);
            redirectAttributes.addFlashAttribute("success", "Администраторът е понижен до обикновен потребител!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при понижаване на администратор: " + e.getMessage());
            log.error("Error demoting admin", e);
            return "redirect:/admin/users";
        }
    }
}
