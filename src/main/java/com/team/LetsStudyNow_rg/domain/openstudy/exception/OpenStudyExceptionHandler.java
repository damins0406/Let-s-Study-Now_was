package com.team.LetsStudyNow_rg.domain.openstudy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class OpenStudyExceptionHandler {
    
    @ExceptionHandler(RoomFullException.class)
    public ResponseEntity<?> handleRoomFull(RoomFullException e) {
        log.warn("방 정원 초과: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
    }
    
    @ExceptionHandler(AlreadyInRoomException.class)
    public ResponseEntity<?> handleAlreadyInRoom(AlreadyInRoomException e) {
        log.warn("중복 참여 시도: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
    }
    
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<?> handleRoomNotFound(RoomNotFoundException e) {
        log.warn("방을 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
    }
    
    @ExceptionHandler(RoomDeletingException.class)
    public ResponseEntity<?> handleRoomDeleting(RoomDeletingException e) {
        log.warn("삭제 예정 방 참여 시도: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.GONE)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청 파라미터: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
    }
}
