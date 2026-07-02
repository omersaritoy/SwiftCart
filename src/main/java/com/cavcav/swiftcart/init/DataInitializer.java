package com.cavcav.swiftcart.init;

import com.cavcav.swiftcart.product.model.Category;
import com.cavcav.swiftcart.product.repository.CategoryRepository;
import com.cavcav.swiftcart.user.model.Role;
import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initCustomers();
        initSellers();
        initCategories();
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





}
