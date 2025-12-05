package com.team.LetsStudyNow_rg.domain.timer.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "study_date"}))
public class StudyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // 사용자 ID

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;  // 공부한 날짜

    @Column(nullable = false)
    private Long totalStudySeconds;  // 해당 날짜의 총 공부 시간 (초)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 생성자
    public StudyHistory(Long memberId, LocalDate studyDate, Long studySeconds) {
        this.memberId = memberId;
        this.studyDate = studyDate;
        this.totalStudySeconds = studySeconds;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 비즈니스 로직

    /**
     * 공부 시간 추가
     */
    public void addStudyTime(Long additionalSeconds) {
        if (additionalSeconds < 0) {
            throw new IllegalArgumentException("추가할 시간은 0 이상이어야 합니다.");
        }
        this.totalStudySeconds += additionalSeconds;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 시간을 "시간:분:초" 형식으로 반환
     */
    public String getFormattedTime() {
        long hours = totalStudySeconds / 3600;
        long minutes = (totalStudySeconds % 3600) / 60;
        long seconds = totalStudySeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
