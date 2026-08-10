package com.softuni.finalexam.config;

import com.softuni.finalexam.models.entity.Category;
import com.softuni.finalexam.models.entity.Product;
import com.softuni.finalexam.repository.CategoryRepository;
import com.softuni.finalexam.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String SEED_WHISKEY = "Johnnie Walker Black Label";
    private static final String SEED_RED_WINE = "Merlot Reserve";
    private static final String SEED_BEER = "Stella Artois";
    private static final String SEED_SOFT_DRINK = "Coca-Cola";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (isSeedDataPresent()) {
            log.info("Seed products already exist. Skipping data initialization.");
            return;
        }

        Category whiskeyCategory = getOrCreateCategory("Whiskey");

        addProduct("Johnnie Walker Black Label",
                "Premium blended Scotch whisky with notes of vanilla, caramel, and smoke. Aged 12 years.",
                new BigDecimal("89.99"), 50, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop&q=80");

        addProduct("Jack Daniel's Old No. 7",
                "Classic Tennessee whiskey with a smooth, mellow flavor and charcoal mellowing process.",
                new BigDecimal("49.99"), 75, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop&q=80");

        addProduct("Jameson Irish Whiskey",
                "Triple-distilled Irish whiskey known for its smoothness and light floral notes.",
                new BigDecimal("39.99"), 60, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Macallan 12 Year Old",
                "Single malt Scotch whisky with rich sherry oak flavor and hints of dried fruits.",
                new BigDecimal("129.99"), 30, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Glenfiddich 15 Year Old",
                "Solera vat aged single malt Scotch whisky with warm honey and vanilla notes.",
                new BigDecimal("119.99"), 40, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Wild Turkey 101",
                "Bold Kentucky straight bourbon whiskey with rich flavors of caramel and spice.",
                new BigDecimal("45.99"), 55, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Maker's Mark",
                "Handcrafted bourbon whiskey with a distinctive red wax seal and smooth, balanced taste.",
                new BigDecimal("54.99"), 45, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Crown Royal Canadian Whisky",
                "Smooth Canadian whisky blended from 50 different whiskies for a rich, balanced flavor.",
                new BigDecimal("64.99"), 50, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Lagavulin 16 Year Old",
                "Peaty Islay single malt Scotch whisky with intense smoke and sea salt character.",
                new BigDecimal("149.99"), 25, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        addProduct("Yamazaki 12 Year Old",
                "Japanese single malt whisky with delicate fruit notes and subtle oak influence.",
                new BigDecimal("199.99"), 20, whiskeyCategory,
                "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop");

        log.info("Successfully initialized 10 whiskey products.");

        initializeBeverageProducts();
        updateExistingProductsWithImages();
    }

    private boolean isSeedDataPresent() {
        return productRepository.existsByName(SEED_WHISKEY)
                && productRepository.existsByName(SEED_RED_WINE)
                && productRepository.existsByName(SEED_BEER)
                && productRepository.existsByName(SEED_SOFT_DRINK);
    }

    private void initializeBeverageProducts() {
        Category redWineCategory = getOrCreateCategory("Red Wine");
        Category whiteWineCategory = getOrCreateCategory("White Wine");
        Category champagneCategory = getOrCreateCategory("Champagne");
        Category beerCategory = getOrCreateCategory("Beer");
        Category waterCategory = getOrCreateCategory("Water");
        Category softDrinkCategory = getOrCreateCategory("Soft Drink");

        addProduct("Merlot Reserve", "Smooth and velvety red wine with notes of black cherry and plum.", new BigDecimal("24.99"), 30, redWineCategory, "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80");
        addProduct("Cabernet Sauvignon", "Full-bodied red wine with rich tannins and dark fruit flavors.", new BigDecimal("29.99"), 25, redWineCategory, "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80");
        addProduct("Pinot Noir", "Elegant red wine with delicate flavors of red berries and earth.", new BigDecimal("27.99"), 28, redWineCategory, "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80");
        addProduct("Shiraz", "Bold and spicy red wine with dark fruit and pepper notes.", new BigDecimal("26.99"), 30, redWineCategory, "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80");
        addProduct("Malbec", "Rich red wine with flavors of blackberry and chocolate.", new BigDecimal("23.99"), 32, redWineCategory, "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80");

        addProduct("Chardonnay", "Crisp white wine with buttery notes and hints of oak.", new BigDecimal("22.99"), 35, whiteWineCategory, "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80");
        addProduct("Sauvignon Blanc", "Fresh and zesty white wine with citrus and herbaceous notes.", new BigDecimal("21.99"), 38, whiteWineCategory, "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80");
        addProduct("Pinot Grigio", "Light and refreshing white wine with pear and apple flavors.", new BigDecimal("19.99"), 40, whiteWineCategory, "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80");
        addProduct("Riesling", "Aromatic white wine with floral notes and balanced sweetness.", new BigDecimal("20.99"), 36, whiteWineCategory, "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80");
        addProduct("Moscato", "Sweet and fruity white wine with peach and apricot flavors.", new BigDecimal("18.99"), 42, whiteWineCategory, "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80");

        addProduct("Dom Pérignon", "Prestigious vintage champagne with complex flavors and fine bubbles.", new BigDecimal("199.99"), 15, champagneCategory, "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=400&h=600&fit=crop&q=80");
        addProduct("Moët & Chandon", "Classic champagne with elegant fruitiness and crisp finish.", new BigDecimal("89.99"), 20, champagneCategory, "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=400&h=600&fit=crop&q=80");

        addProduct("Stella Artois", "Crisp and refreshing Belgian lager with a smooth finish.", new BigDecimal("3.99"), 100, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Heineken", "Premium Dutch lager with a balanced, refreshing taste.", new BigDecimal("3.99"), 95, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Corona Extra", "Light Mexican lager with a smooth, crisp flavor.", new BigDecimal("4.49"), 90, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Budweiser", "Classic American lager with a clean, crisp taste.", new BigDecimal("3.49"), 110, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Guinness", "Rich and creamy Irish stout with roasted malt flavors.", new BigDecimal("5.99"), 80, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Carlsberg", "Danish pilsner with a balanced, hoppy taste.", new BigDecimal("3.79"), 85, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Beck's", "German pilsner with a crisp, clean flavor.", new BigDecimal("4.29"), 88, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Peroni", "Italian lager with a light, refreshing taste.", new BigDecimal("4.99"), 75, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Kozel", "Czech lager with a smooth, malty flavor.", new BigDecimal("3.99"), 92, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");
        addProduct("Zagorka", "Bulgarian lager with a crisp and refreshing taste.", new BigDecimal("2.99"), 105, beerCategory, "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80");

        addProduct("Evian Natural Spring Water", "Pure natural spring water from the French Alps.", new BigDecimal("2.49"), 150, waterCategory, "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80");
        addProduct("Perrier Sparkling Water", "Natural sparkling mineral water with fine bubbles.", new BigDecimal("2.99"), 120, waterCategory, "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80");
        addProduct("Fiji Natural Artesian Water", "Premium artesian water from Fiji.", new BigDecimal("3.49"), 100, waterCategory, "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80");
        addProduct("San Pellegrino", "Italian sparkling mineral water with natural minerals.", new BigDecimal("2.79"), 130, waterCategory, "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80");
        addProduct("Aquafina", "Purified water with a clean, crisp taste.", new BigDecimal("1.99"), 200, waterCategory, "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80");

        addProduct("Coca-Cola", "Classic cola with a refreshing, fizzy taste.", new BigDecimal("2.49"), 180, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Pepsi", "Sweet and refreshing cola with a bold flavor.", new BigDecimal("2.49"), 175, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Fanta Orange", "Fruity orange-flavored soft drink.", new BigDecimal("2.29"), 160, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Sprite", "Lemon-lime soft drink with a crisp, clean taste.", new BigDecimal("2.29"), 170, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("7UP", "Refreshing lemon-lime soda with a zesty flavor.", new BigDecimal("2.29"), 165, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Mountain Dew", "Citrus-flavored soft drink with a bold, energizing taste.", new BigDecimal("2.49"), 155, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Dr Pepper", "Unique blend of 23 flavors in one soft drink.", new BigDecimal("2.49"), 150, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Schweppes Tonic Water", "Classic tonic water with quinine.", new BigDecimal("2.79"), 140, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Red Bull", "Energy drink with taurine and caffeine.", new BigDecimal("3.99"), 120, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");
        addProduct("Monster Energy", "High-energy drink with a bold, fruity flavor.", new BigDecimal("3.49"), 125, softDrinkCategory, "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80");

        log.info("Successfully initialized beverage products.");
    }

    private Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category category = Category.builder().name(categoryName).build();
                    Category saved = categoryRepository.save(category);
                    log.info("Created category: {}", categoryName);
                    return saved;
                });
    }

    private void addProduct(String name, String description, BigDecimal price, int stock,
                            Category category, String imageUrl) {
        if (productRepository.existsByName(name)) {
            return;
        }

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .category(category)
                .imageUrl(imageUrl)
                .build();

        productRepository.save(product);
        log.info("Added product: {}", name);
    }

    private void updateExistingProductsWithImages() {
        final String whiskeyImage = "https://images.unsplash.com/photo-1605634738950-0e0a5c0b0e0a?w=400&h=600&fit=crop&q=80";
        final String redWineImage = "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?w=400&h=600&fit=crop&q=80";
        final String whiteWineImage = "https://images.unsplash.com/photo-1506377247727-4a9440c3d0b3?w=400&h=600&fit=crop&q=80";
        final String champagneImage = "https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=400&h=600&fit=crop&q=80";
        final String beerImage = "https://images.unsplash.com/photo-1608270586201-8b2070e24aa3?w=400&h=600&fit=crop&q=80";
        final String waterImage = "https://images.unsplash.com/photo-1523362628745-0c100150b504?w=400&h=600&fit=crop&q=80";
        final String softDrinkImage = "https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=600&fit=crop&q=80";

        List<Product> productsWithoutImages = productRepository.findAll().stream()
                .filter(p -> p.getImageUrl() == null || p.getImageUrl().isBlank())
                .toList();

        if (productsWithoutImages.isEmpty()) {
            log.info("All products already have images assigned.");
            return;
        }

        int updatedCount = 0;
        for (Product product : productsWithoutImages) {
            String imageUrl = determineImageUrl(product, whiskeyImage, redWineImage, whiteWineImage,
                    champagneImage, beerImage, waterImage, softDrinkImage);

            if (imageUrl != null) {
                product.setImageUrl(imageUrl);
                productRepository.save(product);
                updatedCount++;
            }
        }

        log.info("Updated {} products with image URLs.", updatedCount);
    }

    private String determineImageUrl(Product product, String whiskeyImage, String redWineImage,
                                     String whiteWineImage, String champagneImage, String beerImage,
                                     String waterImage, String softDrinkImage) {
        if (product.getCategory() != null && product.getCategory().getName() != null) {
            String categoryName = product.getCategory().getName().toLowerCase();

            if (categoryName.contains("whiskey") || categoryName.contains("whisky")) {
                return whiskeyImage;
            } else if (categoryName.contains("red wine")) {
                return redWineImage;
            } else if (categoryName.contains("white wine")) {
                return whiteWineImage;
            } else if (categoryName.contains("champagne")) {
                return champagneImage;
            } else if (categoryName.contains("beer")) {
                return beerImage;
            } else if (categoryName.contains("water")) {
                return waterImage;
            } else if (categoryName.contains("soft drink") || categoryName.contains("softdrink")) {
                return softDrinkImage;
            }
        }

        if (product.getName() != null) {
            String productName = product.getName().toLowerCase();

            if (productName.contains("whiskey") || productName.contains("whisky")
                    || productName.contains("bourbon") || productName.contains("scotch")) {
                return whiskeyImage;
            }
            if (productName.contains("merlot") || productName.contains("cabernet")
                    || productName.contains("pinot noir") || productName.contains("shiraz")
                    || productName.contains("malbec") || productName.contains("red wine")) {
                return redWineImage;
            }
            if (productName.contains("chardonnay") || productName.contains("sauvignon")
                    || productName.contains("pinot grigio") || productName.contains("riesling")
                    || productName.contains("moscato") || productName.contains("white wine")) {
                return whiteWineImage;
            }
            if (productName.contains("champagne") || productName.contains("dom pérignon")
                    || productName.contains("moët") || productName.contains("moet")) {
                return champagneImage;
            }
            if (productName.contains("beer") || productName.contains("lager")
                    || productName.contains("stout") || productName.contains("pilsner")
                    || productName.contains("stella") || productName.contains("heineken")
                    || productName.contains("corona") || productName.contains("budweiser")
                    || productName.contains("guinness") || productName.contains("carlsberg")
                    || productName.contains("beck") || productName.contains("peroni")
                    || productName.contains("kozel") || productName.contains("zagorka")) {
                return beerImage;
            }
            if (productName.contains("water") || productName.contains("evian")
                    || productName.contains("perrier") || productName.contains("fiji")
                    || productName.contains("pellegrino") || productName.contains("aquafina")) {
                return waterImage;
            }
            if (productName.contains("cola") || productName.contains("coca")
                    || productName.contains("pepsi") || productName.contains("fanta")
                    || productName.contains("sprite") || productName.contains("7up")
                    || productName.contains("mountain dew") || productName.contains("dr pepper")
                    || productName.contains("schweppes") || productName.contains("red bull")
                    || productName.contains("monster") || productName.contains("energy")
                    || productName.contains("soda") || productName.contains("soft drink")) {
                return softDrinkImage;
            }
        }

        log.warn("Could not determine image URL for product: {}", product.getName());
        return null;
    }
}
