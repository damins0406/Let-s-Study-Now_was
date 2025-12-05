package com.team.LetsStudyNow_rg.domain.timer.repository;

import com.team.LetsStudyNow_rg.domain.timer.entity.PomodoroSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PomodoroSettingRepository extends JpaRepository<PomodoroSetting, Long> {

    /**
     * 사용자의 뽀모도로 설정 조회
     */
    Optional<PomodoroSetting> findByMemberId(Long memberId);

    /**
     * 사용자의 뽀모도로 설정 존재 여부 확인
     */
    boolean existsByMemberId(Long memberId);
}
