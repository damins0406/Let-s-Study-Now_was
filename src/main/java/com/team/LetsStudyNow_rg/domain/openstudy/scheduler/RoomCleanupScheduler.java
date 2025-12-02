package com.team.LetsStudyNow_rg.domain.openstudy.scheduler;

import com.team.LetsStudyNow_rg.domain.openstudy.OpenStudyRoom;
import com.team.LetsStudyNow_rg.domain.openstudy.OpenStudyRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 오픈 스터디 방 자동 삭제 스케줄러
 * Spring의 @Scheduled를 사용하여 주기적으로 실행
 * 
 * 주요 기능:
 * 1. SRS 15.1.1: 생성자 혼자 5분 경과한 방 삭제
 * 2. SRS 15.1.2, 15.1.3: 삭제 예약된 방 삭제
 * 
 * 실행 주기: 1분마다 (60초)
 * 시작 지연: 30초 (애플리케이션 시작 후 안정화 시간 확보)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoomCleanupScheduler {
    
    private final OpenStudyRoomService openStudyRoomService;
    
    /**
     * SRS 15.1.1: 생성자 혼자 5분 경과한 방 자동 삭제
     * 
     * 실행 조건:
     * - 매 1분마다 실행 (fixedRate = 60000ms)
     * - 애플리케이션 시작 30초 후부터 실행 (initialDelay = 30000ms)
     * 
     * 동작 과정:
     * 1. 생성 후 5분 동안 생성자 혼자 있는 방 조회
     * 2. 각 방을 순회하며 삭제 처리
     * 3. 삭제 실패 시 로그 기록 후 계속 진행 (다른 방 삭제에 영향 없음)
     * 
     * 예외 처리:
     * - 개별 방 삭제 실패: 해당 방만 스킵하고 나머지 방은 계속 처리
     * - 전체 로직 실패: 에러 로그 남기고 다음 실행 주기까지 대기
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    public void deleteAloneRooms() {
        log.debug("=== 생성자 혼자 있는 방 확인 시작 ===");
        
        try {
            // 5분 전에 생성됐고 현재도 1명인 방 목록 조회
            List<OpenStudyRoom> aloneRooms = openStudyRoomService.getAloneRoomsExpired();
            
            if (aloneRooms.isEmpty()) {
                log.debug("생성자 혼자 5분 경과한 방 없음");
            } else {
                log.info("생성자 혼자 5분 경과한 방 {}개 발견", aloneRooms.size());
                
                // 각 방을 순회하며 삭제 처리
                for (OpenStudyRoom room : aloneRooms) {
                    try {
                        log.info("방 자동 삭제 시도 (생성자 혼자) - 방ID: {}, 제목: {}, 현재인원: {}, 혼자타이머: {}",
                            room.getId(), room.getTitle(), room.getCurrentParticipants(), room.getAloneTimerStartedAt());
                        
                        openStudyRoomService.deleteAloneRoom(room.getId(), 
                            "5분 동안 다른 참여자가 없어 방이 삭제됩니다.");
                        
                        log.info("방 자동 삭제 완료 (생성자 혼자) - 방ID: {}, 제목: {}",
                            room.getId(), room.getTitle());
                    } catch (Exception e) {
                        // 개별 방 삭제 실패 시 로그만 남기고 계속 진행
                        log.error("방 삭제 실패 - 방ID: {}, 오류: {}", room.getId(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            // 전체 스케줄러 로직 실패 시 에러 로그
            log.error("생성자 혼자 있는 방 확인 중 오류: {}", e.getMessage(), e);
        }
        
        log.debug("=== 생성자 혼자 있는 방 확인 종료 ===");
    }
    
    /**
     * SRS 15.1.2, 15.1.3: 삭제 예정 시간이 지난 방 자동 삭제
     * 
     * 실행 조건:
     * - 매 1분마다 실행 (fixedRate = 60000ms)
     * - 애플리케이션 시작 30초 후부터 실행 (initialDelay = 30000ms)
     * 
     * 삭제 대상:
     * - 15.1.2: 참여자가 1명이 되어 5분 경과한 방
     * - 15.1.3: 빈 방(0명)이 되어 5분 경과한 방
     * 
     * 동작 과정:
     * 1. PENDING_DELETE 상태이고 deleteScheduledAt 시간이 지난 방 조회
     * 2. 각 방을 순회하며 삭제 처리
     * 3. 삭제 사유를 로그에 기록 (빈 방 vs 1명 남은 방)
     * 
     * 예외 처리:
     * - deleteAloneRooms()와 동일한 방식으로 처리
     */
    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    public void deleteScheduledRooms() {
        log.debug("=== 삭제 예정 방 확인 시작 ===");
        
        try {
            // 삭제 예정 시간이 지난 방 목록 조회
            List<OpenStudyRoom> roomsToDelete = openStudyRoomService.getRoomsToDelete();
            
            if (roomsToDelete.isEmpty()) {
                log.debug("삭제 대상 방 없음");
            } else {
                log.info("삭제 대상 방 {}개 발견", roomsToDelete.size());
                
                // 각 방을 순회하며 삭제 처리
                for (OpenStudyRoom room : roomsToDelete) {
                    try {
                        // 삭제 사유 결정 (로그용)
                        String reason = room.getCurrentParticipants() == 0 
                            ? "빈 방 5분 경과" 
                            : "방 참여자가 2명 미만이 되어 5분 후 방을 삭제합니다.";
                        
                        log.info("방 자동 삭제 시도 - 방ID: {}, 제목: {}, 현재인원: {}, 상태: {}, 삭제예정: {}, 사유: {}",
                            room.getId(), room.getTitle(), room.getCurrentParticipants(), 
                            room.getStatus(), room.getDeleteScheduledAt(), reason);
                        
                        openStudyRoomService.deleteRoom(room.getId());
                        
                        log.info("방 자동 삭제 완료 - 방ID: {}, 제목: {}, 사유: {}",
                            room.getId(), room.getTitle(), reason);
                    } catch (Exception e) {
                        // 개별 방 삭제 실패 시 로그만 남기고 계속 진행
                        log.error("방 삭제 실패 - 방ID: {}, 오류: {}", room.getId(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            // 전체 스케줄러 로직 실패 시 에러 로그
            log.error("삭제 예정 방 확인 중 오류: {}", e.getMessage(), e);
        }
        
        log.debug("=== 삭제 예정 방 확인 종료 ===");
    }
}
