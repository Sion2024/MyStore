package com.softuni.finalexam.controller;

import com.softuni.finalexam.enums.OrderStatus;
import com.softuni.finalexam.models.dto.CreateProductDto;
import com.softuni.finalexam.models.entity.Category;
import com.softuni.finalexam.models.entity.OrderItem;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.CategoryRepository;
import com.softuni.finalexam.repository.OrderItemRepository;
import com.softuni.finalexam.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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


    @GetMapping("/products")
    public String showProducts(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean wines,
            @RequestParam(required = false) Boolean champagne,
            @RequestParam(required = false) Boolean whiskey,
            @RequestParam(required = false) Boolean beer,
            @RequestParam(required = false) Boolean water,
            @RequestParam(required = false) Boolean softDrinks,
            Model model) {

        List<Product> products;
        if (wines != null && wines) {
            // Filter for Red Wine and White Wine categories
            products = filterProductsByCategoryNames("Red Wine", "White Wine");
        } else if (champagne != null && champagne) {
            // Filter for Champagne category
            products = filterProductsByCategoryNames("Champagne");
        } else if (whiskey != null && whiskey) {
            // Filter for Whiskey category
            products = filterProductsByCategoryNames("Whiskey");
        } else if (beer != null && beer) {
            // Filter for Beer category
            products = filterProductsByCategoryNames("Beer");
        } else if (water != null && water) {
            // Filter for Water category
            products = filterProductsByCategoryNames("Water");
        } else if (softDrinks != null && softDrinks) {
            // Filter for Soft Drink category
            products = filterProductsByCategoryNames("Soft Drink");
        } else if (categoryId != null) {
            products = productRepository.findAll().stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .toList();
        } else if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            products = productRepository.findAll().stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(searchLower)) ||
                                 (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
        } else {
            products = productRepository.findAll();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("searchQuery", search);
        model.addAttribute("winesFilter", wines != null && wines);
        model.addAttribute("champagneFilter", champagne != null && champagne);
        model.addAttribute("whiskeyFilter", whiskey != null && whiskey);
        model.addAttribute("beerFilter", beer != null && beer);
        model.addAttribute("waterFilter", water != null && water);
        model.addAttribute("softDrinksFilter", softDrinks != null && softDrinks);
        
        return "products";
    }

    private List<Product> filterProductsByCategoryNames(String... categoryNames) {
        List<com.softuni.finalexam.models.entity.Category> categories = categoryRepository.findAll().stream()
                .filter(c -> c != null && c.getName() != null)
                .filter(c -> {
                    for (String categoryName : categoryNames) {
                        if (c.getName().equals(categoryName)) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
        
        List<UUID> categoryIds = categories.stream()
                .map(com.softuni.finalexam.models.entity.Category::getId)
                .toList();
        
        return productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null && categoryIds.contains(p.getCategory().getId()))
                .toList();
    }
    

    @GetMapping("/products/create")
    public String showCreateProductFormOnProductsPage(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        if (!model.containsAttribute("createProductDto")) {
            model.addAttribute("createProductDto", new CreateProductDto());
        }
        return "admin/product-create";
    }

    @PostMapping("/products/create")
    public String createProductFromProductsPage(
            @Valid @ModelAttribute("createProductDto") CreateProductDto createProductDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("createProductDto", createProductDto);
            return "admin/product-create";
        }

        try {
            Category cat = createProductDto.getCategoryId() != null 
                    ? categoryRepository.findById(createProductDto.getCategoryId()).orElse(null) 
                    : null;

            Product product = Product.builder()
                    .name(createProductDto.getName())
                    .description(createProductDto.getDescription())
                    .price(createProductDto.getPrice())
                    .stock(createProductDto.getStock())
                    .category(cat)
                    .imageUrl(createProductDto.getImageUrl() != null && !createProductDto.getImageUrl().trim().isEmpty() 
                            ? createProductDto.getImageUrl() : null)
                    .build();

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
            log.error("Error creating product", e);
            return "redirect:/products/create?error=true";
        }
    }


    @GetMapping("/products/{id}")
    public String showProductDetails(@PathVariable UUID id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }
        model.addAttribute("product", product);
        return "product-details";
    }

    @GetMapping("/admin/products/create")
    public String showCreateProductForm(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        if (!model.containsAttribute("createProductDto")) {
            model.addAttribute("createProductDto", new CreateProductDto());
        }
        return "admin/product-create";
    }

    @PostMapping("/admin/products")
    public String createProduct(
            @Valid @ModelAttribute("createProductDto") CreateProductDto createProductDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("createProductDto", createProductDto);
            return "admin/product-create";
        }

        try {
            Category category = createProductDto.getCategoryId() != null 
                    ? categoryRepository.findById(createProductDto.getCategoryId()).orElse(null) 
                    : null;

            Product product = Product.builder()
                    .name(createProductDto.getName())
                    .description(createProductDto.getDescription())
                    .price(createProductDto.getPrice())
                    .stock(createProductDto.getStock())
                    .category(category)
                    .imageUrl(createProductDto.getImageUrl())
                    .build();

            productRepository.save(product);
            redirectAttributes.addFlashAttribute("success", "Product created successfully!");
            return "redirect:/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create product: " + e.getMessage());
            log.error("Error creating product", e);
            return "redirect:/admin/products/create?error=true";
        }
    }


    @GetMapping("/admin/products")
    public String showAdminProducts(Model model) {
        List<Product> products = productRepository.findAll();
        Map<UUID, List<User>> productUsersMap = new HashMap<>();
        Map<UUID, List<UUID>> productOrderIdsMap = new HashMap<>();
        
        for (Product product : products) {
            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            
            // only show APPROVED orders
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
            
            if (!uniqueUsers.isEmpty()) {
                productUsersMap.put(product.getId(), new ArrayList<>(uniqueUsers));
            }
            if (!orderIds.isEmpty()) {
                productOrderIdsMap.put(product.getId(), orderIds);
            }
        }
        
        List<Product> productsWithApprovedOrders = products.stream()
                .filter(product -> productOrderIdsMap.containsKey(product.getId()) 
                        && !productOrderIdsMap.get(product.getId()).isEmpty())
                .collect(Collectors.toList());
        
        model.addAttribute("products", productsWithApprovedOrders);
        model.addAttribute("productUsersMap", productUsersMap);
        model.addAttribute("productOrderIdsMap", productOrderIdsMap);
        return "admin/products";
    }


    @DeleteMapping("/admin/orders/{id}")
    public String deleteProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productRepository.findById(id).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/admin/orders?error=true";
            }
            
            // need to delete order items first to avoid FK constraint
            List<OrderItem> orderItems = orderItemRepository.findByProduct(product);
            if (!orderItems.isEmpty()) {
                orderItemRepository.deleteAll(orderItems);
            }
            
            productRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
            return "redirect:/admin/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
            log.error("Error deleting product", e);
            return "redirect:/admin/orders?error=true";
        }
    }
}

