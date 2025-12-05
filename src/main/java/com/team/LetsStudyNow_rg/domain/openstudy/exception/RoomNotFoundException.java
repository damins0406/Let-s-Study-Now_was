package com.team.LetsStudyNow_rg.domain.openstudy.exception;

/**
 * 방 미발견 예외
 * 
 * 발생 상황:
 * 1. 존재하지 않는 방 ID로 조회/참여 시도
 * 2. 이미 DELETED 상태인 방에 접근 시도
 * 3. DB에서 방이 물리적으로 삭제된 경우
 * 
 * HTTP 상태: 404 Not Found
 */
public class RoomNotFoundException extends RuntimeException {
    /**
     * 기본 메시지로 예외 생성
     */
    public RoomNotFoundException() {
        super("방을 찾을 수 없습니다");
    }
    
    /**
     * 커스텀 메시지로 예외 생성
     * 
     * @param message 상황에 맞는 구체적인 메시지
     *                예: "이미 삭제된 방입니다"
     */
    public RoomNotFoundException(String message) {
        super(message);
    }
}
