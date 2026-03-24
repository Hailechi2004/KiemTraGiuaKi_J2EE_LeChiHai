package com.example.demo.controller;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final StudentService studentService;
    private final String googleClientId;
    private final String googleClientSecret;

    public AuthController(
        StudentService studentService,
        @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
        @Value("${spring.security.oauth2.client.registration.google.client-secret:}") String googleClientSecret
    ) {
        this.studentService = studentService;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
    }

    @GetMapping("/login")
    public String login(Model model) {
        boolean googleLoginEnabled = isConfigured(googleClientId) && isConfigured(googleClientSecret);
        model.addAttribute("googleLoginEnabled", googleLoginEnabled);
        return "auth-login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth-register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
        BindingResult bindingResult,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth-register";
        }

        try {
            studentService.registerStudent(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth-register";
        }

        return "redirect:/auth/login?registered";
    }

    private boolean isConfigured(String value) {
        return value != null && !value.isBlank() && !value.contains("YOUR_GOOGLE");
    }
}
