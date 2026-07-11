package com.lms.lms.repo;

import com.lms.lms.modals.WebinarResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebinarResourceRepo extends JpaRepository<WebinarResource, String> {

    List<WebinarResource> findByWebinar_IdOrderByCreatedAtAsc(String webinarId);
}
