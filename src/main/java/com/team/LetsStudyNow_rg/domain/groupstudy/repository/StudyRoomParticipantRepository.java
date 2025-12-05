package com.team.LetsStudyNow_rg.domain.groupstudy.repository;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRoomParticipantRepository extends JpaRepository<StudyRoomParticipant, Long> {

    // 스터디방의 모든 참여자 조회
    List<StudyRoomParticipant> findByStudyRoomId(Long studyRoomId);

    // 특정 사용자가 특정 방에 참여 중인지 확인
    Optional<StudyRoomParticipant> findByStudyRoomIdAndMemberId(Long studyRoomId, Long memberId);

    // 스터디방의 참여자 수
    long countByStudyRoomId(Long studyRoomId);

    // 방 참여자 삭제
    void deleteByStudyRoomIdAndMemberId(Long studyRoomId, Long memberId);

    // 방 종료 시, 스터디방의 모든 참여자 삭제
    void deleteByStudyRoomId(Long studyRoomId);
}