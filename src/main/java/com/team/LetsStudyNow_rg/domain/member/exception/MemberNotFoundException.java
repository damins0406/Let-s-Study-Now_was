package com.team.LetsStudyNow_rg.domain.member.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long id) {
        super("해당 사용자(ID: " + id + ")를 찾을 수 없습니다.");
    }
}