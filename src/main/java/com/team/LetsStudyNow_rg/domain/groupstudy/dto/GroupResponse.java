package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.Group;
import java.time.LocalDateTime;

public class GroupResponse {

    private Long id;                    // 그룹 ID
    private String groupName;           // 그룹 이름
    private Long leaderId;              // 그룹 생성자 ID
    private LocalDateTime createdAt;    // 생성일시
    private Long memberCount;           // 그룹 참여자 수

    // 기본 생성자
    public GroupResponse() {
    }

    // Entity → DTO 변환 생성자
    public GroupResponse(Group group) {
        this.id = group.getId();
        this.groupName = group.getGroupName();
        this.leaderId = group.getLeaderId();
        this.createdAt = group.getCreatedAt();
    }

    // Entity + memberCount → DTO 변환 생성자
    public GroupResponse(Group group, Long memberCount) {
        this.id = group.getId();
        this.groupName = group.getGroupName();
        this.leaderId = group.getLeaderId();
        this.createdAt = group.getCreatedAt();
        this.memberCount = memberCount;
    }

    // Getter
    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getMemberCount() {
        return memberCount;
    }
}
