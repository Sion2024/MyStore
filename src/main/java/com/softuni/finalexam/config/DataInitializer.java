package com.softuni.finalexam.config;

import com.softuni.finalexam.models.entity.Category;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.CategoryRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        // Check if whiskey products already exist
        if (productRepository.findAll().stream().anyMatch(p -> p.getName() != null && p.getName().toLowerCase().contains("whiskey"))) {
            log.info("Whiskey products already exist. Skipping data initialization.");
            return;
        }

        // Create or find Whiskey category
        Category whiskeyCategory = categoryRepository.findByName("Whiskey")
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .name("Whiskey")
                            .build();
                    Category saved = categoryRepository.save(category);
                    log.info("Created Whiskey category: {}", saved.getId());
                    return saved;
                });

        // Add 10 whiskey products
        addWhiskeyProduct("Johnnie Walker Black Label", 
                "Premium blended Scotch whisky with notes of vanilla, caramel, and smoke. Aged 12 years.", 
                new BigDecimal("89.99"), 50, whiskeyCategory);

        addWhiskeyProduct("Jack Daniel's Old No. 7", 
                "Classic Tennessee whiskey with a smooth, mellow flavor and charcoal mellowing process.", 
                new BigDecimal("49.99"), 75, whiskeyCategory);

        addWhiskeyProduct("Jameson Irish Whiskey", 
                "Triple-distilled Irish whiskey known for its smoothness and light floral notes.", 
                new BigDecimal("39.99"), 60, whiskeyCategory);

        addWhiskeyProduct("Macallan 12 Year Old", 
                "Single malt Scotch whisky with rich sherry oak flavor and hints of dried fruits.", 
                new BigDecimal("129.99"), 30, whiskeyCategory);

        addWhiskeyProduct("Glenfiddich 15 Year Old", 
                "Solera vat aged single malt Scotch whisky with warm honey and vanilla notes.", 
                new BigDecimal("119.99"), 40, whiskeyCategory);

        addWhiskeyProduct("Wild Turkey 101", 
                "Bold Kentucky straight bourbon whiskey with rich flavors of caramel and spice.", 
                new BigDecimal("45.99"), 55, whiskeyCategory);

        addWhiskeyProduct("Maker's Mark", 
                "Handcrafted bourbon whiskey with a distinctive red wax seal and smooth, balanced taste.", 
                new BigDecimal("54.99"), 45, whiskeyCategory);

        addWhiskeyProduct("Crown Royal Canadian Whisky", 
                "Smooth Canadian whisky blended from 50 different whiskies for a rich, balanced flavor.", 
                new BigDecimal("64.99"), 50, whiskeyCategory);

        addWhiskeyProduct("Lagavulin 16 Year Old", 
                "Peaty Islay single malt Scotch whisky with intense smoke and sea salt character.", 
                new BigDecimal("149.99"), 25, whiskeyCategory);

        addWhiskeyProduct("Yamazaki 12 Year Old", 
                "Japanese single malt whisky with delicate fruit notes and subtle oak influence.", 
                new BigDecimal("199.99"), 20, whiskeyCategory);

        log.info("Successfully initialized 10 whiskey products!");
    }

    private void addWhiskeyProduct(String name, String description, BigDecimal price, int stock, Category category) {
        Optional<Product> existingProduct = productRepository.findAll().stream()
                .filter(p -> p.getName() != null && p.getName().equals(name))
                .findFirst();

        if (existingProduct.isEmpty()) {
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .category(category)
                    .build();

            productRepository.save(product);
            log.info("Added whiskey product: {}", name);
        }
    }
}

