package com.team.LetsStudyNow_rg.domain.groupstudy.repository;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {

    // 그룹의 모든 스터디방 조회
    List<StudyRoom> findByGroupId(Long groupId);

    // 그룹의 활성화된 스터디방만 조회
    List<StudyRoom> findByGroupIdAndStatus(Long groupId, String status);

    // 생성자가 만든 스터디방 조회
    List<StudyRoom> findByCreatorId(Long creatorId);
}