package com.example.demo.repository;

import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourseAndAppointmentDate(Student student, Course course, java.time.LocalDateTime appointmentDate);
    List<Enrollment> findByStudentOrderByAppointmentDateDesc(Student student);
}
