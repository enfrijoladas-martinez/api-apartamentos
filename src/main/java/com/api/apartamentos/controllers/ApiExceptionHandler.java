package com.api.apartamentos.controllers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        FieldError field = exception.getBindingResult().getFieldError();
        return response(HttpStatus.BAD_REQUEST, field == null ? "Solicitud inválida" : field.getField() + ": " + field.getDefaultMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException exception) { return response(HttpStatus.BAD_REQUEST, exception.getMessage()); }
    // JSON mal formado, fecha inválida o valor que no existe en un enum
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> badJson(HttpMessageNotReadableException exception) {
        return response(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido (revisa el JSON, las fechas y los valores permitidos)");
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, Object>> badParam(MethodArgumentTypeMismatchException exception) {
        return response(HttpStatus.BAD_REQUEST, "Valor no válido para '" + exception.getName() + "': " + exception.getValue());
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, Object>> notFound(NoSuchElementException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage() == null ? "El recurso no existe" : exception.getMessage());
    }
    // Email o INE repetido, o un registro que otros usan (no se puede borrar)
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, Object>> conflict(DataIntegrityViolationException exception) {
        return response(HttpStatus.CONFLICT, "Conflicto con datos existentes: un valor único ya está registrado o el registro está en uso");
    }
    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("timestamp", LocalDateTime.now(), "status", status.value(), "error", message));
    }
}
