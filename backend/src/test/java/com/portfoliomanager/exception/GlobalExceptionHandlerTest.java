package com.portfoliomanager.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Investment not found");

        ResponseEntity<ErrorResponse> result = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("Not Found", result.getBody().getError());
        assertEquals("Investment not found", result.getBody().getMessage());
        assertEquals(404, result.getBody().getStatus());
        assertNotNull(result.getBody().getTimestamp());
    }

    @Test
    void handleValidationExceptions() {
        org.springframework.core.MethodParameter parameter = mock(org.springframework.core.MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "name", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> result = handler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Validation Failed", result.getBody().getError());
        assertTrue(result.getBody().getMessage().contains("name: must not be blank"));
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid value");

        ResponseEntity<ErrorResponse> result = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Bad Request", result.getBody().getError());
        assertEquals("Invalid value", result.getBody().getMessage());
    }

    @Test
    void handleTypeMismatch() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ErrorResponse> result = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().getMessage().contains("id"));
    }

    @Test
    void handleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("homeCurrency");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be 3 letters");

        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        Set<ConstraintViolation<?>> violations = new java.util.HashSet<>();
        violations.add(violation);
        when(ex.getConstraintViolations()).thenReturn(violations);

        ResponseEntity<ErrorResponse> result = handler.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertTrue(result.getBody().getMessage().contains("homeCurrency"));
    }

    @Test
    void handleGeneralException() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> result = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Internal Server Error", result.getBody().getError());
    }

    @Test
    void errorResponse_GettersAndSetters() {
        ErrorResponse error = new ErrorResponse("Error", "msg", 500);
        assertEquals("Error", error.getError());
        assertEquals("msg", error.getMessage());
        assertEquals(500, error.getStatus());
        assertNotNull(error.getTimestamp());

        error.setError("Updated");
        error.setMessage("new msg");
        error.setStatus(400);
        assertEquals("Updated", error.getError());
        assertEquals("new msg", error.getMessage());
        assertEquals(400, error.getStatus());
    }
}
