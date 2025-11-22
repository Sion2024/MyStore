package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Order;
import com.softuni.finalexam.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final OrderRepository orderRepository;

    public AdminController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Display admin order management panel
     */
    @GetMapping("/orders")
    public String showAdminOrders(HttpSession session, Model model) {
        // Check if user is admin
        /// //////
        Object userRoleObj = session.getAttribute("userRole");
        if (userRoleObj == null || !"ADMIN".equals(userRoleObj.toString())) {
            log.warn("Unauthorized access attempt to admin orders");
            return "redirect:/orders";
        }

        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }
}

