package com.lms.lms;

import com.lms.lms.dto.response.Default;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@SpringBootApplication
@RestController
@EnableScheduling
public class LearningManagementSystemApplication {

	public static void main(String[] args) {

        TimeZone.setDefault(TimeZone.getTimeZone("GMT+05:30"));

        SpringApplication.run(LearningManagementSystemApplication.class, args);
	}

    @GetMapping("/health")
    public ResponseEntity<Default> health(){
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        var date = istTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        return ResponseEntity.ok(new Default("Application is running", true, date, null));
    }


    @GetMapping("/")
    public ResponseEntity<Default> index() {
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        var date = istTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        return ResponseEntity.ok(new Default("Welcome to Learning Management System. Created By Arihant Jain", true, date, null));
    }

}
