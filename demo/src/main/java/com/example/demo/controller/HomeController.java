package com.example.demo.controller;

import com.example.demo.model.Course;
import com.example.demo.service.CourseService;
import com.example.demo.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public HomeController(CourseService courseService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping({"/", "/home", "/courses"})
    public String home(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) String keyword,
        Model model,
        Authentication authentication,
        HttpServletRequest request
    ) {
        Page<Course> courses = courseService.getCourses(keyword, page, size);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            model.addAttribute("currentUsername", authentication.getName());
            Set<Long> enrolledCourseIds = enrollmentService.getEnrolledCourseIds(authentication.getName());
            model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        } else {
            model.addAttribute("enrolledCourseIds", Collections.emptySet());
        }
        return "home";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Course course;
        try {
            course = courseService.getById(id);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", "Không tìm thấy bác sĩ với ID " + id);
            return "course-not-found";
        }

        model.addAttribute("course", course);
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {
            model.addAttribute("currentUsername", authentication.getName());
            model.addAttribute("enrolled", enrollmentService.isEnrolled(authentication.getName(), id));
        } else {
            model.addAttribute("enrolled", false);
        }
        return "course-detail";
    }
}
