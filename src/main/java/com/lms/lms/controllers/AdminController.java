package com.lms.lms.controllers;

import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.ApplicationStatusReq;
import com.lms.lms.dto.request.UserRoleReq;
import com.lms.lms.dto.request.UserStatusReq;
import com.lms.lms.dto.response.AdminEnrollmentRes;
import com.lms.lms.dto.response.AdminUserRes;
import com.lms.lms.dto.response.CertificateRes;
import com.lms.lms.dto.response.ContactRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.OrderRes;
import com.lms.lms.dto.response.PaginatedResponse;
import com.lms.lms.mappers.BlogCommentMapper;
import com.lms.lms.mappers.RatingMapper;
import com.lms.lms.modals.Certificate;
import com.lms.lms.modals.ContactUs;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.modals.Payments;
import com.lms.lms.modals.User;
import com.lms.lms.repo.BlogCommentRepo;
import com.lms.lms.repo.BlogRepo;
import com.lms.lms.repo.CertificateRepo;
import com.lms.lms.repo.ContactRepo;
import com.lms.lms.repo.CoursesRepo;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.PaymentRepo;
import com.lms.lms.repo.ProgramRepo;
import com.lms.lms.repo.RatingRepo;
import com.lms.lms.repo.ReviewRepo;
import com.lms.lms.repo.TutorialRepo;
import com.lms.lms.repo.UserRepo;
import com.lms.lms.repo.WebinarRepo;
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
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private CertificateRepo certificateRepo;

    @Autowired
    private RatingRepo ratingRepo;

    @Autowired
    private ReviewRepo reviewRepo;

    @Autowired
    private BlogCommentRepo blogCommentRepo;

    @Autowired
    private CoursesRepo coursesRepo;

    @Autowired
    private ProgramRepo programRepo;

    @Autowired
    private WebinarRepo webinarRepo;

    @Autowired
    private TutorialRepo tutorialRepo;

    @Autowired
    private BlogRepo blogRepo;

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private BlogCommentMapper blogCommentMapper;

    @Autowired
    private UserDetails userDetails;

    // ---------------------------------------------------------------- Users

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            User.Role roleFilter = null;
            if (role != null && !role.isBlank()) {
                try {
                    roleFilter = User.Role.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Role. Allowed: STUDENT, INSTRUCTOR, ADMIN", false, null, null));
                }
            }

            String search = (q != null && !q.isBlank()) ? q : "%";
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<User> users = userRepo.adminSearch(search, roleFilter, pageable);
            List<AdminUserRes> userList = users.stream().map(this::toAdminUserRes).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Users Fetched Successfully", true, userList,
                    users.getNumber() + 1, users.getSize(), users.getTotalElements(), users.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Default> getUser(@PathVariable String userId) {
        try {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new Default("User Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(new Default("User Fetched Successfully", true, null, this.toAdminUserRes(user)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<Default> updateUserRole(@PathVariable String userId, @Valid @RequestBody UserRoleReq req) {
        try {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new Default("User Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User current = userDetails.userDetails();
            if (current != null && current.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(new Default("You Cannot Change Your Own Role", false, null, null));
            }

            User.Role role;
            try {
                role = User.Role.valueOf(req.getRole().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(new Default("Invalid Role. Allowed: STUDENT, INSTRUCTOR, ADMIN", false, null, null));
            }

            user.setRole(role);
            userRepo.save(user);
            return ResponseEntity.ok(new Default("User Role Updated Successfully", true, null, this.toAdminUserRes(user)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<Default> updateUserStatus(@PathVariable String userId, @Valid @RequestBody UserStatusReq req) {
        try {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new Default("User Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User current = userDetails.userDetails();
            if (current != null && current.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(new Default("You Cannot Change Your Own Status", false, null, null));
            }

            if (req.getIsActive() != null) {
                user.setIsActive(req.getIsActive());
            }
            if (req.getIsBanned() != null) {
                user.setIsBanned(req.getIsBanned());
            }
            userRepo.save(user);
            return ResponseEntity.ok(new Default("User Status Updated Successfully", true, null, this.toAdminUserRes(user)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Default> deleteUser(@PathVariable String userId) {
        try {
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return new ResponseEntity<>(new Default("User Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            User current = userDetails.userDetails();
            if (current != null && current.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(new Default("You Cannot Delete Your Own Account", false, null, null));
            }

            // soft-delete so authored content (courses, payments, certificates) stays intact
            user.setIsDeleted(true);
            user.setIsActive(false);
            userRepo.save(user);
            return ResponseEntity.ok(new Default("User Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // ------------------------------------------------------------- Contact

    @GetMapping("/contact")
    public ResponseEntity<?> getContactSubmissions(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            ContactUs.Department departmentFilter = null;
            if (department != null && !department.isBlank()) {
                try {
                    departmentFilter = ContactUs.Department.valueOf(department.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Department. Allowed: GENERAL, TECHNICAL, SALES, BILLING", false, null, null));
                }
            }

            ContactUs.Status statusFilter = null;
            if (status != null && !status.isBlank()) {
                try {
                    statusFilter = ContactUs.Status.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Status. Allowed: OPEN, IN_PROGRESS, RESOLVED", false, null, null));
                }
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<ContactUs> submissions = contactRepo.adminSearch(departmentFilter, statusFilter, pageable);
            List<ContactRes> submissionList = submissions.stream().map(this::toContactRes).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Contact Submissions Fetched Successfully", true, submissionList,
                    submissions.getNumber() + 1, submissions.getSize(), submissions.getTotalElements(), submissions.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @GetMapping("/contact/{id}")
    public ResponseEntity<Default> getContactSubmission(@PathVariable String id) {
        try {
            ContactUs submission = contactRepo.findById(id).orElse(null);
            if (submission == null) {
                return new ResponseEntity<>(new Default("Contact Submission Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(new Default("Contact Submission Fetched Successfully", true, null, this.toContactRes(submission)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @PatchMapping("/contact/{id}")
    public ResponseEntity<Default> updateContactStatus(@PathVariable String id, @Valid @RequestBody ApplicationStatusReq req) {
        try {
            ContactUs submission = contactRepo.findById(id).orElse(null);
            if (submission == null) {
                return new ResponseEntity<>(new Default("Contact Submission Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            ContactUs.Status status;
            try {
                status = ContactUs.Status.valueOf(req.getStatus().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(new Default("Invalid Status. Allowed: OPEN, IN_PROGRESS, RESOLVED", false, null, null));
            }

            submission.setStatus(status);
            contactRepo.save(submission);
            return ResponseEntity.ok(new Default("Contact Status Updated Successfully", true, null, this.toContactRes(submission)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/contact/{id}")
    public ResponseEntity<Default> deleteContactSubmission(@PathVariable String id) {
        try {
            if (!contactRepo.existsById(id)) {
                return new ResponseEntity<>(new Default("Contact Submission Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            contactRepo.deleteById(id);
            return ResponseEntity.ok(new Default("Contact Submission Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // -------------------------------------------------------------- Orders

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            Payments.PaymentStatus statusFilter = null;
            if (status != null && !status.isBlank()) {
                try {
                    statusFilter = Payments.PaymentStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest().body(new Default("Invalid Status. Allowed: PENDING, PAID, FAILED", false, null, null));
                }
            }

            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            Page<Payments> payments = statusFilter != null
                    ? paymentRepo.findByStatus(statusFilter, pageable)
                    : paymentRepo.findAll(pageable);
            List<OrderRes> orderList = payments.stream().map(this::toOrderRes).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Orders Fetched Successfully", true, orderList,
                    payments.getNumber() + 1, payments.getSize(), payments.getTotalElements(), payments.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // --------------------------------------------------------- Enrollments

    @GetMapping("/enrollments")
    public ResponseEntity<?> getEnrollments(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("enrolledAt").descending());

            Page<Enrollment> enrollments = courseId != null
                    ? enrollmentRepo.findByCourses_Id(courseId, pageable)
                    : enrollmentRepo.findAll(pageable);
            List<AdminEnrollmentRes> enrollmentList = enrollments.stream().map(this::toAdminEnrollmentRes).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Enrollments Fetched Successfully", true, enrollmentList,
                    enrollments.getNumber() + 1, enrollments.getSize(), enrollments.getTotalElements(), enrollments.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // -------------------------------------------------------- Certificates

    @GetMapping("/certificates")
    public ResponseEntity<?> getCertificates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("issuedAt").descending());

            Page<Certificate> certificates = certificateRepo.findAll(pageable);
            List<CertificateRes> certificateList = certificates.stream().map(this::toCertificateRes).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Certificates Fetched Successfully", true, certificateList,
                    certificates.getNumber() + 1, certificates.getSize(), certificates.getTotalElements(), certificates.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/certificates/{id}")
    public ResponseEntity<Default> revokeCertificate(@PathVariable String id) {
        try {
            if (!certificateRepo.existsById(id)) {
                return new ResponseEntity<>(new Default("Certificate Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            certificateRepo.deleteById(id);
            return ResponseEntity.ok(new Default("Certificate Revoked Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // ------------------------------------------------------------- Ratings

    @GetMapping("/ratings")
    public ResponseEntity<?> getRatings(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            var ratings = courseId != null
                    ? ratingRepo.findAllByCourseId(courseId, pageable)
                    : ratingRepo.findAll(pageable);
            var ratingList = ratings.stream().map(ratingMapper::toDto).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Ratings Fetched Successfully", true, ratingList,
                    ratings.getNumber() + 1, ratings.getSize(), ratings.getTotalElements(), ratings.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/ratings/{id}")
    public ResponseEntity<Default> deleteRating(@PathVariable String id) {
        try {
            if (!ratingRepo.existsById(id)) {
                return new ResponseEntity<>(new Default("Rating Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            ratingRepo.deleteById(id);
            return ResponseEntity.ok(new Default("Rating Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<Default> deleteReview(@PathVariable String id) {
        try {
            if (!reviewRepo.existsById(id)) {
                return new ResponseEntity<>(new Default("Review Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            reviewRepo.deleteById(id);
            return ResponseEntity.ok(new Default("Review Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // ------------------------------------------------------- Blog comments

    @GetMapping("/blog-comments")
    public ResponseEntity<?> getBlogComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            int pageNumber = page > 0 ? page - 1 : 0;
            Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by("createdAt").descending());

            var comments = blogCommentRepo.findAll(pageable);
            var commentList = comments.stream().map(blogCommentMapper::toDto).toList();

            return ResponseEntity.ok().body(new PaginatedResponse<>(
                    "Blog Comments Fetched Successfully", true, commentList,
                    comments.getNumber() + 1, comments.getSize(), comments.getTotalElements(), comments.getTotalPages()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    @DeleteMapping("/blog-comments/{id}")
    @Transactional
    public ResponseEntity<Default> deleteBlogComment(@PathVariable String id) {
        try {
            if (!blogCommentRepo.existsById(id)) {
                return new ResponseEntity<>(new Default("Comment Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }
            // remove direct replies first to avoid dangling parent references
            blogCommentRepo.deleteAll(blogCommentRepo.findByParent_Id(id));
            blogCommentRepo.deleteById(id);
            return ResponseEntity.ok(new Default("Comment Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // ----------------------------------------------------------- Dashboard

    @GetMapping("/dashboard")
    public ResponseEntity<Default> getDashboard() {
        try {
            Map<String, Object> stats = new LinkedHashMap<>();

            Map<String, Object> users = new LinkedHashMap<>();
            users.put("total", userRepo.count());
            users.put("students", userRepo.countByRole(User.Role.STUDENT));
            users.put("instructors", userRepo.countByRole(User.Role.INSTRUCTOR));
            users.put("admins", userRepo.countByRole(User.Role.ADMIN));
            stats.put("users", users);

            Map<String, Object> orders = new LinkedHashMap<>();
            orders.put("total", paymentRepo.count());
            orders.put("paid", paymentRepo.countByStatus(Payments.PaymentStatus.PAID));
            orders.put("pending", paymentRepo.countByStatus(Payments.PaymentStatus.PENDING));
            orders.put("failed", paymentRepo.countByStatus(Payments.PaymentStatus.FAILED));
            orders.put("revenue", paymentRepo.totalRevenue());
            stats.put("orders", orders);

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("courses", coursesRepo.count());
            content.put("programs", programRepo.count());
            content.put("webinars", webinarRepo.count());
            content.put("tutorials", tutorialRepo.count());
            content.put("blogs", blogRepo.count());
            stats.put("content", content);

            Map<String, Object> engagement = new LinkedHashMap<>();
            engagement.put("enrollments", enrollmentRepo.count());
            engagement.put("certificates", certificateRepo.count());
            engagement.put("ratings", ratingRepo.count());
            engagement.put("blogComments", blogCommentRepo.count());
            engagement.put("contactSubmissions", contactRepo.count());
            stats.put("engagement", engagement);

            return ResponseEntity.ok(new Default("Dashboard Stats Fetched Successfully", true, null, stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }

    // ------------------------------------------------------------- mappers

    private AdminUserRes toAdminUserRes(User user) {
        return new AdminUserRes(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getAvatar(),
                user.getIsVerified(),
                user.getIsBanned(),
                user.getIsActive(),
                user.getIsDeleted(),
                user.getCreatedAt()
        );
    }

    private ContactRes toContactRes(ContactUs contact) {
        return new ContactRes(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getSubject(),
                contact.getMessage(),
                contact.getDepartment() != null ? contact.getDepartment().name() : null,
                contact.getPhone(),
                contact.getStatus() != null ? contact.getStatus().name() : null,
                contact.getCreatedAt()
        );
    }

    private OrderRes toOrderRes(Payments payment) {
        return new OrderRes(
                payment.getId(),
                payment.getCourse() != null ? payment.getCourse().getId() : null,
                payment.getCourse() != null ? payment.getCourse().getTitle() : null,
                payment.getPricingPlan() != null ? payment.getPricingPlan().getId() : null,
                payment.getPricingPlan() != null ? payment.getPricingPlan().getTitle() : null,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus() != null ? payment.getStatus().name() : null,
                payment.getCreatedAt()
        );
    }

    private AdminEnrollmentRes toAdminEnrollmentRes(Enrollment enrollment) {
        User user = enrollment.getUser();
        return new AdminEnrollmentRes(
                enrollment.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                enrollment.getCourses() != null ? enrollment.getCourses().getId() : null,
                enrollment.getCourses() != null ? enrollment.getCourses().getTitle() : null,
                enrollment.getEnrolledAt()
        );
    }

    private CertificateRes toCertificateRes(Certificate certificate) {
        return new CertificateRes(
                certificate.getId(),
                certificate.getCertificateNumber(),
                certificate.getUser() != null ? certificate.getUser().getName() : null,
                certificate.getCourse() != null ? certificate.getCourse().getTitle() : null,
                certificate.getIssuedAt()
        );
    }
}
