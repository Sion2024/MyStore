package com.softuni.finalexam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


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



}
