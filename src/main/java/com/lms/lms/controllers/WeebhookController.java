package com.lms.lms.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/webhook")
public class WeebhookController {
    private static final String SECRET = "whsec_6a705f921c0e9703d1ebee0df6f316de";

    @PostMapping()
    public ResponseEntity<String> webhook(
            HttpServletRequest request,
            @RequestHeader("X-Webhook-Signature") String header
    ) throws IOException {
        byte[] raw = request.getInputStream().readAllBytes();
        String rawBody = new String(raw, StandardCharsets.UTF_8);

        if (!verify(header, rawBody)) {
            return ResponseEntity.status(401).body("Invalid");
        }

        System.out.println("Verified payload: " + rawBody);
        return ResponseEntity.ok("OK");
    }

    private boolean verify(String header, String rawBody) {
        Map<String, String> map = Arrays.stream(header.split(","))
                .map(p -> p.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        long timestamp = Long.parseLong(map.get("t"));

        // optional replay protection
        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > 300) {
            return false;
        }

        String signedPayload = timestamp + "." + rawBody;
        String expected = hmac(signedPayload);

        return MessageDigest.isEqual(
                expected.getBytes(),
                map.get("v1").getBytes()
        );
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    SECRET.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
