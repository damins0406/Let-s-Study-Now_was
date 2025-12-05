package com.team.LetsStudyNow_rg.domain.groupstudy.scheduler;

import com.team.LetsStudyNow_rg.domain.groupstudy.service.StudyRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 그룹 스터디 방 자동 종료 스케줄러
 * Spring의 @Scheduled를 사용하여 주기적으로 실행
 * 
 * 주요 기능:
 * - 설정된 studyHours(1~5시간)가 끝난 방 자동 종료
 * - 모든 참여자 자동 퇴장 처리
 * - 세션 종료 및 레벨업 처리
 * - PersonalTimer 종료 처리
 * 
 * 실행 주기: 1분마다 (60초)
 * 시작 지연: 30초 (애플리케이션 시작 후 안정화 시간 확보)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudyRoomCleanupScheduler {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 시간 만료된 그룹 스터디 방 자동 종료
     * 
     * 실행 조건:
     * - 매 1분마다 실행 (fixedRate = 60000ms)
     * - 애플리케이션 시작 30초 후부터 실행 (initialDelay = 30000ms)
     * 
     * 동작 과정:
     * 1. status가 "ACTIVE"이고 endTime이 현재 시간을 지난 방 조회
     * 2. 각 방의 모든 참여자에 대해:
     *    - StudySession 종료 (레벨업 처리)
     *    - PersonalTimer 종료 (공부 시간 누적)
     *    - 참여자 퇴장 처리
     * 3. 방 삭제 처리
     * 
     * 예외 처리:
     * - 전체 로직 실패: 에러 로그 남기고 다음 실행 주기까지 대기
     * 
     * 로그 레벨:
     * - DEBUG: 스케줄러 시작/종료
     * - INFO: 만료된 방 발견 시
     * - ERROR: 예외 발생 시
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    public void autoEndExpiredRooms() {
        log.debug("=== 그룹 스터디 방 자동 종료 확인 시작 ===");
        
        try {
            // StudyRoomService의 autoEndExpiredRooms() 호출
            // 이 메서드 내부에서:
            // 1. 만료된 방 조회
            // 2. 각 참여자의 세션/타이머 종료
            // 3. 참여자 퇴장 처리
            // 4. 방 삭제
            studyRoomService.autoEndExpiredRooms();
            
        } catch (Exception e) {
            // 전체 스케줄러 로직 실패 시 에러 로그
            log.error("그룹 스터디 방 자동 종료 중 오류 발생: {}", e.getMessage(), e);
        }
        
        log.debug("=== 그룹 스터디 방 자동 종료 확인 종료 ===");
    }
}
