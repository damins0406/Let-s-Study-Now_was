package com.team.LetsStudyNow_rg.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeDto(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        // 비밀번호는 영문자, 숫자, 특수기호를 반드시 포함해야 한다.
        @NotBlank @Size(min = 6, max = 15, message = "비밀번호는 6~15자로 작성해주세요.")
        @Pattern(
                regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
                message = "비밀번호는 영문자, 숫자, 특수기호(@$!%*#?&)를 포함해야 합니다."
        )
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String newPasswordCheck

) {
}
