package com.example.demo.controller;

import com.example.demo.service.EnrollmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/enroll")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/{courseId}")
    public String enroll(
        @PathVariable Long courseId,
        @RequestParam(name = "appointmentDate", required = false) String appointmentDate,
        Authentication authentication
    ) {
        if (appointmentDate == null || appointmentDate.isBlank()) {
            return "redirect:/home?dateMissing";
        }

        java.time.LocalDate parsedDate;
        try {
            parsedDate = java.time.LocalDate.parse(appointmentDate);
        } catch (java.time.format.DateTimeParseException ex) {
            return "redirect:/home?invalidDate";
        }

        java.time.LocalDateTime appointmentDateTime = parsedDate.atTime(9, 0);

        boolean enrolled = enrollmentService.enroll(authentication.getName(), courseId, appointmentDateTime);
        if (enrolled) {
            return "redirect:/home?booked";
        }
        return "redirect:/home?duplicate";
    }

    @GetMapping("/my-courses")
    public String myCourses(Model model, Authentication authentication) {
        model.addAttribute("enrollments", enrollmentService.getMyEnrollments(authentication.getName()));
        model.addAttribute("currentUsername", authentication.getName());
        return "my-courses";
    }
}
