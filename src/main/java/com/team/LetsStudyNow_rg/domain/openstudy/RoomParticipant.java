package com.team.LetsStudyNow_rg.domain.openstudy;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 방 참여자 정보 엔티티 (DB 테이블: room_participant)
 * 어떤 회원이 어떤 방에 참여하고 있는지를 기록
 * 
 * 주요 기능:
 * - 방과 회원의 다대다 관계를 중간 테이블로 연결
 * - 참여 시간 기록
 * - 한 회원이 같은 방에 중복 참여하지 못하도록 제약
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "room_participant",
       uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "member_id"}))
public class RoomParticipant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 참여 중인 방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private OpenStudyRoom room;
    
    // 참여 중인 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    // 방 참여 시작 시간 (기록용, 나중에 통계나 참여 시간 계산에 사용 가능)
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
