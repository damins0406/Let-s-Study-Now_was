package com.team.LetsStudyNow_rg.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AccountDeleteDto(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
