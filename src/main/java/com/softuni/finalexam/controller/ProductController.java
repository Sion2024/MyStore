package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.entity.Category;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.CategoryRepository;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Display all products
     */
    @GetMapping("/products")
    public String showProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            Model model) {

        List<Product> products;
        if (categoryId != null) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .toList();
        } else if (search != null && !search.isEmpty()) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getName() != null && 
                            (p.getName().toLowerCase().contains(search.toLowerCase()) ||
                             (p.getDescription() != null && p.getDescription().toLowerCase().contains(search.toLowerCase()))))
                    .toList();
        } else {
            products = productRepository.findAll();
        }

        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchQuery", search);
        
        return "products";
    }
    
    /**
     * Display create product form on products page (Admin only)
     */
    @GetMapping("/products/create")
    public String showCreateProductFormOnProductsPage(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/product-create";
    }
    
    /**
     * Create a new product from products page (Admin only)
     */
    @PostMapping("/products/create")
    public String createProductFromProductsPage(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {

        try {
            Category category = categoryId != null ? 
                    categoryRepository.findById(categoryId).orElse(null) : null;

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .category(category)
                    .imageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl : null)
                    .build();

            productRepository.save(product);
            
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            log.info("Product created successfully: {}", product.getId());
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product.");
            log.error("Failed to create product", e);
            return "redirect:/products/create?error=true";
        }
    }

    /**
     * Display product details
     */
    @GetMapping("/products/{id}")
    public String showProductDetails(@PathVariable UUID id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        
        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        return "product-details";
    }

    /**
     * Display create product form (Admin only)
     */
    @GetMapping("/admin/products/create")
    public String showCreateProductForm(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/product-create";
    }

    /**
     * Create a new product (Admin only)
     */
    @PostMapping("/admin/products")
    public String createProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(required = false) UUID categoryId,
            RedirectAttributes redirectAttributes) {

        try {
            Category category = categoryId != null ? 
                    categoryRepository.findById(categoryId).orElse(null) : null;

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .category(category)
                    .build();

            productRepository.save(product);
            
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            log.info("Product created successfully: {}", product.getId());
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product.");
            log.error("Failed to create product", e);
            return "redirect:/products/create?error=true";
        }
    }

    /**
     * Display admin products list with user order information (only APPROVED orders)
     */
    @GetMapping("/admin/orders")
    public String showAdminProducts(Model model) {
        List<Product> products = productRepository.findAll();
        
        // Create a map of product -> list of users who ordered it (only APPROVED orders)
        Map<UUID, List<User>> productUsersMap = new HashMap<>();
        // Create a map of product -> list of order IDs (only APPROVED orders)
        Map<UUID, List<UUID>> productOrderIdsMap = new HashMap<>();
        
        for (Product product : products) {
            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            
            // Filter only APPROVED orders
            Set<User> uniqueUsers = orderItems.stream()
                    .filter(item -> item.getOrder() != null 
                            && item.getOrder().getUser() != null
                            && item.getOrder().getStatus() == OrderStatus.APPROVED)
                    .map(item -> item.getOrder().getUser())
                    .collect(Collectors.toSet());
            
            List<UUID> orderIds = orderItems.stream()
                    .filter(item -> item.getOrder() != null 
                            && item.getOrder().getStatus() == OrderStatus.APPROVED)
                    .map(item -> item.getOrder().getId())
                    .distinct()
                    .collect(Collectors.toList());
            
            // Only add to maps if there are approved orders
            if (!uniqueUsers.isEmpty()) {
                productUsersMap.put(product.getId(), new ArrayList<>(uniqueUsers));
            }
            if (!orderIds.isEmpty()) {
                productOrderIdsMap.put(product.getId(), orderIds);
            }
        }
        
        // Filter products to only show those with approved orders
        List<Product> productsWithApprovedOrders = products.stream()
                .filter(product -> productOrderIdsMap.containsKey(product.getId()) 
                        && !productOrderIdsMap.get(product.getId()).isEmpty())
                .collect(Collectors.toList());
        
        model.addAttribute("products", productsWithApprovedOrders);
        model.addAttribute("productUsersMap", productUsersMap);
        model.addAttribute("productOrderIdsMap", productOrderIdsMap);
        return "admin/products";
    }

    /**
     * Delete product (Admin only)
     * Deletes all associated order items first to avoid foreign key constraint violations
     */
    @PostMapping("/admin/orders/{id}/delete")
    public String deleteProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/admin/orders?error=true";
            }
            
            // Delete all order items associated with this product first
            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            if (!orderItems.isEmpty()) {
                orderItemRepository.deleteAll(orderItems);
                log.info("Deleted {} order items for product: {}", orderItems.size(), id);
            }
            
            // Now delete the product
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
            log.info("Product deleted successfully: {}", id);
            return "redirect:/admin/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
            log.error("Failed to delete product: {}", id, e);
            return "redirect:/admin/orders?error=true";
        }
    }
}

