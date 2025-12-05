package com.team.LetsStudyNow_rg.domain.openstudy;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.OpenStudyRoomCreateDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.OpenStudyRoomListDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.PageResponseDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.RoomJoinResultDto;
import com.team.LetsStudyNow_rg.domain.openstudy.exception.AlreadyInRoomException;
import com.team.LetsStudyNow_rg.domain.openstudy.exception.RoomDeletingException;
import com.team.LetsStudyNow_rg.domain.openstudy.exception.RoomFullException;
import com.team.LetsStudyNow_rg.domain.openstudy.exception.RoomNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 오픈 스터디 방의 비즈니스 로직을 처리하는 서비스
 * 방 생성, 참여/나가기, 삭제 등의 핵심 기능 제공
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OpenStudyRoomService {

    private final OpenStudyRoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final com.team.LetsStudyNow_rg.domain.studyroom.service.StudySessionService studySessionService;
    private final com.team.LetsStudyNow_rg.domain.timer.service.PersonalTimerService personalTimerService;

    /**
     * 새로운 오픈 스터디 방 생성
     *
     * 생성 과정:
     * 1. 생성자가 이미 다른 방에 참여 중인지 검증
     * 2. 방 엔티티 생성 (생성자는 자동으로 첫 번째 참여자가 됨)
     * 3. 생성자 혼자 5분 타이머 시작
     * 4. 방 저장 후 생성자를 참여자 테이블에 추가
     * 5. 공부 세션 및 타이머 자동 시작
     *
     * @param dto 방 생성 정보 (제목, 설명, 공부 분야, 최대 인원)
     * @param creator 방을 생성하는 회원
     * @return 생성된 방 엔티티
     * @throws AlreadyInRoomException 이미 다른 방에 참여 중인 경우
     */
    public OpenStudyRoom createRoom(OpenStudyRoomCreateDto dto, Member creator) {
        log.info("방 생성 시도 - 제목: {}, 생성자: {}", dto.title(), creator.getUsername());

        // 생성자가 이미 활성 상태인 다른 방에 참여 중인지 확인
        participantRepository.findActiveRoomByMemberId(creator.getId())
            .ifPresent(participant -> {
                throw new AlreadyInRoomException("방을 생성하려면 먼저 현재 방에서 나가야 합니다");
            });

        // 공부 분야 변환 (한글 → Enum)
        StudyField studyField = StudyField.fromDescription(dto.studyField());
        if (studyField == null) {
            throw new IllegalArgumentException("유효하지 않은 공부 분야입니다: " + dto.studyField());
        }

        // 방 엔티티 생성 (currentParticipants는 1로 시작 = 생성자)
        OpenStudyRoom room = OpenStudyRoom.builder()
            .title(dto.title())
            .description(dto.description())
            .studyField(studyField)
            .maxParticipants(dto.maxParticipants())
            .currentParticipants(1)
            .creator(creator)
            .status(RoomStatus.ACTIVE)
            .build();

        // SRS 15.1.1: 생성자 혼자 5분 타이머 시작
        // 5분 동안 다른 참여자가 없으면 방 자동 삭제
        room.startAloneTimer();

        OpenStudyRoom savedRoom = roomRepository.save(room);

        // 생성자를 참여자 테이블에 추가
        RoomParticipant participant = RoomParticipant.builder()
            .room(savedRoom)
            .member(creator)
            .build();

        participantRepository.save(participant);

        // ✅ 공부 세션 시작
        studySessionService.startStudySession(creator.getId(), "OPEN_STUDY", savedRoom.getId());
        
        // ✅ PersonalTimer 자동 시작 (공부 상태로 시작)
        try {
            personalTimerService.startTimer(creator.getId(), savedRoom.getId(), true);
            log.info("타이머 자동 시작 완료 - 회원: {}, 방ID: {}", creator.getUsername(), savedRoom.getId());
        } catch (IllegalStateException e) {
            log.warn("타이머 시작 실패 (이미 활성 타이머 존재) - 회원: {}", creator.getUsername());
        }

        log.info("방 생성 완료 - ID: {}, 제목: {}, 혼자타이머: {}",
            savedRoom.getId(), savedRoom.getTitle(), savedRoom.getAloneTimerStartedAt());

        return savedRoom;
    }

    /**
     * 공부 분야별로 필터링된 방 목록 조회 (페이지네이션)
     * studyFieldStr이 null이거나 빈 문자열이면 최신 생성 순으로 전체 조회
     * 한 페이지당 10개씩 고정
     *
     * @param studyFieldStr 공부 분야 (null이면 전체 조회)
     * @param page 페이지 번호 (1부터 시작)
     * @return 페이지네이션 응답 DTO
     */
    @Transactional(readOnly = true)
    public PageResponseDto<OpenStudyRoomListDto> getRoomListByStudyFieldWithPagination(String studyFieldStr, int page) {
        log.info("공부 분야별 방 목록 조회 (페이징) - 필터: '{}', 페이지: {}", studyFieldStr, page);
        
        // 페이지 번호 검증 (1부터 시작)
        if (page < 1) {
            throw new IllegalArgumentException("페이지 번호는 1 이상이어야 합니다");
        }
        
        // Pageable 생성
        Pageable pageable = PageRequest.of(page - 1, 10);
        
        Page<OpenStudyRoom> roomPage;
        
        // 공부 분야가 지정되지 않은 경우 최신 생성 순으로 전체 조회
        if (studyFieldStr == null || studyFieldStr.trim().isEmpty()) {
            log.info("필터 없음 - 전체 방 목록 조회 (페이징)");
            roomPage = roomRepository.findByStatusInOrderByCreatedAtDesc(
                    List.of(RoomStatus.ACTIVE, RoomStatus.PENDING_DELETE),
                    pageable);
        } else {
            StudyField studyField = StudyField.fromDescription(studyFieldStr.trim());
            if (studyField == null) {
                log.error("유효하지 않은 공부 분야: '{}'", studyFieldStr);
                throw new IllegalArgumentException("유효하지 않은 공부 분야입니다: " + studyFieldStr);
            }
            
            log.info("공부 분야 필터 적용: {} ({})", studyField, studyField.getDescription());
            roomPage = roomRepository.findByStudyFieldAndStatusInOrderByCreatedAtDesc(
                    studyField,
                    List.of(RoomStatus.ACTIVE, RoomStatus.PENDING_DELETE),
                    pageable);
        }
        
        log.info("조회된 방 개수: {}, 전체 페이지: {}, 전체 데이터: {}", 
                roomPage.getContent().size(), roomPage.getTotalPages(), roomPage.getTotalElements());

        // Entity를 DTO로 변환
        Page<OpenStudyRoomListDto> dtoPage = roomPage.map(OpenStudyRoomListDto::from);
        
        return PageResponseDto.of(dtoPage, page);
    }

    /**
     * 오픈 스터디 방에 참여
     *
     * 참여 과정 및 검증:
     * 1. 이미 다른 방에 참여 중인지 확인
     * 2. 방이 존재하는지 확인
     * 3. 방 상태 확인 (PENDING_DELETE나 DELETED 상태면 참여 불가)
     * 4. 정원 초과 여부 확인
     * 5. 이미 해당 방에 참여 중인지 확인
     * 6. 참여자 추가 및 현재 인원 증가
     * 7. 공부 세션 및 타이머 자동 시작
     * 8. 특수 상황 처리:
     *    - 2명이 되면: 생성자 혼자 타이머 취소
     *    - 삭제 예정 상태에서 2명 이상이 되면: 삭제 예약 취소
     *
     * @param roomId 참여할 방의 ID
     * @param member 참여하려는 회원
     * @return 참여 결과 DTO
     * @throws AlreadyInRoomException 이미 다른 방에 참여 중인 경우
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우
     * @throws RoomDeletingException 삭제 예정 상태의 방인 경우
     * @throws RoomFullException 방이 가득 찬 경우
     */
    public RoomJoinResultDto joinRoom(Long roomId, Member member) {
        log.info("방 참여 시도 - 방ID: {}, 회원: {}", roomId, member.getUsername());

        // 이미 다른 활성 방에 참여 중인지 확인 (한 번에 하나의 방만 참여 가능)
        participantRepository.findActiveRoomByMemberId(member.getId())
            .ifPresent(participant -> {
                throw new AlreadyInRoomException();
            });

        // 방 조회
        OpenStudyRoom room = roomRepository.findById(roomId)
            .orElseThrow(RoomNotFoundException::new);

        // SRS 15.1.4: 삭제 예정 방은 새로운 참여 불가
        if (room.getStatus() == RoomStatus.PENDING_DELETE) {
            throw new RoomDeletingException();
        }

        // 이미 삭제된 방인지 확인
        if (room.getStatus() == RoomStatus.DELETED) {
            throw new RoomNotFoundException("이미 삭제된 방입니다");
        }

        // 정원 확인 (동시 접속으로 인한 정원 초과 방지)
        if (room.isFull()) {
            throw new RoomFullException();
        }

        // 이미 해당 방에 참여 중인지 확인 (중복 참여 방지)
        if (participantRepository.existsByRoomIdAndMemberId(roomId, member.getId())) {
            throw new AlreadyInRoomException("이미 해당 방에 참여 중입니다");
        }

        // 참여자 테이블에 추가
        RoomParticipant participant = RoomParticipant.builder()
            .room(room)
            .member(member)
            .build();

        participantRepository.save(participant);
        room.incrementParticipants();

        // ✅ 공부 세션 시작
        studySessionService.startStudySession(member.getId(), "OPEN_STUDY", roomId);
        
        // ✅ PersonalTimer 자동 시작 (공부 상태로 시작)
        try {
            personalTimerService.startTimer(member.getId(), roomId, false);
            log.info("타이머 자동 시작 완료 - 회원: {}, 방ID: {}", member.getUsername(), roomId);
        } catch (IllegalStateException e) {
            log.warn("타이머 시작 실패 (이미 활성 타이머 존재) - 회원: {}", member.getUsername());
        }

        // 2명이 되면: 생성자 혼자 타이머 취소 (더 이상 자동 삭제되지 않음)
        if (room.getCurrentParticipants() == 2) {
            room.resetAloneTimer();
            log.info("생성자 혼자 타이머 취소 - 방ID: {}", roomId);
        }

        // 삭제 예정 상태였는데 2명 이상이 되면: 삭제 예약 취소하고 활성화
        if (room.getStatus() == RoomStatus.PENDING_DELETE && room.getCurrentParticipants() >= 2) {
            room.cancelDeleteSchedule();
            log.info("방 삭제 예약 취소 - 방ID: {}, 현재인원: {}", roomId, room.getCurrentParticipants());
        }

        log.info("방 참여 완료 - 방ID: {}, 회원: {}, 현재인원: {}/{}",
            roomId, member.getUsername(), room.getCurrentParticipants(), room.getMaxParticipants());

        return RoomJoinResultDto.success(roomId);
    }

    /**
     * 오픈 스터디 방에서 나가기
     *
     * 나가기 과정:
     * 1. 해당 방에 참여 중인지 확인
     * 2. 공부 세션 종료 및 타이머 종료 (레벨업 처리)
     * 3. 참여자 테이블에서 제거
     * 4. 현재 인원 감소
     * 5. 남은 인원에 따른 자동 삭제 예약:
     *    - SRS 15.1.2: 1명 남음 → 5분 후 삭제 예약
     *    - SRS 15.1.3: 0명 남음 (빈 방) → 5분 후 삭제 예약
     *
     * @param roomId 나갈 방의 ID
     * @param member 나가려는 회원
     * @throws IllegalStateException 해당 방에 참여하고 있지 않은 경우
     */
    public void leaveRoom(Long roomId, Member member) {
        log.info("방 나가기 시도 - 방ID: {}, 회원: {}", roomId, member.getUsername());

        // 참여자 정보 조회 (없으면 예외 발생)
        RoomParticipant participant = participantRepository.findByRoomIdAndMemberId(roomId, member.getId())
            .orElseThrow(() -> new IllegalStateException("해당 방에 참여하고 있지 않습니다"));

        OpenStudyRoom room = participant.getRoom();

        // ✅ 공부 세션 종료 (레벨업 처리)
        com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
            studySessionService.getActiveSession(member.getId());
        if (activeSession != null) {
            com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                studySessionService.endStudySession(activeSession.getId());
            log.info("공부 세션 종료 - 회원: {}, 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                     member.getUsername(), result.studyMinutes(), result.leveledUp(), result.newLevel());
        }
        
        // ✅ PersonalTimer 종료
        try {
            personalTimerService.endTimer(member.getId());
            log.info("타이머 종료 완료 - 회원: {}", member.getUsername());
        } catch (IllegalArgumentException e) {
            log.warn("타이머 종료 실패 (활성 타이머 없음) - 회원: {}", member.getUsername());
        }

        // 참여자 테이블에서 제거 및 현재 인원 감소
        participantRepository.delete(participant);
        room.decrementParticipants();

        // 나가기 전 인원 체크 (디버깅용)
        int remainingCount = room.getCurrentParticipants();
        log.info("방 나가기 완료 - 방ID: {}, 회원: {}, 남은인원: {}", roomId, member.getUsername(), remainingCount);

        // SRS 15.1.2: 1명 남으면 5분 후 삭제 예약
        if (remainingCount == 1) {
            room.scheduleDelete();
            log.info("방 삭제 예약 (1명 남음) - 방ID: {}, 삭제예정시간: {}, 현재인원: {}", 
                roomId, room.getDeleteScheduledAt(), remainingCount);
        }
        // SRS 15.1.3: 빈 방이 되면 5분 후 삭제 예약
        else if (remainingCount == 0) {
            room.scheduleDelete();
            log.info("방 삭제 예약 (빈 방) - 방ID: {}, 삭제예정시간: {}, 현재인원: {}", 
                roomId, room.getDeleteScheduledAt(), remainingCount);
        }
    }

    /**
     * 방 ID로 방 상세 정보 조회
     *
     * @param roomId 조회할 방의 ID
     * @return 방 엔티티
     * @throws RoomNotFoundException 방을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public OpenStudyRoom getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
            .orElseThrow(RoomNotFoundException::new);
    }

    /**
     * 삭제 예정 시간이 지난 방 목록 조회
     * 스케줄러가 주기적으로 호출하여 삭제할 방을 찾음
     *
     * @return 삭제 대상 방 목록
     */
    @Transactional(readOnly = true)
    public List<OpenStudyRoom> getRoomsToDelete() {
        return roomRepository.findRoomsToDelete(RoomStatus.PENDING_DELETE, LocalDateTime.now());
    }

    /**
     * 생성자 혼자 5분 경과한 방 목록 조회
     * SRS 15.1.1: 생성 후 5분 동안 혼자인 방 자동 삭제용
     * 스케줄러가 주기적으로 호출
     *
     * @return 삭제 대상 방 목록 (생성자 혼자 5분 경과)
     */
    @Transactional(readOnly = true)
    public List<OpenStudyRoom> getAloneRoomsExpired() {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        return roomRepository.findAloneRoomsExpired(fiveMinutesAgo);
    }

    /**
     * 방 삭제 처리 (스케줄러 전용)
     * SRS 15.1.2, 15.1.3: 삭제 예약된 방을 실제로 삭제
     *
     * 삭제 과정:
     * 1. 방이 존재하는지 확인
     * 2. 현재 인원이 1 이하인지 재확인 (동시성 문제 방지)
     * 3. 모든 참여자의 세션과 타이머 종료 (레벨업 처리)
     * 4. 모든 참여자를 DB에서 삭제
     * 5. currentParticipants를 0으로 업데이트
     * 6. 방 상태를 DELETED로 변경 (Soft Delete)
     *
     * @param roomId 삭제할 방의 ID
     */
    public void deleteRoom(Long roomId) {
        OpenStudyRoom room = roomRepository.findById(roomId)
            .orElse(null);

        if (room != null) {
            log.info("방 삭제 시작 - 방ID: {}, 제목: {}, 현재인원: {}, 상태: {}, 삭제예정시간: {}",
                roomId, room.getTitle(), room.getCurrentParticipants(), room.getStatus(), room.getDeleteScheduledAt());

            // 안전장치: 현재 인원이 2명 이상이면 삭제하지 않음
            if (room.getCurrentParticipants() >= 2) {
                log.warn("방 삭제 취소 - 현재 인원이 2명 이상 - 방ID: {}, 현재인원: {}", 
                    roomId, room.getCurrentParticipants());
                // 삭제 예약도 취소
                room.cancelDeleteSchedule();
                return;
            }

            // 삭제 전 참여자 수 기록 (로깅용)
            int participantCount = room.getCurrentParticipants();

            // ✅ 모든 참여자의 세션과 타이머 종료 (레벨업 처리)
            List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
            log.info("방 삭제 - 참여자 수: {} (DB 조회)", participants.size());
            
            for (RoomParticipant participant : participants) {
                Long memberId = participant.getMember().getId();
                String username = participant.getMember().getUsername();
                
                // 공부 세션 종료 (레벨업 처리)
                com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
                    studySessionService.getActiveSession(memberId);
                if (activeSession != null) {
                    com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                        studySessionService.endStudySession(activeSession.getId());
                    log.info("방 삭제 - 참여자 세션 종료 - 회원: {} (ID: {}), 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                             username, memberId, result.studyMinutes(), result.leveledUp(), result.newLevel());
                } else {
                    log.warn("방 삭제 - 참여자 활성 세션 없음 - 회원: {} (ID: {})", username, memberId);
                }
                
                // PersonalTimer 종료
                try {
                    personalTimerService.endTimer(memberId);
                    log.info("방 삭제 - 참여자 타이머 종료 완료 - 회원: {} (ID: {})", username, memberId);
                } catch (IllegalArgumentException e) {
                    log.warn("방 삭제 - 참여자 타이머 종료 실패 (활성 타이머 없음) - 회원: {} (ID: {})", username, memberId);
                }
            }

            // 참여자 테이블에서 모두 제거
            participantRepository.deleteByRoomId(roomId);
            log.info("방 삭제 - 참여자 테이블에서 {} 명 삭제", participants.size());

            // 방 엔티티의 currentParticipants를 0으로 업데이트
            // (방 상세 조회 시 정확한 참여자 수 표시를 위함)
            while (room.getCurrentParticipants() > 0) {
                room.decrementParticipants();
            }

            // Soft Delete: DB에서 실제 삭제하지 않고 상태만 변경
            // (데이터 보존 및 이력 관리를 위함)
            room.delete();

            log.info("방 삭제 완료 - 방ID: {}, 삭제된 참여자 수: {}", roomId, participantCount);
        } else {
            log.warn("방 삭제 실패 - 방을 찾을 수 없음 - 방ID: {}", roomId);
        }
    }

    /**
     * 생성자 혼자 있는 방 삭제 (SRS 15.1.1 전용)
     * 생성 후 5분 동안 다른 참여자가 없는 방을 삭제
     *
     * deleteRoom()과 유사하지만, 생성자의 세션과 타이머도 종료
     *
     * @param roomId 삭제할 방의 ID
     * @param reason 삭제 사유 (로깅용)
     */
    public void deleteAloneRoom(Long roomId, String reason) {
        OpenStudyRoom room = roomRepository.findById(roomId)
            .orElse(null);

        if (room != null) {
            log.info("방 삭제 시작 (생성자 혼자) - 방ID: {}, 제목: {}, 사유: {}, 현재인원: {}, 혼자타이머: {}",
                roomId, room.getTitle(), reason, room.getCurrentParticipants(), room.getAloneTimerStartedAt());

            // 안전장치: 현재 인원이 2명 이상이면 삭제하지 않음
            if (room.getCurrentParticipants() >= 2) {
                log.warn("방 삭제 취소 (생성자 혼자 아님) - 방ID: {}, 현재인원: {}", 
                    roomId, room.getCurrentParticipants());
                // 혼자 타이머도 리셋
                room.resetAloneTimer();
                return;
            }

            // ✅ 모든 참여자(생성자)의 세션과 타이머 종료
            List<RoomParticipant> participants = participantRepository.findByRoomId(roomId);
            log.info("방 삭제(생성자 혼자) - 참여자 수: {} (DB 조회)", participants.size());
            
            for (RoomParticipant participant : participants) {
                Long memberId = participant.getMember().getId();
                String username = participant.getMember().getUsername();
                
                // 공부 세션 종료 (레벨업 처리)
                com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
                    studySessionService.getActiveSession(memberId);
                if (activeSession != null) {
                    com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                        studySessionService.endStudySession(activeSession.getId());
                    log.info("방 삭제(생성자 혼자) - 세션 종료 - 회원: {} (ID: {}), 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                             username, memberId, result.studyMinutes(), result.leveledUp(), result.newLevel());
                } else {
                    log.warn("방 삭제(생성자 혼자) - 활성 세션 없음 - 회원: {} (ID: {})", username, memberId);
                }
                
                // PersonalTimer 종료
                try {
                    personalTimerService.endTimer(memberId);
                    log.info("방 삭제(생성자 혼자) - 타이머 종료 완료 - 회원: {} (ID: {})", username, memberId);
                } catch (IllegalArgumentException e) {
                    log.warn("방 삭제(생성자 혼자) - 타이머 종료 실패 (활성 타이머 없음) - 회원: {} (ID: {})", username, memberId);
                }
            }

            // 참여자 테이블에서 모두 제거
            participantRepository.deleteByRoomId(roomId);
            log.info("방 삭제(생성자 혼자) - 참여자 테이블에서 {} 명 삭제", participants.size());

            // currentParticipants를 0으로 업데이트
            while (room.getCurrentParticipants() > 0) {
                room.decrementParticipants();
            }

            // Soft Delete
            room.delete();

            log.info("방 삭제 완료 (생성자 혼자) - 방ID: {}", roomId);
        } else {
            log.warn("방 삭제 실패 (생성자 혼자) - 방을 찾을 수 없음 - 방ID: {}", roomId);
        }
    }
}
