package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên bác sĩ không được để trống")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String image;

    @NotBlank(message = "Chuyên khoa không được để trống")
    @Column(name = "specialty", nullable = false)
    private String specialty;

    @NotNull(message = "Năm kinh nghiệm là bắt buộc")
    @Min(value = 0, message = "Năm kinh nghiệm phải lớn hơn hoặc bằng 0")
    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
