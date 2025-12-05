package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.GroupMember;
import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long id;                    // 그룹 멤버 ID
    private Long memberId;              // 사용자의 ID
    private String role;                // 역할 (방 생성자 / 멤버)
    private LocalDateTime joinedAt;     // 그룹 참여 일시

    // 기본 생성자
    public GroupMemberResponse() {
    }

    // Entity → DTO 변환
    public GroupMemberResponse(GroupMember groupMember) {
        this.id = groupMember.getId();
        this.memberId = groupMember.getMemberId();
        this.role = groupMember.getRole();
        this.joinedAt = groupMember.getJoinedAt();
    }

    // Getter
    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}