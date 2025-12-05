package com.team.LetsStudyNow_rg.domain.groupstudy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 그룹 ID 자동 증가
    private Long id;

    @Column(nullable = false)
    private String groupName; // 그룹 이름

    @Column(nullable = false)
    private Long leaderId; // 그룹 생성자의 ID

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성일시

    // 기본 생성자
    protected Group() {
    }

    // 생성자
    public Group(String groupName, Long leaderId) {
        this.groupName = groupName;
        this.leaderId = leaderId;
        this.createdAt = LocalDateTime.now();
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
}
