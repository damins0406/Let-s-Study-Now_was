package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

public class CreateStudyRoomRequest {

    private Long groupId;         // 그룹 ID
    private String roomName;      // 스터디방 이름
    private String studyField;    // 공부 분야
    private Integer studyHours;   // 공부 시간 (1~5)
    private Integer maxMembers;   // 최대 인원 (2~10)

    // 기본 생성자
    public CreateStudyRoomRequest() {
    }

    // 생성자
    public CreateStudyRoomRequest(Long groupId, String roomName, String studyField,
                                  Integer studyHours, Integer maxMembers) {
        this.groupId = groupId;
        this.roomName = roomName;
        this.studyField = studyField;
        this.studyHours = studyHours;
        this.maxMembers = maxMembers;
    }

    // Getter
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

    // Setter
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setStudyField(String studyField) {
        this.studyField = studyField;
    }

    public void setStudyHours(Integer studyHours) {
        this.studyHours = studyHours;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
}