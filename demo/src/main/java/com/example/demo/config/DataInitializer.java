package com.example.demo.config;

import com.example.demo.model.Category;
import com.example.demo.model.Course;
import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.Student;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StudentRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           StudentRepository studentRepository,
                           CategoryRepository categoryRepository,
                           CourseRepository courseRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
        this.categoryRepository = categoryRepository;
        this.courseRepository = courseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
            .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN)));
        Role patientRole = roleRepository.findByName(RoleName.PATIENT)
            .orElseGet(() -> roleRepository.save(new Role(RoleName.PATIENT)));

        seedDefaultUsers(adminRole, patientRole);
        if (courseRepository.count() == 0) {
            seedCourses();
        } else {
            migrateLegacyCategories();
        }
    }

    private void seedDefaultUsers(Role adminRole, Role patientRole) {
        Student admin = studentRepository.findByUsername("admin").orElseGet(() -> {
            Student user = new Student();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setEmail("admin@clinic.local");
            return user;
        });
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        studentRepository.save(admin);

        Student patient = studentRepository.findByUsername("patient").orElseGet(() -> {
            Student user = new Student();
            user.setUsername("patient");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setEmail("patient@clinic.local");
            return user;
        });
        Set<Role> patientRoles = new HashSet<>();
        patientRoles.add(patientRole);
        patient.setRoles(patientRoles);
        studentRepository.save(patient);
    }

    private void seedCourses() {
        Category timMach = categoryRepository.findByName("Tim mach")
            .orElseGet(() -> categoryRepository.save(new Category("Tim mach")));
        Category noi = categoryRepository.findByName("Noi")
            .orElseGet(() -> categoryRepository.save(new Category("Noi")));
        Category nhi = categoryRepository.findByName("Nhi")
            .orElseGet(() -> categoryRepository.save(new Category("Nhi")));
        Category daLieu = categoryRepository.findByName("Da lieu")
            .orElseGet(() -> categoryRepository.save(new Category("Da lieu")));
        Category taiMuiHong = categoryRepository.findByName("Tai mui hong")
            .orElseGet(() -> categoryRepository.save(new Category("Tai mui hong")));
        Category mat = categoryRepository.findByName("Mat")
            .orElseGet(() -> categoryRepository.save(new Category("Mat")));
        Category rangHamMat = categoryRepository.findByName("Rang ham mat")
            .orElseGet(() -> categoryRepository.save(new Category("Rang ham mat")));
        Category noiTiet = categoryRepository.findByName("Noi tiet")
            .orElseGet(() -> categoryRepository.save(new Category("Noi tiet")));

        courseRepository.saveAll(List.of(
            buildCourse("PGS.TS. Nguyen Van A", 15, "Tim mach", timMach,
                "https://images.unsplash.com/photo-1526256262350-7da7584cf5eb?w=1200"),
            buildCourse("BS. Le Thi B", 10, "Noi tong hop", noi,
                "https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?w=1200"),
            buildCourse("BS. Tran Van C", 8, "Nhi", nhi,
                "https://images.unsplash.com/photo-1484995998482-1dc8dc0d31c5?w=1200"),
            buildCourse("BS. Vo Minh D", 12, "Da lieu", daLieu,
                "https://images.unsplash.com/photo-1550831107-1553da8c8464?w=1200"),
            buildCourse("BS. Pham Hong E", 20, "Tai mui hong", taiMuiHong,
                "https://images.unsplash.com/photo-1580281657521-72fcb1e8f27f?w=1200"),
            buildCourse("BS. Do Quang F", 7, "Mat", mat,
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=1200"),
            buildCourse("BS. Hoang Thi G", 5, "Rang ham mat", rangHamMat,
                "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1200"),
            buildCourse("BS. Bui Quoc H", 17, "Noi tiet", noiTiet,
                "https://images.unsplash.com/photo-1529448287466-e5a46c961a9d?w=1200")
        ));
    }

    private void migrateLegacyCategories() {
        renameCategory("Backend", "Tim mach");
        renameCategory("Frontend", "Noi");
        renameCategory("Database", "Noi tiet");
    }

    private void renameCategory(String oldName, String newName) {
        categoryRepository.findByName(oldName).ifPresent(oldCategory -> {
            categoryRepository.findByName(newName).ifPresentOrElse(
                existing -> {
                    // Nếu đã tồn tại, không duplicate
                },
                () -> {
                    oldCategory.setName(newName);
                    categoryRepository.save(oldCategory);
                }
            );
        });
    }

    private Course buildCourse(String name, int experienceYears, String specialty, Category category, String image) {
        Course course = new Course();
        course.setName(name);
        course.setExperienceYears(experienceYears);
        course.setSpecialty(specialty);
        course.setCategory(category);
        course.setImage(image);
        return course;
    }
}
