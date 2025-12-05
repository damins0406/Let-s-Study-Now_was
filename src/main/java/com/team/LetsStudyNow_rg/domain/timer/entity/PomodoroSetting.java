package com.team.LetsStudyNow_rg.domain.timer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "pomodoro_setting")
public class PomodoroSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;  // 사용자 ID (1:1 관계)

    @Column(nullable = false)
    private Integer studyMinutes;  // 공부 시간 (분)

    @Column(nullable = false)
    private Integer restMinutes;   // 휴식 시간 (분)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 생성자
    public PomodoroSetting(Long memberId, Integer studyMinutes, Integer restMinutes) {
        validateMinutes(studyMinutes, restMinutes);
        this.memberId = memberId;
        this.studyMinutes = studyMinutes;
        this.restMinutes = restMinutes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직

    /**
     * 뽀모도로 설정 업데이트
     */
    public void updateSetting(Integer studyMinutes, Integer restMinutes) {
        validateMinutes(studyMinutes, restMinutes);
        this.studyMinutes = studyMinutes;
        this.restMinutes = restMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시간 유효성 검증
     */
    private void validateMinutes(Integer studyMinutes, Integer restMinutes) {
        if (studyMinutes == null || studyMinutes < 1 || studyMinutes > 120) {
            throw new IllegalArgumentException("공부 시간은 1~120분 사이여야 합니다.");
        }
        if (restMinutes == null || restMinutes < 1 || restMinutes > 120) {
            throw new IllegalArgumentException("휴식 시간은 1~120분 사이여야 합니다.");
        }
    }

    /**
     * 공부 시간 (초로 반환)
     */
    public long getStudySeconds() {
        return studyMinutes * 60L;
    }

    /**
     * 휴식 시간 (초로 반환)
     */
    public long getRestSeconds() {
        return restMinutes * 60L;
    }
}
