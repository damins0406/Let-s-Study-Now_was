package com.team.LetsStudyNow_rg.domain.openstudy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 오픈 스터디 방의 공부 분야를 나타내는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum StudyField {
    PROGRAMMING("프로그래밍"),
    ENGLISH("영어"),
    CERTIFICATION("자격증"),
    CIVIL_SERVICE("공무원"),
    UNIVERSITY_ENTRANCE("대학입시"),
    JOB_PREPARATION("취업준비"),
    LANGUAGE("어학"),
    ETC("기타");
    
    private final String description;
    
    /**
     * StudyField를 찾는 메서드
     * @param description 공부 분야 한글 설명 (예: "프로그래밍", "영어")
     * @return 매칭되는 StudyField, 없으면 null
     */
    public static StudyField fromDescription(String description) {
        if (description == null) {
            return null;
        }
        
        for (StudyField field : values()) {
            if (field.description.equals(description)) {
                return field;
            }
        }
        
        return null;
    }
}
