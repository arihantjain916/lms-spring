package com.lms.lms;

import com.lms.lms.dto.response.Default;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@RestController
@EnableScheduling
@EnableWebSocketMessageBroker
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

//    to return html file
//    @GetMapping("/")
//    public ResponseEntity<Resource> test() throws InaccessibleObjectException {
//        Resource resource = new ClassPathResource("static/index.html");
//        return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_HTML)
//                .body(resource);
//    }

    @GetMapping("/")
    public ResponseEntity<Default> index() {
        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        var date = istTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        return ResponseEntity.ok(new Default("Welcome to Learning Management System. Created By Arihant Jain", true, date, null));
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public void send(String message) throws Exception {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        System.out.println("messages: " + message);
//        return new OutputMessage(message.getFrom(), message.getText(), time);
    }

}
