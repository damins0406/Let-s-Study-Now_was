package com.team.LetsStudyNow_rg.domain.studyroom.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * StudyRoom 도메인 예외 처리 핸들러
 */
@RestControllerAdvice
@Slf4j
public class StudyRoomExceptionHandler {
    
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSessionNotFoundException(SessionNotFoundException e) {
        log.error("SessionNotFoundException: {}", e.getMessage());
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "SESSION_NOT_FOUND");
        errorResponse.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage());
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "INVALID_STATE");
        errorResponse.put("message", e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
