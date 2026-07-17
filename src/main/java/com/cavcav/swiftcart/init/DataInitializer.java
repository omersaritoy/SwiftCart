package com.cavcav.swiftcart.init;

import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.model.Product;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import com.cavcav.swiftcart.product.repository.ProductRepository;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initCustomers();
        initSellers();
        initCategories();
        initProducts();

    }

    private void initAdmin() {
        if (!userRepository.existsByEmail("admin@swiftcart.com")) {
            User admin = new User();
            admin.setEmail("admin@swiftcart.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            admin.setIsEmailVerified(true);
            userRepository.save(admin);
            log.info("Admin created: email=admin@swiftcart.com");
        }
    }

    private void initCustomers() {
        List<String> customerEmails = List.of(
                "alice@gmail.com",
                "bob@gmail.com",
                "charlie@gmail.com",
                "diana@gmail.com",
                "eve@gmail.com",
                "frank@gmail.com",
                "grace@gmail.com",
                "henry@gmail.com",
                "iris@gmail.com",
                "jack@gmail.com"
        );

        customerEmails.forEach(email -> {
            if (!userRepository.existsByEmail(email)) {
                User customer = new User();
                customer.setEmail(email);
                customer.setPassword(passwordEncoder.encode("password123"));
                customer.setRole(Role.CUSTOMER);
                customer.setIsActive(true);
                customer.setIsEmailVerified(true);
                userRepository.save(customer);
            }
        });

        log.info("Customers initialized: total=10");
    }

    private void initSellers() {
        List<String> sellerEmails = List.of(
                "seller1@gmail.com",
                "seller2@gmail.com",
                "seller3@gmail.com",
                "seller4@gmail.com",
                "seller5@gmail.com",
                "seller6@gmail.com",
                "seller7@gmail.com",
                "seller8@gmail.com",
                "seller9@gmail.com",
                "seller10@gmail.com"
        );

        sellerEmails.forEach(email -> {
            if (!userRepository.existsByEmail(email)) {
                User seller = new User();
                seller.setEmail(email);
                seller.setPassword(passwordEncoder.encode("password123"));
                seller.setRole(Role.SELLER);
                seller.setIsActive(true);
                seller.setIsEmailVerified(true);
                userRepository.save(seller);
            }
        });

        log.info("Sellers initialized: total=10");
    }

    private void initCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already exist, skipping");
            return;
        }

        // Ana kategoriler
        Category electronics = createCategory("Electronics", "Electronic devices and accessories", null);
        Category clothing = createCategory("Clothing", "Fashion and apparel", null);
        Category books = createCategory("Books", "Books and publications", null);
        Category sports = createCategory("Sports", "Sports and outdoor equipment", null);
        Category home = createCategory("Home & Garden", "Home and garden products", null);

        // Alt kategoriler — Electronics
        createCategory("Phones", "Smartphones and accessories", electronics);
        createCategory("Laptops", "Laptops and computers", electronics);
        createCategory("Tablets", "Tablets and e-readers", electronics);

        // Alt kategoriler — Clothing
        createCategory("Men's Clothing", "Men's fashion", clothing);
        createCategory("Women's Clothing", "Women's fashion", clothing);

        log.info("Categories initialized: total=10");
    }

    private Category createCategory(String name, String description, Category parent) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .parentCategory(parent)
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }

    private void initProducts() {
        if (productRepository.count() > 0) {
            log.info("Products already exist, skipping");
            return;
        }

        User seller1 = userRepository.findByEmail("seller1@gmail.com").orElseThrow();
        User seller2 = userRepository.findByEmail("seller2@gmail.com").orElseThrow();
        User seller3 = userRepository.findByEmail("seller3@gmail.com").orElseThrow();

        Category phones = categoryRepository.findByName("Phones").orElseThrow();
        Category laptops = categoryRepository.findByName("Laptops").orElseThrow();
        Category tablets = categoryRepository.findByName("Tablets").orElseThrow();
        Category mens = categoryRepository.findByName("Men's Clothing").orElseThrow();
        Category womens = categoryRepository.findByName("Women's Clothing").orElseThrow();
        Category books = categoryRepository.findByName("Books").orElseThrow();
        Category sports = categoryRepository.findByName("Sports").orElseThrow();
        Category home = categoryRepository.findByName("Home & Garden").orElseThrow();

        // Phones
        createProduct("iPhone 15 Pro", "Apple iPhone 15 Pro 256GB", new BigDecimal("1299.99"), 50, phones, seller1);
        createProduct("Samsung Galaxy S24", "Samsung Galaxy S24 128GB", new BigDecimal("999.99"), 75, phones, seller1);
        createProduct("Google Pixel 8", "Google Pixel 8 128GB", new BigDecimal("799.99"), 40, phones, seller2);

        // Laptops
        createProduct("MacBook Pro 14", "Apple MacBook Pro M3 14 inch", new BigDecimal("1999.99"), 20, laptops, seller1);
        createProduct("Dell XPS 15", "Dell XPS 15 Intel i7 16GB RAM", new BigDecimal("1499.99"), 30, laptops, seller2);
        createProduct("Lenovo ThinkPad X1", "Lenovo ThinkPad X1 Carbon", new BigDecimal("1299.99"), 25, laptops, seller2);

        // Tablets
        createProduct("iPad Pro 12.9", "Apple iPad Pro M2 12.9 inch", new BigDecimal("1099.99"), 35, tablets, seller1);
        createProduct("Samsung Galaxy Tab S9", "Samsung Galaxy Tab S9 Ultra", new BigDecimal("849.99"), 45, tablets, seller3);

        // Men's Clothing
        createProduct("Levi's 501 Jeans", "Classic straight fit jeans", new BigDecimal("79.99"), 100, mens, seller2);
        createProduct("Nike Air Force 1", "Nike Air Force 1 White", new BigDecimal("109.99"), 80, mens, seller3);
        createProduct("Polo Ralph Lauren Shirt", "Classic fit polo shirt", new BigDecimal("89.99"), 60, mens, seller3);

        // Women's Clothing
        createProduct("Zara Floral Dress", "Summer floral midi dress", new BigDecimal("59.99"), 90, womens, seller2);
        createProduct("H&M Blazer", "Oversized fit blazer", new BigDecimal("69.99"), 70, womens, seller3);
        createProduct("Adidas Ultraboost", "Women's running shoes", new BigDecimal("149.99"), 55, womens, seller1);

        // Books
        createProduct("Clean Code", "Clean Code by Robert C. Martin", new BigDecimal("39.99"), 200, books, seller3);
        createProduct("The Pragmatic Programmer", "20th Anniversary Edition", new BigDecimal("49.99"), 150, books, seller2);
        createProduct("Designing Data-Intensive Applications", "By Martin Kleppmann", new BigDecimal("54.99"), 120, books, seller3);

        // Sports
        createProduct("Wilson Tennis Racket", "Wilson Pro Staff RF97", new BigDecimal("199.99"), 30, sports, seller1);
        createProduct("Garmin Forerunner 955", "GPS Running Smartwatch", new BigDecimal("499.99"), 25, sports, seller2);

        // Home
        createProduct("Dyson V15 Detect", "Cordless vacuum cleaner", new BigDecimal("649.99"), 20, home, seller3);

        log.info("Products initialized: total=20");
    }

    private void createProduct(String name, String description, BigDecimal price,
                               Integer stock, Category category, User seller) {
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .category(category)
                .seller(seller)
                .imageUrls(new ArrayList<>())
                .isActive(true)
                .build();
        productRepository.save(product);
    }


}
