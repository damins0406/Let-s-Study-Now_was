package com.team.LetsStudyNow_rg.domain.checklist.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChecklistUpdateDto
        (
                @NotBlank(message = "내용을 입력해주세요.")
                String content
        ) {
}

