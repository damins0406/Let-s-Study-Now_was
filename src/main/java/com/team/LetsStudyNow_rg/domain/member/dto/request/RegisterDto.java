package com.team.LetsStudyNow_rg.domain.member.dto.request;

import jakarta.validation.constraints.*;

public record RegisterDto(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 2, max = 12, message = "아이디는 2~12자 내로 작성해주세요.")
        String username,
        // 비밀번호는 영문자, 숫자, 특수기호를 반드시 포함해야 한다.
        @NotBlank @Size(min = 6, max = 15, message = "비밀번호는 6~15자로 작성해주세요.")
        @Pattern(
                regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
                message = "비밀번호는 영문자, 숫자, 특수기호(@$!%*#?&)를 포함해야 합니다."
        )
        String password,

        @NotBlank
        String checkPassword,

        String studyField,
        String bio
) {
    @AssertTrue(message = "비밀번호를 확인하세요.")
    public boolean isCheckPw(){
        return password != null && password.equals(checkPassword);
    }
}
