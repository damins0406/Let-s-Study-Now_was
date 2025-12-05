package com.team.LetsStudyNow_rg.domain.checklist.dto.response;

import java.time.LocalDate;

public record ChecklistResponseDto(
        Long id,
        LocalDate targetDate,
        String content,
        boolean isCompleted
) {
}
