package com.raju.fintrack.service;

import com.raju.fintrack.dto.Dtos.*;
import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.TransactionType;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.UserRepository;
import com.raju.fintrack.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final List<String> EXPENSE_DEFAULTS = List.of(
            "Food & Groceries", "Transport", "Home & Rent", "Utilities", "Health",
            "Education", "Entertainment", "Shopping", "Subscriptions");
    private static final List<String> INCOME_DEFAULTS = List.of("Salary", "Freelance", "Other Income");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);

        EXPENSE_DEFAULTS.forEach(n -> seedCategory(user, n, TransactionType.EXPENSE));
        INCOME_DEFAULTS.forEach(n -> seedCategory(user, n, TransactionType.INCOME));

        return new AuthResponse(jwtUtil.generate(email), user.getName(), email);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        }
        return new AuthResponse(jwtUtil.generate(email), user.getName(), email);
    }

    private void seedCategory(User user, String name, TransactionType type) {
        Category c = new Category();
        c.setName(name);
        c.setType(type);
        c.setUser(user);
        categoryRepository.save(c);
    }
}
