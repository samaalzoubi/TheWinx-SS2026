package com.winx.rating.api;

import com.winx.rating.domain.exception.BookingNotCompletedException;
import com.winx.rating.domain.exception.BookingNotFoundException;
import com.winx.rating.domain.exception.DuplicateRatingException;
import com.winx.rating.domain.exception.InvalidScoreException;
import com.winx.rating.domain.exception.RatingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation failed");
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(InvalidScoreException.class)
    public ResponseEntity<Map<String, String>> handleInvalidScore(InvalidScoreException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookingNotFound(BookingNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RatingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRatingNotFound(RatingNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BookingNotCompletedException.class)
    public ResponseEntity<Map<String, String>> handleBookingNotCompleted(BookingNotCompletedException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DuplicateRatingException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateRating(DuplicateRatingException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    private ResponseEntity<Map<String, String>> errorResponse(HttpStatus status, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
