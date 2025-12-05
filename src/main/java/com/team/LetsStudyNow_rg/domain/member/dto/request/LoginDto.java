package com.team.LetsStudyNow_rg.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "이메일을 입력해주세요.")
        String email,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
) {
}
