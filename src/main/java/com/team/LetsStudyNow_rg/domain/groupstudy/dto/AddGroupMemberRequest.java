package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

public class AddGroupMemberRequest {

    private Long groupId;   // 그룹 ID
    private Long memberId;  // 그룹에 추가할 멤버 ID

    // 기본 생성자
    public AddGroupMemberRequest() {
    }

    // 생성자
    public AddGroupMemberRequest(Long groupId, Long memberId) {
        this.groupId = groupId;
        this.memberId = memberId;
    }

    // Getter
    public Long getGroupId() {
        return groupId;
    }

    public Long getMemberId() {
        return memberId;
    }

    // Setter
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
