package com.college.backend.dto;

import com.college.backend.model.User;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String course;
    private String year;
    private String phone;
    private String gender;
    private String dob;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.course = user.getCourse();
        this.year = user.getYear();
        this.phone = user.getPhone();
        this.gender = user.getGender();
        this.dob = user.getDob();
    }
}
