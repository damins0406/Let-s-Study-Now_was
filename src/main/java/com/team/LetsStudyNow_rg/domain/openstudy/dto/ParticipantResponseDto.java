package com.team.LetsStudyNow_rg.domain.openstudy.dto;

import com.team.LetsStudyNow_rg.domain.timer.entity.TimerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오픈스터디방 참여자 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponseDto {
    
    /**
     * 회원 ID
     */
    private Long memberId;
    
    /**
     * 회원 닉네임
     */
    private String username;
    
    /**
     * 프로필 이미지
     */
    private String profileImage;
    
    /**
     * 공부/휴식 상태 (STUDYING 또는 RESTING)
     */
    private TimerStatus timerStatus;
}
