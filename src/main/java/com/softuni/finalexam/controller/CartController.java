package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Display cart page
     */
    @GetMapping
    public String showCart(HttpSession session, Model model) {
        List<CartItemDto> cartItems = cartService.getCartItems(session);
        BigDecimal subtotal = cartService.calculateSubtotal(session);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartSubtotal", subtotal);
        return "cart";
    }

    /**
     * Add product to cart
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam UUID productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.addToCart(session, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Продуктът е добавен в количката!");
            log.info("Product {} added to cart with quantity {}", productId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.error("Failed to add product to cart: {}", productId, e);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при добавяне на продукт в количката.");
            log.error("Unexpected error adding product to cart", e);
        }

        return "redirect:/cart";
    }

    /**
     * Remove product from cart
     */
    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam UUID productId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.removeFromCart(session, productId);
            redirectAttributes.addFlashAttribute("success", "Продуктът е премахнат от количката!");
            log.info("Product {} removed from cart", productId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при премахване на продукт.");
            log.error("Failed to remove product from cart: {}", productId, e);
        }

        return "redirect:/cart";
    }

    /**
     * Update product quantity in cart
     */
    @PostMapping("/update")
    public String updateQuantity(
            @RequestParam UUID productId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.updateQuantity(session, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Количеството е актуализирано!");
            log.info("Product {} quantity updated to {}", productId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.error("Failed to update quantity: {}", productId, e);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Грешка при актуализиране на количество.");
            log.error("Unexpected error updating quantity", e);
        }

        return "redirect:/cart";
    }

    /**
     * Clear cart
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.clearCart(session);
        redirectAttributes.addFlashAttribute("success", "Количката е изчистена!");
        log.info("Cart cleared");
        return "redirect:/cart";
    }
}

