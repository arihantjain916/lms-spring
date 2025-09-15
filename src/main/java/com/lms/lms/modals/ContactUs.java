package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class ContactUs {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String name;
    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String email;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String subject;

    @Column(nullable = false, columnDefinition = "varchar(255)")
    private String message;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    private Department department;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    private String phone;

    public enum Department {
        GENERAL, TECHNICAL, SALES, BILLING
    }
}
