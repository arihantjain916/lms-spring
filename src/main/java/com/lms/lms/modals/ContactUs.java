package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

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

    @Column(nullable = false)
    private Department department;

    @Column(nullable = false, columnDefinition = "varchar(20)")
    private String phone;

    // nullable so adding the column to a table with existing rows does not fail;
    // new submissions default to OPEN
    @Enumerated(value = EnumType.STRING)
    private Status status = Status.OPEN;

    // nullable for the same reason; populated on insert for new submissions
    @CreationTimestamp
    private Date createdAt = new Date();

    public enum Department {
        GENERAL, TECHNICAL, SALES, BILLING
    }

    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED
    }
}
