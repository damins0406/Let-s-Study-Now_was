package com.team.LetsStudyNow_rg.domain.checklist.repository;

import com.team.LetsStudyNow_rg.domain.checklist.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    // 특정 사용자의 특정 날짜의 체크리스트 조회
    List<Checklist> findByMemberIdAndTargetDate(Long memberId, LocalDate targetDate);

    // 특정 사용자의 특정 체크리스트 조회
    Optional<Checklist> findByIdAndMemberId(Long checklistId, Long memberId);

    // 특정 월에 대한 체크리스트가 있는 날짜(일) 조회
    @Query("SELECT DISTINCT DAY(c.targetDate) " +
            "FROM Checklist c " +
            "WHERE c.member.id = :memberId AND YEAR(c.targetDate) = :year AND MONTH(c.targetDate) = :month")
    List<Integer> findDaysWithChecklistByMonth(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("month") int month
    );
}
