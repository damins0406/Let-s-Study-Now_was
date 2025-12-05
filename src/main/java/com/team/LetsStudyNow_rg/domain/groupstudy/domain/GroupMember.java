package com.team.LetsStudyNow_rg.domain.groupstudy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_members") //
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;  // 그룹 ID

    @Column(nullable = false)
    private Long memberId;  // 그룹에 참여한 멤버 ID

    @Column(nullable = false)
    private String role;  // '방 생성자' / '멤버'

    @Column(nullable = false)
    private LocalDateTime joinedAt;  // 그룹 참여 일시

    // 기본 생성자
    protected GroupMember() {
    }

    // 생성자
    public GroupMember(Long groupId, Long memberId, String role) {
        this.groupId = groupId;
        this.memberId = memberId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    // Getter
    public Long getId() {
        return id;
    }

    public Long getGroupId() {
        return groupId;
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

    // 방 생성자인지 확인
    public boolean isLeader() {
        return "LEADER".equals(role);
    }
}