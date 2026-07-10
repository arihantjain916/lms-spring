package com.lms.lms.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.lms.modals.Enrollment;
import com.lms.lms.modals.Payments;
import com.lms.lms.repo.EnrollmentRepo;
import com.lms.lms.repo.PaymentRepo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
public class PaymentWebhookController {

    @Value("${payment.webhook-secret:}")
    private String webhookSecret;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> webhook(
            HttpServletRequest request,
            @RequestHeader("X-Webhook-Signature") String header
    ) {
        try {
            if (webhookSecret == null || webhookSecret.isBlank()) {
                return ResponseEntity.status(503).body("Webhook Secret Not Configured");
            }

            byte[] raw = request.getInputStream().readAllBytes();
            String rawBody = new String(raw, StandardCharsets.UTF_8);

            if (!verify(header, rawBody)) {
                return ResponseEntity.status(401).body("Invalid Signature");
            }

            JsonNode payload = objectMapper.readTree(rawBody);
            String event = payload.path("event").asText();
            String orderId = payload.path("orderId").asText();

            Payments payment = paymentRepo.findById(orderId).orElse(null);
            if (payment == null) {
                return ResponseEntity.badRequest().body("Order Not Found");
            }

            switch (event) {
                case "payment.success" -> {
                    if (payment.getStatus() != Payments.PaymentStatus.PAID) {
                        payment.setStatus(Payments.PaymentStatus.PAID);
                        paymentRepo.save(payment);

                        Boolean isUserAlreadyEnrolled = enrollmentRepo.existsByUser_IdAndCourses_Id(payment.getUser().getId(), payment.getCourse().getId());
                        if (!isUserAlreadyEnrolled) {
                            Enrollment enrollment = new Enrollment();
                            enrollment.setCourses(payment.getCourse());
                            enrollment.setUser(payment.getUser());
                            enrollmentRepo.save(enrollment);
                        }
                    }
                }
                case "payment.failed" -> {
                    if (payment.getStatus() == Payments.PaymentStatus.PENDING) {
                        payment.setStatus(Payments.PaymentStatus.FAILED);
                        paymentRepo.save(payment);
                    }
                }
                default -> {
                    return ResponseEntity.badRequest().body("Unknown Event");
                }
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private boolean verify(String header, String rawBody) {
        Map<String, String> map = Arrays.stream(header.split(","))
                .map(p -> p.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));

        long timestamp = Long.parseLong(map.get("t"));

        // replay protection
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
                    webhookSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
