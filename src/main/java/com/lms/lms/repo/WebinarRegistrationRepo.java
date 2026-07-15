package com.lms.lms.repo;

import com.lms.lms.modals.WebinarRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebinarRegistrationRepo extends JpaRepository<WebinarRegistration, String> {

    Boolean existsByUser_IdAndWebinar_Id(String userId, String webinarId);

    int deleteByUser_IdAndWebinar_Id(String userId, String webinarId);

    Page<WebinarRegistration> findByUser_Id(String userId, Pageable pageable);

    Page<WebinarRegistration> findByWebinar_Id(String webinarId, Pageable pageable);

    Integer countByWebinar_Id(String webinarId);

    int deleteByUser_Id(String userId);
}
