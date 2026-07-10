package com.lms.lms.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.UpdatePasswordReq;
import com.lms.lms.dto.request.UpdateUserReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.MeRes;
import com.lms.lms.modals.User;
import com.lms.lms.repo.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Default> deleteMe(HttpServletResponse response) {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }

            user.setIsDeleted(true);
            user.setIsActive(false);
            userRepo.save(user);
            refreshTokenController.deleteRefreshToken(user);

            response.addCookie(this.deleteCookie("token"));
            response.addCookie(this.deleteCookie("refresh"));
            return ResponseEntity.ok(new Default("User Deleted Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
        return cookie;
    }
}
