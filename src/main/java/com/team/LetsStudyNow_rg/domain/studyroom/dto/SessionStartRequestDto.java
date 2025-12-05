package com.team.LetsStudyNow_rg.domain.studyroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 세션 시작 요청 DTO
 */
@Schema(description = "공부 세션 시작 요청")
public record SessionStartRequestDto(
        @Schema(
            description = "스터디 타입",
            example = "OPEN_STUDY",
            allowableValues = {"OPEN_STUDY", "GROUP_STUDY"},
            required = true
        )
        @NotBlank(message = "스터디 타입은 필수입니다")
        String studyType,
        
        @Schema(
            description = "스터디방 ID",
            example = "1",
            required = true
        )
        @NotNull(message = "방 ID는 필수입니다")
        Long roomId
) {
}
