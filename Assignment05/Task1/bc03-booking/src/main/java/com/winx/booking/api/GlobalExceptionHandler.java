package com.winx.booking.api;

import com.winx.booking.api.dto.ErrorResponse;
import com.winx.booking.domain.exception.ActiveBookingExistsException;
import com.winx.booking.domain.exception.BookingNotFoundException;
import com.winx.booking.domain.exception.InvalidBookingStateException;
import com.winx.booking.domain.exception.RestrictionViolationException;
import com.winx.booking.domain.exception.UserNotFoundException;
import com.winx.booking.domain.exception.VehicleNotFoundException;
import com.winx.booking.domain.exception.VehicleUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message.isBlank() ? "Validation failed" : message));
    }

    @ExceptionHandler({UserNotFoundException.class, VehicleNotFoundException.class, BookingNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({VehicleUnavailableException.class, ActiveBookingExistsException.class, InvalidBookingStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(RestrictionViolationException.class)
    public ResponseEntity<ErrorResponse> handleRestrictionViolation(RestrictionViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }
}
