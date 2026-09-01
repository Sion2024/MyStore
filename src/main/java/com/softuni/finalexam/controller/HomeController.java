package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.ProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductRepository productRepository;
    private final LocaleResolver localeResolver;

    @Value("${app.home.carousel-size:5}")
    private int carouselSize;

    @Value("${app.home.products-size:8}")
    private int homeProductsSize;

    @GetMapping("/")
    public String home(Model model) {
        List<Product> allProducts = new ArrayList<>(productRepository.findAll());
        Collections.shuffle(allProducts);

        List<Product> randomProducts = allProducts.stream()
                .limit(carouselSize)
                .collect(Collectors.toList());

        List<Product> products = allProducts.stream()
                .skip(carouselSize)
                .limit(homeProductsSize)
                .collect(Collectors.toList());

        model.addAttribute("products", products);
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
