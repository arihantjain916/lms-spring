package com.lms.lms.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    private String key = "jkbcidbc!@#$%^&1541AFVGYVASDFGHJKL@#$%";

    @Autowired
    private EncryptionService encryptionService;

    public String generateToken(String id){

        String encryptedId = encryptionService.encrypt(id);
        return Jwts
                .builder()
                .claim("id", encryptedId)
                .subject("user")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(generateKey())
                .compact();
    }

    private SecretKey generateKey(){
        byte[] keyBytes = key.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String extractUsername(String token) {
        String id = extractClaim(token, claims -> claims.get("id", String.class));
        return encryptionService.decrypt(id);
//        return extractClaim(token, claims -> claims.get("id", String.class));
    }

    public boolean validateToken(String token, UserDetails userDetails) {

        final String id = extractUsername(token);
        return (id.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private  boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
