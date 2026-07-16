package com.lms.lms.repo;

import com.lms.lms.modals.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, String> {

    Page<Notification> findByUser_Id(String userId, Pageable pageable);

    Page<Notification> findByUser_IdAndIsReadFalse(String userId, Pageable pageable);

    long countByUser_IdAndIsReadFalse(String userId);

    // scoped by user so one account cannot mark another's notification read
    Optional<Notification> findByIdAndUser_Id(String id, String userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllRead(@Param("userId") String userId);
}
