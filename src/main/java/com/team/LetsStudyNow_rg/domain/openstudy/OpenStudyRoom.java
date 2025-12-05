package com.team.LetsStudyNow_rg.domain.openstudy;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 오픈 스터디 방 엔티티 (DB 테이블: open_study_room)
 * 방의 기본 정보, 참여자 수, 삭제 관련 상태를 관리
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "open_study_room")
public class OpenStudyRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 방 제목 (최대 30자)
    @Column(nullable = false, length = 30)
    private String title;
    
    // 방 설명 (최대 200자, 선택사항)
    @Column(length = 200)
    private String description;
    
    // 공부 분야 (DB에저장: "프로그래밍", "영어", "기타" 등)
    @Convert(converter = StudyFieldConverter.class)
    @Column(nullable = false)
    private StudyField studyField;
    
    // 방 최대 인원 (2~10명)
    @Column(nullable = false)
    private int maxParticipants;
    
    // 현재 참여 인원 (실시간으로 증감)
    @Column(nullable = false)
    private int currentParticipants;
    
    // 방 생성자 (Member 테이블과 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Member creator;
    
    // 방 상태 (ACTIVE: 활성, PENDING_DELETE: 삭제 예정, DELETED: 삭제됨)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;
    
    // 방 생성 시간
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // 삭제 예정 시간 (참여자가 1명 이하일 때 5분 후로 설정)
    private LocalDateTime deleteScheduledAt;
    
    // 생성자 혼자 있는 타이머 시작 시간 (5분 경과 시 방 삭제)
    private LocalDateTime aloneTimerStartedAt;
    
    // 방 참여자 목록 (RoomParticipant와 일대다 관계)
    // 방이 삭제되면 참여자 정보도 함께 삭제됨 (cascade)
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomParticipant> participants = new ArrayList<>();
    
    // === 비즈니스 로직 메서드 ===
    
    /**
     * 현재 참여 인원을 1명 증가
     * 새로운 사용자가 방에 참여할 때 호출
     */
    public void incrementParticipants() {
        this.currentParticipants++;
    }
    
    /**
     * 현재 참여 인원을 1명 감소
     * 사용자가 방에서 나갈 때 호출
     * 0 미만으로 내려가지 않도록 검증
     */
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }
    
    /**
     * 방이 정원 초과 상태인지 확인
     * @return true: 현재 인원 >= 최대 인원 (더 이상 참여 불가)
     */
    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }
    
    /**
     * 방을 5분 후 삭제 예약 상태로 변경
     * 참여자가 1명 이하로 떨어졌을 때 호출
     * - 상태를 PENDING_DELETE로 변경
     * - 현재 시간 + 5분을 삭제 예정 시간으로 설정
     */
    public void scheduleDelete() {
        this.status = RoomStatus.PENDING_DELETE;
        this.deleteScheduledAt = LocalDateTime.now().plusMinutes(5);
    }
    
    /**
     * 삭제 예약을 취소하고 방을 활성 상태로 복구
     * 삭제 예정 방에 새로운 참여자가 들어왔을 때 호출
     * - 상태를 ACTIVE로 변경
     * - 삭제 예정 시간을 null로 초기화
     */
    public void cancelDeleteSchedule() {
        this.status = RoomStatus.ACTIVE;
        this.deleteScheduledAt = null;
    }
    
    /**
     * 생성자 혼자 있는 타이머 시작
     * 방이 생성되는 순간 호출
     * 5분 동안 다른 참여자가 없으면 자동 삭제됨
     */
    public void startAloneTimer() {
        this.aloneTimerStartedAt = LocalDateTime.now();
    }
    
    /**
     * 생성자 혼자 있는 타이머 해제
     * 두 번째 참여자가 입장했을 때 호출
     * 타이머 시작 시간을 null로 초기화하여 자동 삭제 방지
     */
    public void resetAloneTimer() {
        this.aloneTimerStartedAt = null;
    }
    
    /**
     * 방을 삭제 상태로 변경 (Soft Delete)
     * 실제 DB에서 삭제하지 않고 상태만 DELETED로 변경
     * 데이터 보존 및 이력 관리를 위해 사용
     */
    public void delete() {
        this.status = RoomStatus.DELETED;
    }
    
    /**
     * 방에 참여 가능한 상태인지 확인
     * @return true: ACTIVE 상태 && 정원 미달
     */
    public boolean isJoinable() {
        return this.status == RoomStatus.ACTIVE && !isFull();
    }
    
    /**
     * 생성자 혼자 있는 타이머가 5분 경과했는지 확인
     * 스케줄러가 1분마다 체크
     * @return true: 타이머가 시작됐고 && 5분 경과 && 현재 인원 1명
     */
    public boolean isAloneTimerExpired() {
        return this.aloneTimerStartedAt != null 
            && LocalDateTime.now().isAfter(this.aloneTimerStartedAt.plusMinutes(5))
            && this.currentParticipants == 1;
    }
    
    /**
     * 삭제 예정 시간이 지났는지 확인
     * 스케줄러가 1분마다 체크
     * @return true: 삭제 예정 시간이 설정됐고 && 현재 시간이 지남
     */
    public boolean isDeleteScheduleExpired() {
        return this.deleteScheduledAt != null 
            && LocalDateTime.now().isAfter(this.deleteScheduledAt);
    }
}
