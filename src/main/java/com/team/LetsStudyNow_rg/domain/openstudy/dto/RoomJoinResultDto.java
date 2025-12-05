package com.team.LetsStudyNow_rg.domain.openstudy.dto;

/**
 * 방 참여 결과 응답 DTO
 * POST /api/open-study/rooms/{roomId}/join 응답용
 * 
 * 성공/실패 여부와 메시지, 방 ID를 함께 전달
 * 정적 팩토리 메서드를 통해 일관된 응답 형식 제공
 */
public record RoomJoinResultDto(
    // 성공 여부 (true: 성공, false: 실패)
    boolean success,
    
    // 결과 메시지 (예: "방에 입장했습니다")
    String message,
    
    // 참여한 방의 ID (실패 시 null)
    Long roomId
) {
    /**
     * 성공 응답 생성
     * 
     * @param roomId 참여한 방의 ID
     * @return 성공 응답 DTO
     */
    public static RoomJoinResultDto success(Long roomId) {
        return new RoomJoinResultDto(true, "방에 입장했습니다", roomId);
    }
    
    /**
     * 실패 응답 생성
     * 
     * @param message 실패 사유 메시지
     * @return 실패 응답 DTO (roomId는 null)
     */
    public static RoomJoinResultDto fail(String message) {
        return new RoomJoinResultDto(false, message, null);
    }
}
