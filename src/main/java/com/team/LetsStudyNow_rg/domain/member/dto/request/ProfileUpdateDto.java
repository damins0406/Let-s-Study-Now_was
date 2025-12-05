package com.team.LetsStudyNow_rg.domain.member.dto.request;

import jakarta.validation.constraints.Size;

public record ProfileUpdateDto(
        String studyField,
        @Size(max = 200, message = "자기소개는 200자 이내로 작성해주세요.")
        String bio
) {
}
