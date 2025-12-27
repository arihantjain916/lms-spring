package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.RefreshToken;
import com.lms.lms.modals.User;
import com.lms.lms.repo.RefreshTokenRepo;
import com.lms.lms.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/auth/refresh")
public class RefreshTokenController {

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Autowired
    private JwtService jwtService;


    @GetMapping("")
    public ResponseEntity<Default> generateToken(HttpServletRequest request, @CookieValue(name = "refresh") String token, HttpServletResponse response) {
        try {
            var Token = refreshTokenRepo.findByTokenOrderByCreatedAtDesc(token);

            if (Token == null) {
                Cookie refreshCookie = this.deleteCookie();
                response.addCookie(refreshCookie);
                return new ResponseEntity<>(new Default("Invalid Token", false, null, null), HttpStatus.UNAUTHORIZED);
            }
            if (Token.getExpiresAt().before(new Date()) || !Token.getClient().equals(request.getHeader("User-Agent")) || !Token.getIpaddress().equals(request.getRemoteAddr())) {
                Cookie refreshCookie = this.deleteCookie();
                response.addCookie(refreshCookie);
                return new ResponseEntity<>(new Default("Invalid Token", false, null, null), HttpStatus.UNAUTHORIZED);
            }
            var newToken = jwtService.generateToken(Token.getUser().getId(), request.getHeader("User-Agent"), request.getRemoteAddr());

            Cookie tokenCookie = this.setCookie(newToken, 60 * 60);
            response.addCookie(tokenCookie);
            return ResponseEntity.ok().body(new Default("Token Generated Successfully", true, null, newToken));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), null, null, null));
        }
    }


    public String createRefreshToken(User user, String ipAddress, String header) {
        var token = new RefreshToken();
        token.setUser(user);
        token.setExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30));
        token.setToken(UUID.randomUUID().toString());
        token.setClient(header);
        token.setIpaddress(ipAddress);
        refreshTokenRepo.save(token);
        return token.getToken();
    }

    public Boolean deleteRefreshToken(User user) {
        try {
            int cont = refreshTokenRepo.deleteByUser(user);
            return cont > 0;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    private Cookie setCookie(String value, int maxAge) {
        Cookie cookie = new Cookie("token", value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
//        cookie.setAttribute("Secure", "true");
        return cookie;
    }

    private Cookie deleteCookie() {
        Cookie cookie = new Cookie("refresh", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "None");
//        cookie.setAttribute("Secure", "true");
        return cookie;
    }
}
