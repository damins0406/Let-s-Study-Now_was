package com.team.LetsStudyNow_rg.domain.timer.repository;

import com.team.LetsStudyNow_rg.domain.timer.entity.StudyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long> {

    /**
     * 특정 날짜의 공부 기록 조회
     */
    Optional<StudyHistory> findByMemberIdAndStudyDate(Long memberId, LocalDate studyDate);

    /**
     * 사용자의 모든 공부 기록 조회 (최신순)
     */
    List<StudyHistory> findByMemberIdOrderByStudyDateDesc(Long memberId);

    /**
     * 사용자의 특정 기간 공부 기록 조회
     */
    List<StudyHistory> findByMemberIdAndStudyDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);

    /**
     * 사용자의 총 누적 공부 시간 계산 (초)
     */
    @Query("SELECT COALESCE(SUM(sh.totalStudySeconds), 0) FROM StudyHistory sh WHERE sh.memberId = :memberId")
    Long getTotalStudySecondsByMemberId(@Param("memberId") Long memberId);

    /**
     * 사용자의 특정 기간 누적 공부 시간 계산 (초)
     */
    @Query("SELECT COALESCE(SUM(sh.totalStudySeconds), 0) FROM StudyHistory sh " +
           "WHERE sh.memberId = :memberId AND sh.studyDate BETWEEN :startDate AND :endDate")
    Long getTotalStudySecondsByMemberIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
