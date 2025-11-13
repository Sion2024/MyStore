package com.softuni.finalexam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/cart")
    public String showCart() {
        return "cart";
    }

    @GetMapping("/profile-add")
    public String showProfileAdd() {
        return "profile-add";
    }

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
        // Handle form submission here
        // You can add email sending logic or save to database
        return "redirect:/contacts?success=true";
    }

}
