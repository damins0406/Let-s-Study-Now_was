package com.team.LetsStudyNow_rg.domain.openstudy.exception;

/**
 * 중복 참여 예외
 * 
 * 발생 상황:
 * 1. 이미 다른 활성 방에 참여 중인데 새 방 생성 시도
 * 2. 이미 다른 활성 방에 참여 중인데 다른 방 참여 시도
 * 3. 이미 참여 중인 방에 다시 참여 시도
 * 
 * 비즈니스 규칙:
 * - 한 명의 사용자는 동시에 하나의 활성 방에만 참여 가능
 * - 참여 전에 반드시 기존 방에서 나가야 함
 */
public class AlreadyInRoomException extends RuntimeException {
    /**
     * 기본 메시지로 예외 생성
     */
    public AlreadyInRoomException() {
        super("이미 다른 스터디룸에 참여 중입니다");
    }
    
    /**
     * 커스텀 메시지로 예외 생성
     * 
     * @param message 상황에 맞는 구체적인 메시지
     */
    public AlreadyInRoomException(String message) {
        super(message);
    }
}
