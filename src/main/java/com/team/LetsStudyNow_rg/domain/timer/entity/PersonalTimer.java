package com.team.LetsStudyNow_rg.domain.timer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "personal_timer")
public class PersonalTimer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;  // 사용자 ID

    @Column(nullable = false)
    private Long roomId;  // 현재 입장한 방 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimerMode timerMode;  // 기본 모드 or 뽀모도로 모드

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimerStatus timerStatus;  // 공부 중 or 휴식 중

    @Column(nullable = false)
    private LocalDateTime sessionStartTime;  // 현재 세션 시작 시간

    @Column(nullable = false)
    private Long totalStudySeconds;  // 총 누적 공부 시간 (초)

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 타이머 생성 시간 (방 입장 시간)

    private LocalDateTime updatedAt;  // 마지막 업데이트 시간

    // 생성자
    public PersonalTimer(Long memberId, Long roomId, TimerMode timerMode, TimerStatus timerStatus) {
        this.memberId = memberId;
        this.roomId = roomId;
        this.timerMode = timerMode;
        this.timerStatus = timerStatus;
        this.sessionStartTime = LocalDateTime.now();
        this.totalStudySeconds = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직

    /**
     * 수동 토글: 공부 ↔ 휴식 전환 (기본 모드에서만 가능)
     */
    public void toggleStatus() {
        if (this.timerMode == TimerMode.POMODORO) {
            throw new IllegalStateException("뽀모도로 모드에서는 수동 토글을 사용할 수 없습니다.");
        }

        // 현재 세션 종료하고 시간 누적
        if (this.timerStatus == TimerStatus.STUDYING) {
            accumulateStudyTime();
        }

        // 상태 전환
        this.timerStatus = (this.timerStatus == TimerStatus.STUDYING) 
                ? TimerStatus.RESTING 
                : TimerStatus.STUDYING;

        // 새 세션 시작
        this.sessionStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 뽀모도로 모드로 전환
     */
    public void switchToPomodoroMode() {
        // 현재 세션 시간 누적
        if (this.timerStatus == TimerStatus.STUDYING) {
            accumulateStudyTime();
        }

        this.timerMode = TimerMode.POMODORO;
        this.sessionStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기본 모드로 전환
     */
    public void switchToBasicMode() {
        // 현재 세션 시간 누적
        if (this.timerStatus == TimerStatus.STUDYING) {
            accumulateStudyTime();
        }

        this.timerMode = TimerMode.BASIC;
        this.sessionStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 뽀모도로 상태 변경 (공부 ↔ 휴식)
     */
    public void changePomodoroStatus(TimerStatus newStatus) {
        if (this.timerMode != TimerMode.POMODORO) {
            throw new IllegalStateException("뽀모도로 모드가 아닙니다.");
        }

        // 현재 세션 시간 누적
        if (this.timerStatus == TimerStatus.STUDYING) {
            accumulateStudyTime();
        }

        this.timerStatus = newStatus;
        this.sessionStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 현재까지의 공부 시간 누적
     */
    private void accumulateStudyTime() {
        if (this.timerStatus == TimerStatus.STUDYING) {
            long seconds = java.time.Duration.between(this.sessionStartTime, LocalDateTime.now()).getSeconds();
            this.totalStudySeconds += seconds;
        }
    }

    /**
     * 타이머 종료 (방 퇴장 시)
     */
    public void endTimer() {
        // 마지막 세션 시간 누적
        if (this.timerStatus == TimerStatus.STUDYING) {
            accumulateStudyTime();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 현재 세션의 경과 시간 (초)
     */
    public long getCurrentSessionSeconds() {
        return java.time.Duration.between(this.sessionStartTime, LocalDateTime.now()).getSeconds();
    }
}
