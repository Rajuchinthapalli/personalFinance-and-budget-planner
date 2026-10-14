package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.CategoryRequest;
import com.raju.fintrack.entity.Category;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.BudgetRepository;
import com.raju.fintrack.repository.CategoryRepository;
import com.raju.fintrack.repository.TransactionRepository;
import com.raju.fintrack.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepo;
    private final TransactionRepository txRepo;
    private final BudgetRepository budgetRepo;
    private final CurrentUser currentUser;

    @GetMapping
    public List<Category> list() {
        return categoryRepo.findByUserOrderByTypeAscNameAsc(currentUser.get());
    }

    @PostMapping
    public Category create(@Valid @RequestBody CategoryRequest req) {
        Category c = new Category();
        c.setUser(currentUser.get());
        return save(c, req);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return save(find(id), req);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id) {
        Category c = find(id);
        if (txRepo.existsByCategory(c)) {
            throw new ApiException(HttpStatus.CONFLICT, "This category is used by transactions. Delete them first.");
        }
        budgetRepo.deleteByCategory(c);
        categoryRepo.delete(c);
    }

    private Category find(Long id) {
        User user = currentUser.get();
        return categoryRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private Category save(Category c, CategoryRequest req) {
        c.setName(req.name().trim());
        c.setType(req.type());
        c.setParentId(req.parentId());
        return categoryRepo.save(c);
    }
}
