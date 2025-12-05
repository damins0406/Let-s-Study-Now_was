package com.team.LetsStudyNow_rg.domain.groupstudy.dto;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoomParticipant;
import java.time.LocalDateTime;

public class StudyRoomParticipantResponse {

    private Long id;                        // 참여자 ID
    private Long memberId;                  // 사용자 ID
    private LocalDateTime joinedAt;         // 입장 시간

    // 기본 생성자
    public StudyRoomParticipantResponse() {
    }

    // Entity → DTO 변환
    public StudyRoomParticipantResponse(StudyRoomParticipant participant) {
        this.id = participant.getId();
        this.memberId = participant.getMemberId();
        this.joinedAt = participant.getJoinedAt();
    }

    // Getter
    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
