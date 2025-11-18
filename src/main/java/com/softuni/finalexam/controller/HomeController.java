package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class HomeController {

    private final ProductRepository productRepository;

    public HomeController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Get all products to display on home page
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "home";
    }

    // Cart is now handled by CartController

    @GetMapping("/wishlist-view")
    public String showWishlistView() {
        return "wishlist-view";
    }

    @GetMapping("/contacts")
    public String showContacts() {
        return "contacts";
    }

    @PostMapping("/contacts")
    public String handleContactForm(@RequestParam String name,
                                     @RequestParam String phone,
                                     @RequestParam String email,
                                     @RequestParam String message) {

        return "redirect:/contacts?success=true";
    }

    @GetMapping("/delivery")
    public String showDelivery() {
        return "delivery";
    }

}
