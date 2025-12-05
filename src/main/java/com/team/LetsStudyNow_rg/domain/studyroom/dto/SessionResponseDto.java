package com.team.LetsStudyNow_rg.domain.studyroom.dto;

import com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 세션 응답 DTO
 */
@Builder
public record SessionResponseDto(
        Long sessionId,
        Long memberId,
        String studyType,
        Long roomId,
        String mode,
        Integer studyMinutes,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean isActive
) {
    public static SessionResponseDto from(StudySession session) {
        return SessionResponseDto.builder()
                .sessionId(session.getId())
                .memberId(session.getMember().getId())
                .studyType(session.getStudyType())
                .roomId(session.getRoomId())
                .mode(session.getMode())
                .studyMinutes(session.getStudyMinutes())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .isActive(session.isActive())
                .build();
    }
}
