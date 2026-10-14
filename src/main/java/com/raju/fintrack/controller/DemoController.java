package com.raju.fintrack.controller;

import com.raju.fintrack.security.CurrentUser;
import com.raju.fintrack.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {
    private final DemoService demoService;
    private final CurrentUser currentUser;

    @PostMapping("/seed")
    public Map<String, String> seed() {
        demoService.seed(currentUser.get());
        return Map.of("message", "Sample data loaded");
    }
}
