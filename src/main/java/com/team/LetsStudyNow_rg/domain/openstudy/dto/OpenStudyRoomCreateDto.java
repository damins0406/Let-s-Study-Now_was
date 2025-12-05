package com.team.LetsStudyNow_rg.domain.openstudy.dto;

import jakarta.validation.constraints.*;

/**
 * 오픈 스터디 방 생성 요청 DTO
 * 클라이언트가 방을 생성할 때 전송하는 데이터
 * 
 * Record 타입 사용:
 * - Java 14+의 불변 객체
 * - getter, equals, hashCode, toString 자동 생성
 * 
 * Validation:
 * - @NotBlank: null, 빈 문자열, 공백만 있는 문자열 불가
 * - @Size: 문자열 길이 제한
 * - @NotNull: null 불가
 * - @Min/@Max: 숫자 범위 제한
 */
public record OpenStudyRoomCreateDto(
    // 방 제목 (필수, 1~30자)
    @NotBlank(message = "방 제목을 입력하세요")
    @Size(min = 1, max = 30, message = "방 제목은 1자 이상 30자 이하로 입력하세요")
    String title,
    
    // 방 설명 (선택, 최대 200자)
    @Size(max = 200, message = "방 설명은 200자 이하로 입력하세요")
    String description,
    
    // 공부 분야 (필수)
    // 예: "프로그래밍", "영어", "자격증" 등
    @NotBlank(message = "공부 분야를 선택하세요")
    String studyField,
    
    // 최대 인원 (필수, 2~10명)
    // SRS 12.1.2: 2~10명 내외로 설정
    @NotNull(message = "최대 인원을 설정하세요")
    @Min(value = 2, message = "최소 인원은 2명입니다")
    @Max(value = 10, message = "최대 인원은 10명입니다")
    Integer maxParticipants
) {
}
