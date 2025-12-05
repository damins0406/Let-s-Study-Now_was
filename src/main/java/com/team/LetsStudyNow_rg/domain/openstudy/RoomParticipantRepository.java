package com.team.LetsStudyNow_rg.domain.openstudy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RoomParticipant 엔티티의 데이터베이스 접근을 담당하는 Repository
 * 참여자 조회, 존재 여부 확인, 삭제 등의 쿼리 메서드 제공
 */
@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    
    /**
     * 특정 방에 특정 회원이 참여 중인지 확인
     * 중복 참여 방지용
     * 
     * @param roomId 방 ID
     * @param memberId 회원 ID
     * @return true: 참여 중, false: 참여하지 않음
     */
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);
    
    /**
     * 특정 방의 모든 참여자 조회
     * 방 참여자 목록 기능 구현 시 사용 가능
     * 
     * @param roomId 방 ID
     * @return 해당 방의 참여자 목록
     */
    List<RoomParticipant> findByRoomId(Long roomId);
    
    /**
     * 특정 회원이 현재 참여 중인 활성 방 조회
     * 한 명의 회원은 동시에 하나의 활성 방에만 참여 가능
     * 
     * JPQL 사용:
     * - RoomParticipant와 연관된 OpenStudyRoom의 상태가 ACTIVE인 경우만 조회
     * - 방 생성이나 참여 시 중복 검증용으로 사용
     * 
     * @param memberId 회원 ID
     * @return 참여 중인 활성 방 정보 (없으면 Optional.empty())
     */
    @Query("SELECT rp FROM RoomParticipant rp WHERE rp.member.id = :memberId " +
           "AND rp.room.status = 'ACTIVE'")
    Optional<RoomParticipant> findActiveRoomByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 특정 방의 특정 회원 참여 정보 조회
     * 방 나가기 기능에서 사용 (해당 참여 기록을 찾아서 삭제)
     * 
     * @param roomId 방 ID
     * @param memberId 회원 ID
     * @return 참여 정보 (없으면 Optional.empty())
     */
    Optional<RoomParticipant> findByRoomIdAndMemberId(Long roomId, Long memberId);
    
    /**
     * 특정 방의 모든 참여자 삭제
     * 방 삭제 시 해당 방의 모든 참여자를 일괄 삭제
     * @param roomId 방 ID
     */
    void deleteByRoomId(Long roomId);
}
