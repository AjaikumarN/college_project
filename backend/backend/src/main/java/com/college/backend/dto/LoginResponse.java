package com.college.backend.dto;

import com.college.backend.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    private String name;
    private String email;
    private String course;
    private String year;
    private String semester;
    private String phone;
    private String gender;
    private String role;
    private Boolean isVerified;
    private Boolean isActive;
    private String selectedSubjects;
    private String accessToken;
    private String tokenType = "Bearer";

    // Constructors
    public LoginResponse() {}

    public LoginResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.course = user.getCourse();
        this.year = user.getYear();
        this.semester = user.getSemester();
        this.phone = user.getPhone();
        this.gender = user.getGender();
        this.role = user.getRole() != null ? user.getRole().toString().toLowerCase() : "student";
        this.isVerified = user.getIsVerified();
        this.isActive = user.getIsActive();
        this.selectedSubjects = user.getSelectedSubjects();
    }
    
    public LoginResponse(User user, String accessToken) {
        this(user);
        this.accessToken = accessToken;
    }
}