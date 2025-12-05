package com.team.LetsStudyNow_rg.domain.studyroom.exception;

/**
 * 세션을 찾을 수 없을 때 발생하는 예외
 */
public class SessionNotFoundException extends RuntimeException {
    
    public SessionNotFoundException(Long sessionId) {
        super("세션을 찾을 수 없습니다. 세션 ID: " + sessionId);
    }
    
    public SessionNotFoundException(String message) {
        super(message);
    }
}
