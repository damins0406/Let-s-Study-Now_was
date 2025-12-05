package com.team.LetsStudyNow_rg.domain.member.dto.response;

public record ProfileDto(
        Long id,
        String email,
        String username,
        String profileImage,
        String studyField,
        String bio,
        Integer level
) {
}
