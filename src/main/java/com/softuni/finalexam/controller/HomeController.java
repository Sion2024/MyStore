package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;
    private final LocaleResolver localeResolver;



    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        
        // Get 5 random products for the carousel
        List<Product> randomProducts = products;
        if (!products.isEmpty()) {
            List<Product> shuffledProducts = new java.util.ArrayList<>(products);
            Collections.shuffle(shuffledProducts);
            // Take up to 5 random products
            randomProducts = shuffledProducts.stream()
                    .limit(5)
                    .collect(Collectors.toList());
        }
        model.addAttribute("randomProducts", randomProducts);
        
        return "home";
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
        // TODO: implement email sending for contact form
        return "redirect:/contacts?success=true";
    }

    @GetMapping("/delivery")
    public String showDelivery() {
        return "delivery";
    }

    @GetMapping("/change-language")
    public String changeLanguage(@RequestParam String lang, HttpServletRequest request, HttpServletResponse response) {
        Locale locale = new Locale(lang);
        localeResolver.setLocale(request, response, locale);
        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/";
    }

}
