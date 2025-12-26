package com.lms.lms.controllers;


import com.lms.lms.GlobalValue.UserDetails;
import com.lms.lms.dto.request.Login;
import com.lms.lms.dto.request.Register;
import com.lms.lms.dto.response.Default;
import com.lms.lms.dto.response.LoginRes;
import com.lms.lms.modals.User;
import com.lms.lms.repo.UserRepo;
import com.lms.lms.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @PostMapping("/register")
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
            userRepo.save(user);
            return ResponseEntity.ok(new Default("User Created Successfully", true, null, null));
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> register(@Valid @RequestBody Login login, HttpServletResponse response, HttpServletRequest request) {
        try {
            User isUserExist = userRepo.findByUsername(login.getUsername()).orElse(null);

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


            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));

            if (auth.isAuthenticated()) {
                var token = jwtService.generateToken(isUserExist.getId(), request.getHeader("User-Agent"), request.getRemoteAddr());
                var refreshToken = refreshTokenController.createRefreshToken(isUserExist, request.getRemoteAddr(), request.getHeader("User-Agent"));
                Cookie tokenCookie = this.setCookie("token", token, 60 * 60);
                Cookie refreshCookie = this.setCookie("refresh", refreshToken, 60 * 60 * 24 * 30);

                response.addCookie(tokenCookie);
                response.addCookie(refreshCookie);
                return ResponseEntity.ok(new LoginRes("User Login Successfully", true, token, refreshToken));
            }
            return new ResponseEntity<>(new Default("Invalid Credentials", false, null, null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'INSTRUCTOR')")
    @GetMapping("/logout")
    public ResponseEntity<Default> logout(HttpServletResponse response) {
        try {
            Boolean isDelete = refreshTokenController.deleteRefreshToken(userDetails.userDetails());
            if (isDelete) {
                Cookie tokenCookie = this.deleteCookie("token");
                Cookie refreshCookie = this.deleteCookie("refresh");

                response.addCookie(tokenCookie);
                response.addCookie(refreshCookie);
                return new ResponseEntity<>(new Default("User Logout Successfully", true, null, null), HttpStatus.OK);
            }
            return new ResponseEntity<>(new Default("User Logout Failed", false, null, null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new Default(e.getMessage(), false, null, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Cookie setCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
//        cookie.setAttribute("Secure", "true");
        return cookie;
    }

    private Cookie deleteCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(0);
        return cookie;
    }
}
