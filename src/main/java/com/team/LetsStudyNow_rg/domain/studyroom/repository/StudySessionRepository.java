package com.team.LetsStudyNow_rg.domain.studyroom.repository;

import com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    
    /**
     * 회원의 활성 세션 조회 (종료되지 않은 세션)
     */
    Optional<StudySession> findByMemberIdAndEndTimeIsNull(Long memberId);
    

}
