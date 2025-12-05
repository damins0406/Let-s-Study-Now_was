package com.team.LetsStudyNow_rg.domain.groupstudy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_rooms")
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;  // 그룹 ID

    @Column(nullable = false)
    private String roomName;  // 스터디방 이름

    @Column(nullable = false)
    private String studyField;  // 공부 분야 (프로그래밍, 영어, 자격증 등)

    @Column(nullable = false)
    private Integer studyHours;  // 공부 시간 (1~5시간)

    @Column(nullable = false)
    private Integer maxMembers;  // 최대 인원 (2~10명)

    @Column(nullable = false)
    private Integer currentMembers;  // 현재 인원

    @Column(nullable = false)
    private Long creatorId;  // 방 생성자 ID

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 생성 시간

    @Column(nullable = false)
    private LocalDateTime endTime;  // 종료 시간

    @Column(nullable = false)
    private String status;  // 상태 (ACTIVE, ENDED)

    // 기본 생성자
    protected StudyRoom() {
    }

    // 생성자
    public StudyRoom(Long groupId, String roomName, String studyField,
                     Integer studyHours, Integer maxMembers, Long creatorId) {
        this.groupId = groupId;
        this.roomName = roomName;
        this.studyField = studyField;
        this.studyHours = studyHours;
        this.maxMembers = maxMembers;
        this.currentMembers = 1;  // 방 생성자는 자동 입장
        this.creatorId = creatorId;
        this.createdAt = LocalDateTime.now();
        this.endTime = LocalDateTime.now().plusHours(studyHours);  // 종료 시간 계산
        this.status = "ACTIVE";
    }

    // Getter
    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getStudyField() {
        return studyField;
    }

    public Integer getStudyHours() {
        return studyHours;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public Integer getCurrentMembers() {
        return currentMembers;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    // 스터디방 입장 (인원 자동 증가)
    public void addParticipant() {
        if (currentMembers >= maxMembers) {
            throw new IllegalStateException("최대 인원에 도달하여 입장이 불가합니다");
        }
        this.currentMembers++;
    }

    // 스터디방 퇴장 (인원 자동 감소)
    public void removeParticipant() {
        if (currentMembers > 0) {
            this.currentMembers--;
        }
    }

    // 스터디방 종료
    public void end() {
        this.status = "ENDED";
    }

    // 스터디방 입장 가능 여부
    public boolean isFull() {
        return currentMembers >= maxMembers;
    }

    // 스터디방 종료 여부
    public boolean isEnded() {
        return "ENDED".equals(status) || LocalDateTime.now().isAfter(endTime);
    }
}