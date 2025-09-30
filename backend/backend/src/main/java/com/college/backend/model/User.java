package com.college.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String password;
    private String course;
    private String year;
    private String phone;
    private String gender;
    private String dob;

    @Column(length = 255)
    private String selectedSubjects; // comma-separated subject codes
}
