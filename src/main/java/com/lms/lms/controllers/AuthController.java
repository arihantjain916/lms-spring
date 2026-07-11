package com.lms.lms.controllers;


import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.EmailReq;
import com.lms.lms.dto.request.Login;
import com.lms.lms.dto.request.Register;
import com.lms.lms.dto.request.ResetPasswordReq;
import com.lms.lms.dto.request.VerifyEmailReq;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LoginRes;
import com.lms.lms.dto.response.MeRes;
import com.lms.lms.modals.User;
import com.lms.lms.modals.VerificationToken;
import com.lms.lms.repo.UserRepo;
import com.lms.lms.repo.VerificationTokenRepo;
import com.lms.lms.service.EmailService;
import com.lms.lms.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenController refreshTokenController;

    @Autowired
    private UserDetails userDetails;

    @Autowired
    private VerificationTokenRepo verificationTokenRepo;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Default> register(@Valid @RequestBody Register register) {
        try {
            User username = userRepo.findByUsername(register.getUsername()).orElse(null);
            User email = userRepo.findByEmail(register.getEmail()).orElse(null);

            if (username != null || email != null) {
                return new ResponseEntity<>(new Default("User Already Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }
            User user = new User();

            var pass = encoder.encode(register.getPassword());
            user.setUsername(register.getUsername());
            user.setEmail(register.getEmail());
            user.setPassword(pass);
            user.setName(register.getName());
            // flush so constraint violations surface here, before any email goes out
            userRepo.saveAndFlush(user);

            String token = createVerificationToken(user, VerificationToken.TokenType.EMAIL_VERIFICATION, 1000L * 60 * 60 * 24);
            emailService.sendVerificationEmail(user.getEmail(), token);
            return ResponseEntity.ok(new Default("User Created Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> register(@Valid @RequestBody Login login, HttpServletResponse response, HttpServletRequest request) {
        try {
            User isUserExist = userRepo.findByUsername(login.getUsername())
                    .orElseGet(() -> userRepo.findByEmail(login.getUsername()).orElse(null));

            if (isUserExist == null) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            if (!isUserExist.getIsActive()) {
                return new ResponseEntity<>(new Default("User Is Not Active", false, null, null), HttpStatus.BAD_REQUEST);
            }

            if (isUserExist.getIsBanned()) {
                return new ResponseEntity<>(new Default("User Is Banned", false, null, null), HttpStatus.BAD_REQUEST);
            }

            if (isUserExist.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.BAD_REQUEST);
            }

            if (!isUserExist.getIsVerified()) {
                return new ResponseEntity<>(new Default("User Is Not Verified", false, null, null), HttpStatus.BAD_REQUEST);
            }


            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(isUserExist.getUsername(), login.getPassword()));

            if (auth.isAuthenticated()) {
                var token = jwtService.generateToken(isUserExist.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());
                var refreshToken = refreshTokenController.createRefreshToken(isUserExist, request.getRemoteAddr(), request.getHeader("User-Agent"));
                Cookie tokenCookie = this.setCookie("token", token, 60 * 60, request.isSecure());
                Cookie refreshCookie = this.setCookie("refresh", refreshToken, 60 * 60 * 24 * 30, request.isSecure());

                response.addCookie(tokenCookie);
                response.addCookie(refreshCookie);
                return ResponseEntity.ok(new LoginRes("User Login Successfully", true, token, refreshToken));
            }
            return new ResponseEntity<>(new Default("Invalid Credentials", false, null, null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @PostMapping("/logout")
    public ResponseEntity<Default> logout(HttpServletResponse response, HttpServletRequest request) {
        try {
            refreshTokenController.deleteRefreshToken(userDetails.userDetails());

            // always clear the cookies, even if no refresh token rows were found
            Cookie tokenCookie = this.deleteCookie("token", request.isSecure());
            Cookie refreshCookie = this.deleteCookie("refresh", request.isSecure());

            response.addCookie(tokenCookie);
            response.addCookie(refreshCookie);
            return new ResponseEntity<>(new Default("User Logout Successfully", true, null, null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT', 'INSTRUCTOR')")
    @GetMapping("/me")
    public ResponseEntity<Default> me() {
        try {
            User user = userDetails.userDetails();
            if (user == null || user.getIsDeleted()) {
                return new ResponseEntity<>(new Default("User Does Not Exists", false, null, null), HttpStatus.NOT_FOUND);
            }
            MeRes res = new MeRes(user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole().name(), user.getAvatar(), user.getIsVerified(), user.getCreatedAt());
            return ResponseEntity.ok(new Default("User Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Default> forgotPassword(@Valid @RequestBody EmailReq req) {
        try {
            User user = userRepo.findByEmail(req.getEmail()).orElse(null);
            if (user != null && !user.getIsDeleted() && !user.getIsBanned()) {
                verificationTokenRepo.deleteByUserAndType(user, VerificationToken.TokenType.PASSWORD_RESET);
                String token = createVerificationToken(user, VerificationToken.TokenType.PASSWORD_RESET, 1000L * 60 * 60);
                emailService.sendPasswordResetEmail(user.getEmail(), token);
            }
            return ResponseEntity.ok(new Default("If The Email Exists, A Password Reset Link Has Been Sent", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Default> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        try {
            VerificationToken token = verificationTokenRepo.findByTokenAndType(req.getToken(), VerificationToken.TokenType.PASSWORD_RESET).orElse(null);

            if (token == null || token.getExpiresAt().before(new Date())) {
                return new ResponseEntity<>(new Default("Invalid Or Expired Token", false, null, null), HttpStatus.BAD_REQUEST);
            }
            User user = token.getUser();
            user.setPassword(encoder.encode(req.getPassword()));
            userRepo.save(user);
            verificationTokenRepo.delete(token);
            refreshTokenController.deleteRefreshToken(user);
            return ResponseEntity.ok(new Default("Password Reset Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/verify-email")
    @Transactional
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailReq req, HttpServletResponse response, HttpServletRequest request) {
        try {
            VerificationToken token = verificationTokenRepo.findByTokenAndType(req.getToken(), VerificationToken.TokenType.EMAIL_VERIFICATION).orElse(null);

            if (token == null || token.getExpiresAt().before(new Date())) {
                return new ResponseEntity<>(new Default("Invalid Or Expired Token", false, null, null), HttpStatus.BAD_REQUEST);
            }
            User user = token.getUser();
            user.setIsVerified(true);
            userRepo.save(user);
            verificationTokenRepo.delete(token);

            // log the user in right away so they don't have to sign in after verifying
            if (!user.getIsBanned() && !user.getIsDeleted() && user.getIsActive()) {
                var jwtToken = jwtService.generateToken(user.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());
                var refreshToken = refreshTokenController.createRefreshToken(user, request.getRemoteAddr(), request.getHeader("User-Agent"));
                Cookie tokenCookie = this.setCookie("token", jwtToken, 60 * 60, request.isSecure());
                Cookie refreshCookie = this.setCookie("refresh", refreshToken, 60 * 60 * 24 * 30, request.isSecure());

                response.addCookie(tokenCookie);
                response.addCookie(refreshCookie);
                return ResponseEntity.ok(new LoginRes("Email Verified Successfully", true, jwtToken, refreshToken));
            }

            return ResponseEntity.ok(new Default("Email Verified Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/resend-verification")
    @Transactional
    public ResponseEntity<Default> resendVerification(@Valid @RequestBody EmailReq req) {
        try {
            User user = userRepo.findByEmail(req.getEmail()).orElse(null);
            if (user != null && !user.getIsDeleted() && !user.getIsBanned() && !user.getIsVerified()) {
                verificationTokenRepo.deleteByUserAndType(user, VerificationToken.TokenType.EMAIL_VERIFICATION);
                String token = createVerificationToken(user, VerificationToken.TokenType.EMAIL_VERIFICATION, 1000L * 60 * 60 * 24);
                emailService.sendVerificationEmail(user.getEmail(), token);
            }
            return ResponseEntity.ok(new Default("If The Email Exists And Is Not Verified, A Verification Link Has Been Sent", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createVerificationToken(User user, VerificationToken.TokenType type, long validForMs) {
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setType(type);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(new Date(System.currentTimeMillis() + validForMs));
        verificationTokenRepo.save(token);
        return token.getToken();
    }

    private Cookie setCookie(String name, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        cookie.setAttribute("SameSite", secure ? "None" : "Lax");
//        cookie.setAttribute("Secure", "true");
        return cookie;
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
