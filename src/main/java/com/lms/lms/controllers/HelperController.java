package com.lms.lms.controllers;

import com.lms.lms.dto.response.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

@RestController
public class HelperController {

    @PostMapping("/upload")
    public ResponseEntity<Default> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            List<String> allowedTypes = Arrays.asList(
                    "application/pdf",
                    "image/png",
                    "image/jpeg",
                    "audio/mpeg",
                    "audio/mpeg3"
            );

            String contentType = file.getContentType();
            if (contentType == null || !allowedTypes.contains(contentType)) {
                return ResponseEntity
                        .badRequest()
                        .body(new Default("Invalid file type. Allowed: PDF, PNG, JPG, JPEG, MP3, MPEG", false, null, null));
            }
            String filePath = System.getProperty("user.dir") + "/Uploads" + File.separator + file.getOriginalFilename();
            FileOutputStream fout = new FileOutputStream(filePath);
            fout.write(file.getBytes());

            // Closing the connection
            fout.close();

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/Uploads/")
                    .path(file.getOriginalFilename())
                    .toUriString();
            return ResponseEntity.ok().body(new Default("File Uploaded Successfully", true, null, fileUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new Default(e.getMessage(), false, null, null));
        }
    }
}
