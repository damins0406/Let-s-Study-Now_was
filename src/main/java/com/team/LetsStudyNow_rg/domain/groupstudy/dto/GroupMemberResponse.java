package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.GroupMember;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import java.time.LocalDateTime;

public class GroupMemberResponse {

    private Long id;                    // 그룹 멤버 ID
    private Long memberId;              // 사용자의 ID
    private String username;            // 사용자 이름
    private String profileImage;        // 프로필 이미지 URL
    private String role;                // 역할 (방 생성자 / 멤버)
    private LocalDateTime joinedAt;     // 그룹 참여 일시

    // 기본 생성자
    public GroupMemberResponse() {
    }

    // Entity → DTO 변환 (Member 정보 포함)
    public GroupMemberResponse(GroupMember groupMember, Member member) {
        this.id = groupMember.getId();
        this.memberId = groupMember.getMemberId();
        this.username = member.getUsername();
        this.profileImage = member.getProfileImage();
        this.role = groupMember.getRole();
        this.joinedAt = groupMember.getJoinedAt();
    }

    // 기존 생성자 (하위 호환성 유지)
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

    public String getUsername() {
        return username;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}