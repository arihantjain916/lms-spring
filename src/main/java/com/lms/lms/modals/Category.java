package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
//@Data
@Entity
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String slug;
    @Column(unique = false, nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    private Boolean isFeatured = false;

    @Column(nullable = true)
    private String icon = "Code";

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Courses> courses = new ArrayList<>();
}
