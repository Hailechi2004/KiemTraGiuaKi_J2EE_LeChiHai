package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.Student;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.StudentRepository;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Student registerStudent(String username, String rawPassword, String email) {
        if (studentRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Role studentRole = roleRepository.findByName(RoleName.PATIENT)
            .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo quyền PATIENT"));

        Student student = new Student();
        student.setUsername(username.trim());
        student.setPassword(passwordEncoder.encode(rawPassword));
        student.setEmail(email.trim().toLowerCase(Locale.ROOT));
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        student.setRoles(roles);
        return studentRepository.save(student);
    }

    public Student findByUsername(String username) {
        return studentRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
    }

    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản theo email"));
    }

    public Student createOrGetOauthStudent(String email, String preferredUsername) {
        return studentRepository.findByEmail(email).orElseGet(() -> {
            String baseUsername = preferredUsername == null || preferredUsername.isBlank()
                ? email.split("@")[0]
                : preferredUsername.trim().toLowerCase(Locale.ROOT).replace(" ", "_");
            String username = baseUsername;
            int suffix = 1;
            while (studentRepository.existsByUsername(username)) {
                username = baseUsername + suffix;
                suffix++;
            }

            Role studentRole = roleRepository.findByName(RoleName.PATIENT)
                .orElseThrow(() -> new IllegalStateException("Chưa khởi tạo quyền PATIENT"));

            Student student = new Student();
            student.setUsername(username);
            student.setEmail(email.trim().toLowerCase(Locale.ROOT));
            student.setPassword(passwordEncoder.encode("oauth2_user_default_password"));
            Set<Role> roles = new HashSet<>();
            roles.add(studentRole);
            student.setRoles(roles);
            return studentRepository.save(student);
        });
    }
}
