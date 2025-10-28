package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.RefreshToken;
import com.lms.lms.modals.User;
import com.lms.lms.repo.RefreshTokenRepo;
import com.lms.lms.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Default> generateToken(HttpServletRequest request, @CookieValue(name = "refresh") String token) {
        try {
            var Token = refreshTokenRepo.findByTokenOrderByCreatedAtDesc(token);

            if (Token == null) {
                return ResponseEntity.badRequest().body(new Default("Invalid Token", false, null, null));
            }
            if (Token.getExpiresAt().before(new Date()) || !Token.getClient().equals(request.getHeader("User-Agent")) || !Token.getIpaddress().equals(request.getRemoteAddr())) {
                return ResponseEntity.badRequest().body(new Default("Invalid Token", false, null, null));
            }
            var newToken = jwtService.generateToken(Token.getUser().getId());
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
}
