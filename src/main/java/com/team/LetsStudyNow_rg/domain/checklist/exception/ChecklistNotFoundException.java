package com.team.LetsStudyNow_rg.domain.checklist.exception;

public class ChecklistNotFoundException extends RuntimeException {
    public ChecklistNotFoundException(Long id) {
        super("해당 체크리스트(ID: " + id + ")를 찾을 수 없거나 접근 권한이 없습니다.");
    }
}