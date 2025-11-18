package com.softuni.finalexam.controller;

import com.softuni.finalexam.models.entity.Category;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.CategoryRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product.");
            log.error("Failed to create product", e);
            return "redirect:/admin/products/create?error=true";
        }
    }

    /**
     * Display admin products list
     */
    @GetMapping("/admin/products")
    public String showAdminProducts(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    /**
     * Delete product (Admin only)
     */
    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
            log.info("Product deleted successfully: {}", id);
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product.");
            log.error("Failed to delete product: {}", id, e);
            return "redirect:/admin/products?error=true";
        }
    }
}

