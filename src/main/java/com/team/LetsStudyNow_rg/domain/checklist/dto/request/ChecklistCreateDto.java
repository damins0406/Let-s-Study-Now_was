package com.team.LetsStudyNow_rg.domain.checklist.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ChecklistCreateDto(
        @NotNull(message = "날짜를 선택해야 합니다.")
        LocalDate targetDate,

        @NotBlank(message = "내용을 입력해주세요")
        String content
) {
}
