package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "webinar_resources")
public class WebinarResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    private String type;

    @ManyToOne
    @JoinColumn(name = "webinar_id", nullable = false)
    private Webinar webinar;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();
}
