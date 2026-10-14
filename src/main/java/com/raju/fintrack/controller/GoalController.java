package com.raju.fintrack.controller;

import com.raju.fintrack.dto.Dtos.ContributeRequest;
import com.raju.fintrack.dto.Dtos.GoalRequest;
import com.raju.fintrack.entity.SavingsGoal;
import com.raju.fintrack.entity.User;
import com.raju.fintrack.exception.ApiException;
import com.raju.fintrack.repository.SavingsGoalRepository;
import com.raju.fintrack.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
    private final SavingsGoalRepository goalRepo;
    private final CurrentUser currentUser;

    @GetMapping
    public List<SavingsGoal> list() {
        return goalRepo.findByUserOrderByIdAsc(currentUser.get());
    }

    @PostMapping
    public SavingsGoal create(@Valid @RequestBody GoalRequest req) {
        SavingsGoal g = new SavingsGoal();
        g.setUser(currentUser.get());
        return save(g, req);
    }

    @PutMapping("/{id}")
    public SavingsGoal update(@PathVariable Long id, @Valid @RequestBody GoalRequest req) {
        return save(find(id), req);
    }

    @PostMapping("/{id}/contribute")
    public SavingsGoal contribute(@PathVariable Long id, @Valid @RequestBody ContributeRequest req) {
        SavingsGoal g = find(id);
        g.setSaved(g.getSaved().add(req.amount()));
        return goalRepo.save(g);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        goalRepo.delete(find(id));
    }

    private SavingsGoal find(Long id) {
        User user = currentUser.get();
        return goalRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Goal not found"));
    }

    private SavingsGoal save(SavingsGoal g, GoalRequest req) {
        g.setName(req.name().trim());
        g.setTarget(req.target());
        g.setSaved(req.saved() == null ? BigDecimal.ZERO : req.saved());
        g.setDeadline(req.deadline());
        return goalRepo.save(g);
    }
}
