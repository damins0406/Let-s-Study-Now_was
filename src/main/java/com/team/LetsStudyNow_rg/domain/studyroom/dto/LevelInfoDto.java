package com.team.LetsStudyNow_rg.domain.studyroom.dto;

import lombok.Builder;

/**
 * 레벨 정보 DTO
 */
@Builder
public record LevelInfoDto(
        Long memberId,              // 회원 ID
        String username,            // 회원 이름
        Integer currentLevel,       // 현재 레벨
        Integer totalExp,           // 총 누적 경험치 (분)
        Integer currentLevelExp,    // 현재 레벨에서 획득한 경험치 (분)
        Integer requiredExpForNextLevel, // 다음 레벨까지 필요한 총 경험치 (분)
        Integer remainingExp,       // 다음 레벨까지 남은 경험치 (분)
        Double progress             // 현재 레벨 진행률 (0~100%)
) {
}
