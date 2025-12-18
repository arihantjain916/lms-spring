package com.lms.lms.repo;

import com.lms.lms.modals.AiDailyUsage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface AiDailyUsageRepo extends JpaRepository<AiDailyUsage, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT u FROM AiDailyUsage u
                WHERE u.userId = :userId AND u.usageDate = :usageDate
            """)
    Optional<AiDailyUsage> findByUserIdAndUsageDateForUpdate(
            String userId,
            LocalDate usageDate
    );
}
