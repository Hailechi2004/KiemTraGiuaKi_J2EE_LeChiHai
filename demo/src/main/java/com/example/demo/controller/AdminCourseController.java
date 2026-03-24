package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.model.Course;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;
    private final CategoryRepository categoryRepository;

    public AdminCourseController(CourseService courseService, CategoryRepository categoryRepository) {
        this.courseService = courseService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("courses", courseService.getCourses(null, 0, 100).getContent());
        return "admin-course-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("categories", getCategories());
        return "admin-course-form";
    }

    @PostMapping("/create")
    public String create(
        @Valid @ModelAttribute("course") Course course,
        BindingResult bindingResult,
        @RequestParam Long categoryId,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", getCategories());
            return "admin-course-form";
        }
        course.setCategory(categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục")));
        courseService.save(course);
        return "redirect:/admin/courses?created";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseService.getById(id));
        model.addAttribute("categories", getCategories());
        return "admin-course-form";
    }

    @PostMapping("/edit/{id}")
    public String edit(
        @PathVariable Long id,
        @Valid @ModelAttribute("course") Course course,
        BindingResult bindingResult,
        @RequestParam Long categoryId,
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", getCategories());
            return "admin-course-form";
        }
        course.setCategory(categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục")));
        course.setId(id);
        courseService.save(course);
        return "redirect:/admin/courses?updated";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses?deleted";
    }

    private List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}
