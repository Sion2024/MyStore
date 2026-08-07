package com.softuni.finalexam.service;

import com.softuni.finalexam.models.dto.CartItemDto;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {

    private static final String CART_SESSION_KEY = "cartItems";
    private final ProductRepository productRepository;

    public List<CartItemDto> getCartItems(HttpSession session) {
        Object cartItemsObj = session.getAttribute(CART_SESSION_KEY);
        if (cartItemsObj instanceof List) {
            return (List<CartItemDto>) cartItemsObj;
        }
        return new ArrayList<>();
    }

    public void addToCart(HttpSession session, UUID productId, int quantity) {
        // could cache this but probably not needed for now
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
        }

        List<CartItemDto> cartItems = getCartItems(session);
        Optional<CartItemDto> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemDto item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("Cannot add more items. Max available: " + product.getStock());
            }
            item.setQuantity(newQuantity);
        } else {
            cartItems.add(CartItemDto.builder()
                    .product(product)
                    .quantity(quantity)
                    .build());
        }

        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    public void removeFromCart(HttpSession session, UUID productId) {
        List<CartItemDto> cartItems = getCartItems(session);
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
        session.setAttribute(CART_SESSION_KEY, cartItems);
    }

    public void updateQuantity(HttpSession session, UUID productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(session, productId);
            return;
        }

        List<CartItemDto> cartItems = getCartItems(session);
        Optional<CartItemDto> itemOpt = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItemDto item = itemOpt.get();
            Product product = item.getProduct();
            if (quantity > product.getStock()) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStock());
            }
            item.setQuantity(quantity);
            session.setAttribute(CART_SESSION_KEY, cartItems);
        }
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public BigDecimal calculateSubtotal(HttpSession session) {
        List<CartItemDto> cartItems = getCartItems(session);
        return cartItems.stream()
                .map(CartItemDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems(HttpSession session) {
        List<CartItemDto> cartItems = getCartItems(session);
        return cartItems.stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }
}

