package com.lms.lms.controllers;

import com.lms.lms.dto.response.CertificateRes;
import com.lms.lms.dto.response.Default;
import com.lms.lms.modals.Certificate;
import com.lms.lms.repo.CertificateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/certificates")
public class CertificateController {

    @Autowired
    private CertificateRepo certificateRepo;

    @GetMapping("/{certificateId}")
    public ResponseEntity<Default> getCertificateById(@PathVariable String certificateId) {
        try {
            Certificate certificate = certificateRepo.findById(certificateId).orElse(null);
            if (certificate == null) {
                return new ResponseEntity<>(new Default("Certificate Not Found", false, null, null), HttpStatus.NOT_FOUND);
            }

            CertificateRes res = new CertificateRes(
                    certificate.getId(),
                    certificate.getCertificateNumber(),
                    certificate.getUser().getName(),
                    certificate.getCourse().getTitle(),
                    certificate.getIssuedAt()
            );
            return ResponseEntity.ok(new Default("Certificate Fetched Successfully", true, null, res));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
