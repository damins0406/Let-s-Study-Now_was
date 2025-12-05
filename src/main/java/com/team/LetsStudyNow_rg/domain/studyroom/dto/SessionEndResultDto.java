package com.team.LetsStudyNow_rg.domain.studyroom.dto;

import lombok.Builder;

/**
 * 세션 종료 결과 DTO
 */
@Builder
public record SessionEndResultDto(
        Long sessionId,         // 세션 ID
        Integer studyMinutes,   // 총 공부 시간 (분)
        Boolean leveledUp,      // 레벨업 여부
        Integer newLevel        // 새 레벨 (레벨업 안했으면 null)
) {
}
