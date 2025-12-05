package com.team.LetsStudyNow_rg.domain.studyroom.entity;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 공부 세션 엔티티
 * 회원이 스터디방에서 공부한 시간을 기록하고 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "study_sessions")
public class StudySession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 공부하는 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    // 스터디 타입 ("OPEN_STUDY" 또는 "GROUP_STUDY")
    @Column(nullable = false, length = 20)
    private String studyType;
    
    // 스터디방 ID (오픈스터디 또는 그룹스터디의 방 ID)
    @Column(nullable = false)
    private Long roomId;
    
    // 세션 시작 시간
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();
    
    // 세션 종료 시간 (null이면 아직 진행 중)
    private LocalDateTime endTime;
    
    // 현재 모드 ("STUDY" 또는 "REST")
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String mode = "STUDY";
    
    // 마지막 모드 변경 시간
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime lastModeChangeTime = LocalDateTime.now();
    
    // 실제 공부한 시간 (분 단위)
    @Column(nullable = false)
    @Builder.Default
    private Integer studyMinutes = 0;
    
    /**
     * 공부 시간 추가 (분 단위)
     */
    public void addStudyMinutes(int minutes) {
        this.studyMinutes += minutes;
    }
    
    /**
     * 세션 종료
     */
    public void endSession() {
        this.endTime = LocalDateTime.now();
    }
    
    /**
     * 세션이 진행 중인지 확인
     */
    public boolean isActive() {
        return this.endTime == null;
    }
}
