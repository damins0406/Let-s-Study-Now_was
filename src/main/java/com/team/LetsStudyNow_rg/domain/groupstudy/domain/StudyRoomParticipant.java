package com.team.LetsStudyNow_rg.domain.groupstudy.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_room_participants")
public class StudyRoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studyRoomId;  // 스터디방 ID

    @Column(nullable = false)
    private Long memberId;  // 방 참여자 ID

    @Column(nullable = false)
    private LocalDateTime joinedAt;  // 방 입장 시간

    // 기본 생성자
    protected StudyRoomParticipant() {
    }

    // 생성자
    public StudyRoomParticipant(Long studyRoomId, Long memberId) {
        this.studyRoomId = studyRoomId;
        this.memberId = memberId;
        this.joinedAt = LocalDateTime.now();
    }

    // Getter
    public Long getId() {
        return id;
    }

    public Long getStudyRoomId() {
        return studyRoomId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}