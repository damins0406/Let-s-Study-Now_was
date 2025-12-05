package com.team.LetsStudyNow_rg.domain.openstudy.exception;

/**
 * 방 정원 초과 예외
 * 
 * 발생 상황:
 * - currentParticipants >= maxParticipants 상태에서 참여 시도
 * - 동시 접속으로 인한 정원 초과 (Race Condition)
 * 
 * 비즈니스 규칙:
 * - 각 방은 생성 시 설정한 최대 인원(2~10명)을 초과할 수 없음
 * - 정원 초과 방은 목록에서 참여 버튼이 비활성화되어야 함
 */
public class RoomFullException extends RuntimeException {
    /**
     * 기본 메시지로 예외 생성
     */
    public RoomFullException() {
        super("방 인원이 꽉 찼습니다");
    }
    
    /**
     * 커스텀 메시지로 예외 생성
     * 
     * @param message 상황에 맞는 구체적인 메시지
     */
    public RoomFullException(String message) {
        super(message);
    }
}
