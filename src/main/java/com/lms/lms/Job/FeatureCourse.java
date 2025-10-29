package com.lms.lms.Job;

import com.lms.lms.repo.CourseStatsRepo;
import com.lms.lms.repo.CoursesRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Component
public class FeatureCourse {

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private CourseStatsRepo courseStatsRepo;

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Kolkata")
//    @Scheduled(fixedRate = 5000)
    @Transactional
    public void featureCourse() {

        ZoneId IST = ZoneId.of("Asia/Kolkata");


        ZonedDateTime startOfTodayIst = ZonedDateTime.now(IST).toLocalDate().atStartOfDay(IST);
        Instant toInstant = startOfTodayIst.toInstant();
        Instant fromInstant = startOfTodayIst.minusDays(4).toInstant();

        Date from = Date.from(fromInstant);
        Date to = Date.from(toInstant);
        List<Long> topIds = courseStatsRepo.topCourseIdsByViewsInWindow(from, to, PageRequest.of(0, Math.min(9, 4)));

        if (topIds.isEmpty()) return;

        coursesRepo.resetAllFeatured();
        coursesRepo.markFeaturedByIds(topIds);
    }
}
