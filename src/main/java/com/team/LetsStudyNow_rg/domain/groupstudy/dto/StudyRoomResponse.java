package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoom;
import java.time.Duration;
import java.time.LocalDateTime;

public class StudyRoomResponse {

    private Long id;
    private Long groupId;
    private String roomName;
    private String studyField;
    private Integer studyHours;
    private Integer maxMembers;
    private Integer currentMembers;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime endTime;
    private String status;
    private Long remainingMinutes;  // 남은 시간 (분)

    // 기본 생성자
    public StudyRoomResponse() {
    }

    // Entity → DTO 변환
    public StudyRoomResponse(StudyRoom studyRoom) {
        this.id = studyRoom.getId();
        this.groupId = studyRoom.getGroupId();
        this.roomName = studyRoom.getRoomName();
        this.studyField = studyRoom.getStudyField();
        this.studyHours = studyRoom.getStudyHours();
        this.maxMembers = studyRoom.getMaxMembers();
        this.currentMembers = studyRoom.getCurrentMembers();
        this.creatorId = studyRoom.getCreatorId();
        this.createdAt = studyRoom.getCreatedAt();
        this.endTime = studyRoom.getEndTime();
        this.status = studyRoom.getStatus();

        // 남은 시간 계산
        if ("ACTIVE".equals(studyRoom.getStatus())) {
            Duration duration = Duration.between(LocalDateTime.now(), studyRoom.getEndTime());
            this.remainingMinutes = Math.max(0, duration.toMinutes());
        } else {
            this.remainingMinutes = 0L;
        }
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

    public Long getRemainingMinutes() {
        return remainingMinutes;
    }
}