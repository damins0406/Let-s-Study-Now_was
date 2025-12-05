package com.team.LetsStudyNow_rg.domain.timer.service;

import com.team.LetsStudyNow_rg.domain.timer.dto.response.StudyTimeResponse;
import com.team.LetsStudyNow_rg.domain.timer.dto.response.TimerStatusResponse;
import com.team.LetsStudyNow_rg.domain.timer.entity.*;
import com.team.LetsStudyNow_rg.domain.timer.repository.PersonalTimerRepository;
import com.team.LetsStudyNow_rg.domain.timer.repository.PomodoroSettingRepository;
import com.team.LetsStudyNow_rg.domain.timer.repository.StudyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalTimerService {

    private final PersonalTimerRepository personalTimerRepository;
    private final PomodoroSettingRepository pomodoroSettingRepository;
    private final StudyHistoryRepository studyHistoryRepository;

    /**
     * 타이머 시작 (방 입장 시)
     * - 스터디룸 입장 시 자동으로 타이머 시작
     * - 모든 참여자는 공부 상태(STUDYING)로 시작
     */
    @Transactional
    public TimerStatusResponse startTimer(Long memberId, Long roomId, boolean isRoomCreator) {
        // 이미 활성 타이머가 있는지 확인
        if (personalTimerRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("이미 활성화된 타이머가 있습니다. 한 번에 하나의 방에서만 타이머를 사용할 수 있습니다.");
        }

        // 기본 모드로 시작
        // 방 입장 시 모든 참여자는 공부 상태(STUDYING)로 시작
        PersonalTimer timer = new PersonalTimer(
                memberId,
                roomId,
                TimerMode.BASIC,
                TimerStatus.STUDYING
        );

        PersonalTimer savedTimer = personalTimerRepository.save(timer);
        return new TimerStatusResponse(savedTimer);
    }

    /**
     * 타이머 종료 (방 퇴장 시)
     * - 방 타이머 종료 시 개인 타이머도 자동 종료
     * - 누적 시간을 StudyHistory에 저장
     */
    @Transactional
    public void endTimer(Long memberId) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        // 타이머 종료 및 시간 누적
        timer.endTimer();

        // StudyHistory에 저장
        saveToStudyHistory(memberId, timer.getTotalStudySeconds());

        // 타이머 삭제
        personalTimerRepository.delete(timer);
    }

    /**
     * 수동 토글 (공부 ↔ 휴식)
     * - 기본 모드에서만 작동
     */
    @Transactional
    public TimerStatusResponse toggleTimer(Long memberId) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        // 뽀모도로 모드에서는 수동 토글 불가
        timer.toggleStatus();

        return new TimerStatusResponse(timer);
    }

    /**
     * 뽀모도로 모드 시작
     * - 기존 공부 시간 유지
     * - 뽀모도로 상태가 기준이 됨
     */
    @Transactional
    public TimerStatusResponse startPomodoroMode(Long memberId) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        // 1.3.2: 뽀모도로 설정이 있는지 확인
        if (!pomodoroSettingRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("뽀모도로 설정이 필요합니다.");
        }

        timer.switchToPomodoroMode();

        return new TimerStatusResponse(timer);
    }

    /**
     * 뽀모도로 모드 종료
     * - 기본 모드로 전환
     * - 누적 데이터는 유지
     */
    @Transactional
    public TimerStatusResponse stopPomodoroMode(Long memberId) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        timer.switchToBasicMode();

        return new TimerStatusResponse(timer);
    }

    /**
     * 뽀모도로 상태 자동 전환
     * - 공부 시간 완료 → 휴식으로 전환
     * - 휴식 시간 완료 → 공부로 전환
     */
    @Transactional
    public TimerStatusResponse changePomodoroStatus(Long memberId, TimerStatus newStatus) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        timer.changePomodoroStatus(newStatus);

        return new TimerStatusResponse(timer);
    }

    /**
     * 타이머 상태 조회
     */
    public TimerStatusResponse getTimerStatus(Long memberId) {
        PersonalTimer timer = personalTimerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 타이머가 없습니다."));

        return new TimerStatusResponse(timer);
    }

    /**
     * 누적 시간 조회
     * - 총 누적 시간
     * - 오늘의 누적 시간
     */
    public StudyTimeResponse getStudyTime(Long memberId) {
        // 총 누적 시간
        Long totalSeconds = studyHistoryRepository.getTotalStudySecondsByMemberId(memberId);

        // 오늘 누적 시간
        LocalDate today = LocalDate.now();
        Long todaySeconds = studyHistoryRepository.findByMemberIdAndStudyDate(memberId, today)
                .map(StudyHistory::getTotalStudySeconds)
                .orElse(0L);

        return StudyTimeResponse.of(totalSeconds, todaySeconds);
    }

    /**
     * StudyHistory에 공부 시간 저장
     */
    private void saveToStudyHistory(Long memberId, Long studySeconds) {
        if (studySeconds == 0) {
            return;  // 공부 시간이 0이면 저장하지 않음
        }

        LocalDate today = LocalDate.now();

        StudyHistory history = studyHistoryRepository
                .findByMemberIdAndStudyDate(memberId, today)
                .orElseGet(() -> new StudyHistory(memberId, today, 0L));

        history.addStudyTime(studySeconds);
        studyHistoryRepository.save(history);
    }
}
