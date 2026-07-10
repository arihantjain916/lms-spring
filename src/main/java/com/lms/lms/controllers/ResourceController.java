package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.LessonResource;
import com.lms.lms.modals.User;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.LessonResourceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private LessonResourceRepo lessonResourceRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private UserDetails userDetails;

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Default> downloadResource(@PathVariable String resourceId) {
        try {
            LessonResource resource = lessonResourceRepo.findById(resourceId).orElse(null);
            if (resource == null) {
                return new ResponseEntity<>(new Default("Resource Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User user = userDetails.userDetails();
            if (!this.canAccessCourse(user, resource.getLesson().getCourses())) {
                return new ResponseEntity<>(new Default("User Is Not Enrolled In This Course", false, null, null), HttpStatus.FORBIDDEN);
            }

            return ResponseEntity.ok(new Default("Resource Download Url Fetched Successfully", true, null, resource.getUrl()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    private boolean canAccessCourse(User user, Courses course) {
        if (user == null || user.getIsDeleted()) {
            return false;
        }
        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }
        if (course.getUser() != null && course.getUser().getId().equals(user.getId())) {
            return true;
        }
        return enrollmentRepo.existsByUser_IdAndCourses_Id(user.getId(), course.getId());
    }
}
