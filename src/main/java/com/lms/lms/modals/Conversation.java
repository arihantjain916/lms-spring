package com.lms.lms.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    // whoever opened the thread; always the student/user side.
    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    // the instructor/admin on the other side of a DIRECT thread.
    // null for SUPPORT: those sit in a shared queue any ADMIN can answer.
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Courses course;

    @Column(columnDefinition = "TEXT")
    private String subject;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    // denormalised so inboxes sort without joining messages
    private Date lastMessageAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt = new Date();

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt = new Date();

    public enum Type {
        SUPPORT,
        DIRECT
    }

    public enum Status {
        OPEN,
        CLOSED
    }
}
