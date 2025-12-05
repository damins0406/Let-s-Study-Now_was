package com.team.LetsStudyNow_rg.domain.timer.repository;

import com.team.LetsStudyNow_rg.domain.timer.entity.PersonalTimer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalTimerRepository extends JpaRepository<PersonalTimer, Long> {

    /**
     * 사용자의 활성 타이머 조회
     * (한 사용자는 동시에 하나의 타이머만 가질 수 있음)
     */
    Optional<PersonalTimer> findByMemberId(Long memberId);

    /**
     * 사용자의 활성 타이머 존재 여부 확인
     */
    boolean existsByMemberId(Long memberId);

    /**
     * 특정 방의 모든 타이머 조회
     */
    java.util.List<PersonalTimer> findByRoomId(Long roomId);

    /**
     * 사용자의 활성 타이머 삭제
     */
    void deleteByMemberId(Long memberId);
}
