package com.lms.lms.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.UpdatePasswordReq;
import com.lms.lms.dto.request.UpdateUserReq;
import com.lms.lms.dto.response.CourseRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.EnrollmentDetailsRes;
import com.lms.lms.dto.response.MeRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.CourseMapper;
import com.lms.lms.dto.response.WishlistRes;
import com.lms.lms.modals.Courses;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.modals.Review;
import com.lms.lms.modals.User;
import com.lms.lms.modals.Wishlist;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.LessonProgressRepo;
import com.lms.lms.repo.PricingRepo;
import com.lms.lms.repo.QuestionHelpfulRepo;
import com.lms.lms.repo.RatingRepo;
import com.lms.lms.repo.ReviewRepo;
import com.lms.lms.repo.UserRepo;
import com.lms.lms.repo.VerificationTokenRepo;
import com.lms.lms.repo.WebinarRegistrationRepo;
import com.lms.lms.repo.WishlistRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private RefreshTokenController refreshTokenController;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private WishlistRepo wishlistRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private PricingRepo pricingRepo;

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private VerificationTokenRepo verificationTokenRepo;

    @Autowired
    private LessonProgressRepo lessonProgressRepo;

    @Autowired
    private WebinarRegistrationRepo webinarRegistrationRepo;

    @Autowired
    private QuestionHelpfulRepo questionHelpfulRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @PatchMapping("/me")
    public ResponseEntity<Default> updateMe(@Valid @RequestBody UpdateUserReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (req.getUsername() != null && !req.getUsername().isBlank() && !req.getUsername().equals(user.getUsername())) {
                User existing = userRepo.findByUsername(req.getUsername()).orElse(null);
                if (existing != null) {
                    return new ResponseEntity<>(new Default("Username Already Taken", false, null, null), HttpStatus.BAD_REQUEST);
                }
                user.setUsername(req.getUsername());
            }

            if (req.getName() != null && !req.getName().isBlank()) {
                user.setName(req.getName());
            }

            userRepo.save(user);
            MeRes res = new MeRes(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole().name(), user.getAvatar(), user.getIsVerified(), user.getCreatedAt());
            return ResponseEntity.ok(new Default("User Updated Successfully", true, null, res));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @PatchMapping("/me/password")
    public ResponseEntity<Default> updatePassword(@Valid @RequestBody UpdatePasswordReq req) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            if (!encoder.matches(req.getCurrentPassword(), user.getPassword())) {
                return new ResponseEntity<>(new Default("Current Password Is Incorrect", false, null, null), HttpStatus.BAD_REQUEST);
            }

            user.setPassword(encoder.encode(req.getNewPassword()));
            userRepo.save(user);
            refreshTokenController.deleteRefreshToken(user);
            return ResponseEntity.ok(new Default("Password Updated Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @PostMapping("/me/avatar")
    public ResponseEntity<Default> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            List<String> allowedTypes = Arrays.asList(
                    "image/png",
                    "image/jpeg",
                    "image/webp"
            );

            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                return ResponseEntity
                        .badRequest()
                        .body(new Default("Invalid file type. Allowed: PNG, JPG, JPEG, WEBP", false, null, null));
            }

            var res = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "/lms/avatars/"));

            user.setAvatar(res.get("secure_url").toString());
            userRepo.save(user);
            return ResponseEntity.ok(new Default("Avatar Uploaded Successfully", true, null, res.get("secure_url").toString()));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @DeleteMapping("/me")
    @Transactional
    public ResponseEntity<Default> deleteMe(HttpServletResponse response, HttpServletRequest request) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            // anonymize PII so the account is unrecoverable and the email/username become free again
            String anonymousId = java.util.UUID.randomUUID().toString().substring(0, 8);
            user.setName("deleted-user-" + anonymousId);
            user.setUsername("deleted-" + anonymousId);
            user.setEmail("deleted-" + anonymousId + "@deleted.local");
            user.setPassword(encoder.encode(java.util.UUID.randomUUID().toString()));
            user.setAvatar(null);
            user.setIsVerified(false);
            user.setIsDeleted(true);
            user.setIsActive(false);
            userRepo.save(user);

            // purge personal artifacts; content that other data depends on (payments, reviews,
            // questions, certificates, authored courses) stays attached to the anonymized user
            refreshTokenController.deleteRefreshToken(user);
            verificationTokenRepo.deleteByUser(user);
            wishlistRepo.deleteByUser_Id(user.getId());
            enrollmentRepo.deleteByUser_Id(user.getId());
            lessonProgressRepo.deleteByUser_Id(user.getId());
            webinarRegistrationRepo.deleteByUser_Id(user.getId());
            questionHelpfulRepo.deleteByUser_Id(user.getId());

            response.addCookie(this.deleteCookie("token", request.isSecure()));
            response.addCookie(this.deleteCookie("refresh", request.isSecure()));
            return ResponseEntity.ok(new Default("User Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/me/enrollments")
    public ResponseEntity<?> getMyEnrollments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("enrolledAt").descending());

            Page<Enrollment> enrollments = enrollmentRepo.findByUser_Id(user.getId(), pageable);
            List<EnrollmentDetailsRes> enrollmentList = enrollments
                    .stream()
                    .map(enrollment -> new EnrollmentDetailsRes(enrollment.getId(), enrollment.getEnrolledAt(), this.toCourseRes(enrollment.getCourses())))
                    .toList();

            PaginatedResponse<EnrollmentDetailsRes> paginatedResponse = new PaginatedResponse<>(
                    "Enrollments Fetched Successfully",
                    true,
                    enrollmentList,
                    enrollments.getNumber() + 1,
                    enrollments.getSize(),
                    enrollments.getTotalElements(),
                    enrollments.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/me/enrollments/{courseId}")
    public ResponseEntity<Default> getMyEnrollmentForCourse(@PathVariable Long courseId) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            Enrollment enrollment = enrollmentRepo.findByUser_IdAndCourses_Id(user.getId(), courseId).orElse(null);
            if (enrollment == null) {
                return new ResponseEntity<>(new Default("Enrollment Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            EnrollmentDetailsRes res = new EnrollmentDetailsRes(enrollment.getId(), enrollment.getEnrolledAt(), this.toCourseRes(enrollment.getCourses()));
            return ResponseEntity.ok(new Default("Enrollment Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @PostMapping("/me/wishlist/{courseId}")
    public ResponseEntity<Default> addToWishlist(@PathVariable Long courseId) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            Courses course = coursesRepo.findById(courseId).orElse(null);
            if (course == null) {
                return new ResponseEntity<>(new Default("Course Not Found", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Boolean isAlreadyWishlisted = wishlistRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (isAlreadyWishlisted) {
                return new ResponseEntity<>(new Default("Course Already In Wishlist", false, null, null), HttpStatus.BAD_REQUEST);
            }

            Wishlist wishlist = new Wishlist();
            wishlist.setCourses(course);
            wishlist.setUser(user);
            wishlistRepo.save(wishlist);

            return ResponseEntity.ok(new Default("Course Added To Wishlist Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @DeleteMapping("/me/wishlist/{courseId}")
    @Transactional
    public ResponseEntity<Default> removeFromWishlist(@PathVariable Long courseId) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            Boolean isWishlisted = wishlistRepo.existsByUser_IdAndCourses_Id(user.getId(), courseId);
            if (!isWishlisted) {
                return new ResponseEntity<>(new Default("Course Is Not In Wishlist", false, null, null), HttpStatus.BAD_REQUEST);
            }

            wishlistRepo.deleteByUser_IdAndCourses_Id(user.getId(), courseId);
            return ResponseEntity.ok(new Default("Course Removed From Wishlist Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/me/wishlist")
    public ResponseEntity<?> getMyWishlist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("addedAt").descending());

            Page<Wishlist> wishlist = wishlistRepo.findByUser_Id(user.getId(), pageable);
            List<WishlistRes> wishlistItems = wishlist
                    .stream()
                    .map(item -> new WishlistRes(item.getId(), item.getAddedAt(), this.toCourseRes(item.getCourses())))
                    .toList();

            PaginatedResponse<WishlistRes> paginatedResponse = new PaginatedResponse<>(
                    "Wishlist Fetched Successfully",
                    true,
                    wishlistItems,
                    wishlist.getNumber() + 1,
                    wishlist.getSize(),
                    wishlist.getTotalElements(),
                    wishlist.getTotalPages()
            );
            return ResponseEntity.ok().body(paginatedResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CourseRes toCourseRes(Courses course) {
        CourseRes dto = courseMapper.toDto(course);
        Double price = pricingRepo.getMinPlanPriceByCourseId(course.getId());
        Double avgRating = ratingRepo.avgRatingOfCourse(course.getId());
        Integer totalRating = ratingRepo.totalRatingofCourse(course.getId());
        Integer upCount = reviewRepo.countReviewByCourseIdAndVoteType(course.getId(), Review.VoteType.UPVOTE);
        Integer downCount = reviewRepo.countReviewByCourseIdAndVoteType(course.getId(), Review.VoteType.DOWNVOTE);
        dto.setPrice(price);
        dto.setAvgRating(avgRating);
        dto.setTotalRating(totalRating);
        dto.setUpvote(upCount);
        dto.setDownvote(downCount);
        return dto;
    }

    private Cookie deleteCookie(String name, boolean secure) {
        // must mirror the attributes used when setting, otherwise browsers ignore the deletion
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
        return cookie;
    }
}
