package com.lms.lms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.lms.dto.response.ErrorRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorRes> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {

        ErrorRes errorRes = new ErrorRes(
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "The requested method is not allowed for the requested resource"
        );

        return new ResponseEntity<>(errorRes, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorRes> handleNotFound(NoHandlerFoundException ex) {

        ErrorRes errorRes = new ErrorRes(
                ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "The requested resource was not found"
        );

        return new ResponseEntity<>(errorRes, HttpStatus.NOT_FOUND);
    }


//    public void handleException(
//            HttpServletResponse response,
//            HttpStatus status,
//            String error,
//            String message
//    ) throws IOException {
//
//        ZonedDateTime istTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
//        var date = istTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
//        response.setStatus(status.value());
//        response.setContentType("application/json");
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("timestamp", date);
//        body.put("status", status.value());
//        body.put("error", error);
//        body.put("message", message);
//        response.getWriter().write(objectMapper.writeValueAsString(body));
//    }
}
