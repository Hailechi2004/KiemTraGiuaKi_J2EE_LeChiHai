package com.example.demo.service;

import com.example.demo.model.Course;
import com.example.demo.model.Enrollment;
import com.example.demo.model.Student;
import com.example.demo.repository.EnrollmentRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final StudentService studentService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, CourseService courseService, StudentService studentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseService = courseService;
        this.studentService = studentService;
    }

    public boolean enroll(String username, Long courseId, java.time.LocalDateTime appointmentDate) {
        Student student = studentService.findByUsername(username);
        Course course = courseService.getById(courseId);

        if (enrollmentRepository.existsByStudentAndCourseAndAppointmentDate(student, course, appointmentDate)) {
            return false;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setAppointmentDate(appointmentDate);
        enrollmentRepository.save(enrollment);
        return true;
    }

    public List<Enrollment> getMyEnrollments(String username) {
        Student student = studentService.findByUsername(username);
        return enrollmentRepository.findByStudentOrderByAppointmentDateDesc(student);
    }

    public Set<Long> getEnrolledCourseIds(String username) {
        List<Enrollment> enrollments = getMyEnrollments(username);
        Set<Long> courseIds = new HashSet<>();
        for (Enrollment enrollment : enrollments) {
            courseIds.add(enrollment.getCourse().getId());
        }
        return courseIds;
    }

    public boolean isEnrolled(String username, Long courseId) {
        return getEnrolledCourseIds(username).contains(courseId);
    }
}
