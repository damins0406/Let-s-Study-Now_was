package com.team.LetsStudyNow_rg.domain.studyroom.service;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.exception.MemberNotFoundException;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto;
import com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession;
import com.team.LetsStudyNow_rg.domain.studyroom.exception.SessionNotFoundException;
import com.team.LetsStudyNow_rg.domain.studyroom.repository.StudySessionRepository;
import com.team.LetsStudyNow_rg.domain.timer.entity.PersonalTimer;
import com.team.LetsStudyNow_rg.domain.timer.repository.PersonalTimerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 공부 세션 관리 서비스
 * Timer와 연동하여 실제 공부 시간을 측정하고 레벨업 처리
 * 
 * 참고: 공부/휴식 모드 전환은 Timer API 사용
 * - POST /api/timer/toggle (기본 모드)
 * - POST /api/timer/pomodoro/change-status (뽀모도로 모드)
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class StudySessionService {
    
    private final StudySessionRepository studySessionRepository;
    private final MemberRepository memberRepository;
    private final LevelUpService levelUpService;
    private final PersonalTimerRepository personalTimerRepository;
    
    /**
     * 공부 세션 시작 (공부 모드로 시작)
     * 
     * @param memberId 회원 ID
     * @param studyType 스터디 타입 ("OPEN_STUDY" 또는 "GROUP_STUDY")
     * @param roomId 스터디방 ID
     * @return 생성된 세션
     */
    public StudySession startStudySession(Long memberId, String studyType, Long roomId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        // 이미 활성화된 세션이 있는지 확인
        studySessionRepository.findByMemberIdAndEndTimeIsNull(memberId)
            .ifPresent(session -> {
                log.warn("회원 {}에게 이미 활성 세션이 있습니다. 기존 세션을 종료합니다.", memberId);
                endStudySession(session.getId());
            });
        
        StudySession session = StudySession.builder()
            .member(member)
            .studyType(studyType)
            .roomId(roomId)
            .startTime(LocalDateTime.now())
            .lastModeChangeTime(LocalDateTime.now())
            .mode("STUDY")
            .studyMinutes(0)
            .build();
        
        StudySession savedSession = studySessionRepository.save(session);
        log.info("공부 세션 시작 - 회원: {}, 타입: {}, 방ID: {}, 세션ID: {}", 
                 member.getUsername(), studyType, roomId, savedSession.getId());
        
        return savedSession;
    }
    
    /**
     * 공부 세션 종료 및 레벨업 처리
     * Timer에서 측정한 실제 공부 시간을 가져와서 레벨업 처리
     * 
     * @param sessionId 세션 ID
     * @return 세션 종료 결과 (레벨업 여부 포함)
     */
    public SessionEndResultDto endStudySession(Long sessionId) {
        StudySession session = studySessionRepository.findById(sessionId)
            .orElseThrow(() -> new SessionNotFoundException(sessionId));
        
        if (!session.isActive()) {
            throw new IllegalStateException("이미 종료된 세션입니다.");
        }
        
        // Timer에서 실제 공부 시간을 가져옴 (Timer가 공부/휴식을 구분하여 측정)
        int totalStudyMinutes = 0;
        PersonalTimer timer = personalTimerRepository.findByMemberId(session.getMember().getId())
            .orElse(null);
        
        if (timer != null) {
            // Timer 종료 처리하여 마지막 세션 시간 누적
            timer.endTimer();
            // 초 단위를 분 단위로 변환
            totalStudyMinutes = (int) (timer.getTotalStudySeconds() / 60);
            log.info("Timer에서 가져온 공부 시간: {}분 ({}초)", totalStudyMinutes, timer.getTotalStudySeconds());
        } else {
            log.warn("Timer를 찾을 수 없습니다. 공부 시간 = 0분");
        }
        
        session.addStudyMinutes(totalStudyMinutes);
        session.endSession();
        studySessionRepository.save(session);
        
        log.info("공부 세션 종료 - 세션ID: {}, 총 공부시간: {}분 ({}시간 {}분)", 
                 sessionId, totalStudyMinutes, totalStudyMinutes / 60, totalStudyMinutes % 60);
        
        // 레벨업 처리
        boolean leveledUp = false;
        Integer newLevel = null;
        if (totalStudyMinutes > 0) {
            leveledUp = levelUpService.addStudyTimeAndCheckLevelUp(
                session.getMember().getId(), 
                totalStudyMinutes
            );
            
            if (leveledUp) {
                Member member = memberRepository.findById(session.getMember().getId())
                    .orElseThrow(() -> new MemberNotFoundException(session.getMember().getId()));
                newLevel = member.getLevel();
            }
        }
        
        return SessionEndResultDto.builder()
            .sessionId(sessionId)
            .studyMinutes(totalStudyMinutes)
            .leveledUp(leveledUp)
            .newLevel(newLevel)
            .build();
    }
    
    /**
     * 회원의 활성 세션 조회
     * 
     * @param memberId 회원 ID
     * @return 활성 세션 (없으면 null)
     */
    @Transactional(readOnly = true)
    public StudySession getActiveSession(Long memberId) {
        return studySessionRepository.findByMemberIdAndEndTimeIsNull(memberId)
            .orElse(null);
    }
}
