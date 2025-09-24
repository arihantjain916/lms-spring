package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.CourseStats;
import com.lms.lms.repo.CourseStatsRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.service.ViewerIdResolver;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/course")
public class CourseStatsController {

    private static final String GUEST_COOKIE = "guest_id";
    private static final int GUEST_COOKIE_MAX_AGE = 60 * 60 * 24 * 365;
    @Autowired
    private CourseStatsRepo courseStatsRepo;
    @Autowired
    private CoursesRepo coursesRepo;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private ViewerIdResolver viewerIdResolver;

    private static String getCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return null;
    }

    private static String extractIp(HttpServletRequest req) {
        String h = req.getHeader("X-Forwarded-For");
        if (h != null && !h.isBlank()) return h.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    @GetMapping("/{courseId}/view")
    public ResponseEntity<Default> save(@PathVariable Long courseId) {
        try {
            var isCourseExist = coursesRepo.findById(courseId).orElse(null);
            if (isCourseExist == null) {
                return ResponseEntity.ok().body(new Default("Course Not Found", false, null, null));
            }
            String guestCookie = getCookie(request, GUEST_COOKIE);
            var viewer = viewerIdResolver.currentViewer(guestCookie);

            CourseStats courseStats = new CourseStats();
            courseStats.setCourseId(isCourseExist);

            Boolean isUserExist = courseStatsRepo.existsByCourseId_IdAndUserId(courseId, viewer.id());
            if (isUserExist) {
                return ResponseEntity.ok().body(new Default("View Added Successfully", false, null, null));
            }

            if (!viewer.authenticated() && guestCookie == null) {
                Cookie c = new Cookie(GUEST_COOKIE, viewer.id());
                c.setHttpOnly(true);
                c.setSecure(true);
                c.setPath("/");
                c.setMaxAge(GUEST_COOKIE_MAX_AGE);
                response.addCookie(c);

                courseStats.setUserId(viewer.id());
                courseStats.setIp(extractIp(request));
                courseStatsRepo.save(courseStats);

                return ResponseEntity.ok().body(new Default("View Added Successfully", false, null, null));
            }


            courseStats.setUserId(viewer.id());
            courseStatsRepo.save(courseStats);
            return ResponseEntity.ok().body(new Default("View Added Successfully", false, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
