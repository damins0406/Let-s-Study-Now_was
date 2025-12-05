package com.team.LetsStudyNow_rg.domain.checklist.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.team.LetsStudyNow_rg.domain.checklist")
public class ChecklistExceptionHandler {

    @ExceptionHandler(ChecklistNotFoundException.class)
    public ResponseEntity<String> handleChecklistNotFound(ChecklistNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}