package com.team.LetsStudyNow_rg.domain.openstudy.dto;

import com.team.LetsStudyNow_rg.domain.openstudy.OpenStudyRoom;

/**
 * 오픈 스터디 방 목록/상세 조회 응답 DTO
 * 클라이언트에게 방 정보를 전달할 때 사용
 * 
 * 사용처:
 * - GET /api/open-study/rooms: 방 목록 조회
 * - GET /api/open-study/rooms/{roomId}: 방 상세 조회
 * 
 * 엔티티 직접 노출 방지:
 * - OpenStudyRoom 엔티티를 직접 반환하지 않고 DTO로 변환
 * - 필요한 정보만 선택적으로 전달
 * - 순환 참조 문제 방지 (OpenStudyRoom ↔ RoomParticipant)
 */
public record OpenStudyRoomListDto(
    // 방 ID
    Long id,
    
    // 방 제목
    String title,
    
    // 방 설명
    String description,
    
    // 공부 분야
    String studyField,
    
    // 현재 참여 인원
    int currentParticipants,
    
    // 최대 인원
    int maxParticipants,
    
    // 정원 초과 여부 (프론트엔드에서 참여 버튼 비활성화용)
    boolean isFull,
    
    // 방 생성자의 아이디 (username)
    String creatorUsername
) {
    /**
     * OpenStudyRoom 엔티티를 DTO로 변환하는 정적 팩토리 메서드
     * 
     * @param room OpenStudyRoom 엔티티
     * @return 변환된 DTO
     */
    public static OpenStudyRoomListDto from(OpenStudyRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("room이 null입니다");
        }
        if (room.getStudyField() == null) {
            throw new IllegalStateException("room.studyField가 null입니다. roomId: " + room.getId());
        }
        if (room.getCreator() == null) {
            throw new IllegalStateException("room.creator가 null입니다. roomId: " + room.getId());
        }
        
        return new OpenStudyRoomListDto(
            room.getId(),
            room.getTitle(),
            room.getDescription(),
            room.getStudyField().getDescription(),
            room.getCurrentParticipants(),
            room.getMaxParticipants(),
            room.isFull(),
            room.getCreator().getUsername()
        );
    }
}
