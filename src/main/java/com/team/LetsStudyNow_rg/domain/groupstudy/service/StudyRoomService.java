package com.team.LetsStudyNow_rg.domain.groupstudy.service;

import com.team.LetsStudyNow_rg.domain.groupstudy.domain.Group;
import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoom;
import com.team.LetsStudyNow_rg.domain.groupstudy.domain.StudyRoomParticipant;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.CreateStudyRoomRequest;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.StudyRoomParticipantResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.StudyRoomResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupMemberRepository;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.GroupRepository;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.StudyRoomParticipantRepository;
import com.team.LetsStudyNow_rg.domain.groupstudy.repository.StudyRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final com.team.LetsStudyNow_rg.domain.studyroom.service.StudySessionService studySessionService;
    private final com.team.LetsStudyNow_rg.domain.timer.service.PersonalTimerService personalTimerService;

    // 생성자 주입
    public StudyRoomService(StudyRoomRepository studyRoomRepository,
                            StudyRoomParticipantRepository participantRepository,
                            GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            com.team.LetsStudyNow_rg.domain.studyroom.service.StudySessionService studySessionService,
                            com.team.LetsStudyNow_rg.domain.timer.service.PersonalTimerService personalTimerService) {
        this.studyRoomRepository = studyRoomRepository;
        this.participantRepository = participantRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.studySessionService = studySessionService;
        this.personalTimerService = personalTimerService;
    }

    // 스터디방 생성 (SRS 6.1.1~6.1.8)
    @Transactional
    public StudyRoomResponse createRoom(CreateStudyRoomRequest request, Long creatorId) {
        // 1. 그룹 존재 확인
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다"));

        // 2. 방 생성자가 그룹 멤버인지 확인 (SRS 6.1.6)
        groupMemberRepository.findByGroupIdAndMemberId(request.getGroupId(), creatorId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 멤버만 스터디 방을 생성할 수 있습니다"));

        // 3. 방 이름 검증 (SRS 6.1.5)
        if (request.getRoomName() == null || request.getRoomName().trim().isEmpty()) {
            throw new IllegalArgumentException("방 이름을 입력해주세요");
        }

        // 4. 공부 시간 검증 (SRS 6.1.4)
        if (request.getStudyHours() < 1 || request.getStudyHours() > 5) {
            throw new IllegalArgumentException("공부 시간은 1시간에서 5시간 사이로 설정해야 합니다");
        }

        // 5. 인원 수 검증 (SRS 6.1.2)
        if (request.getMaxMembers() < 2 || request.getMaxMembers() > 10) {
            throw new IllegalArgumentException("인원 수는 2명에서 10명 사이로 설정해야 합니다");
        }

        // 6. 공부 분야 검증 (SRS 6.1.3)
        if (request.getStudyField() == null || request.getStudyField().trim().isEmpty()) {
            throw new IllegalArgumentException("공부 분야를 선택해야 합니다");
        }

        // 7. 스터디방 생성
        StudyRoom studyRoom = new StudyRoom(
                request.getGroupId(),
                request.getRoomName(),
                request.getStudyField(),
                request.getStudyHours(),
                request.getMaxMembers(),
                creatorId
        );
        StudyRoom savedRoom = studyRoomRepository.save(studyRoom);

        // 8. 방 생성자는 자동 입장 (SRS 6.1.8)
        StudyRoomParticipant participant = new StudyRoomParticipant(
                savedRoom.getId(),
                creatorId
        );
        participantRepository.save(participant);

        // ✅ 공부 세션 시작
        studySessionService.startStudySession(creatorId, "GROUP_STUDY", savedRoom.getId());
        
        // ✅ PersonalTimer 자동 시작 (공부 상태로 시작)
        try {
            personalTimerService.startTimer(creatorId, savedRoom.getId(), true);
        } catch (IllegalStateException e) {
            // 이미 활성 타이머가 있는 경우 무시 (로그는 PersonalTimerService에서 처리)
        }

        return new StudyRoomResponse(savedRoom);
    }

    // 스터디방 단건 조회
    public StudyRoomResponse getRoom(Long roomId) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));
        return new StudyRoomResponse(room);
    }

    // 그룹의 스터디방 목록 조회 (SRS 6.4.1)
    public List<StudyRoomResponse> getGroupRooms(Long groupId) {
        List<StudyRoom> rooms = studyRoomRepository.findByGroupIdAndStatus(groupId, "ACTIVE");
        return rooms.stream()
                .map(StudyRoomResponse::new)
                .collect(Collectors.toList());
    }

    // 전체 활성화된 스터디방 목록
    public List<StudyRoomResponse> getAllActiveRooms() {
        List<StudyRoom> rooms = studyRoomRepository.findAll().stream()
                .filter(room -> "ACTIVE".equals(room.getStatus()))
                .collect(Collectors.toList());
        return rooms.stream()
                .map(StudyRoomResponse::new)
                .collect(Collectors.toList());
    }

    // 스터디방 입장 (SRS 6.5.1)
    @Transactional
    public void joinRoom(Long roomId, Long memberId) {
        // 1. 스터디방 존재 확인
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));

        // 2. 그룹 멤버인지 확인 (SRS 6.5.2)
        groupMemberRepository.findByGroupIdAndMemberId(room.getGroupId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 멤버만 입장할 수 있습니다"));

        // 3. 이미 입장했는지 확인
        if (participantRepository.findByStudyRoomIdAndMemberId(roomId, memberId).isPresent()) {
            throw new IllegalArgumentException("이미 입장한 방입니다");
        }

        // 4. 최대 인원 확인 (SRS 6.4.4, 6.5.5)
        if (room.isFull()) {
            throw new IllegalArgumentException("최대 인원에 도달하여 입장이 불가합니다");
        }

        // 5. 방 종료 여부 확인
        if (room.isEnded()) {
            throw new IllegalArgumentException("이미 종료된 스터디 방입니다");
        }

        // 6. 입장 처리
        room.addParticipant();
        studyRoomRepository.save(room);

        StudyRoomParticipant participant = new StudyRoomParticipant(roomId, memberId);
        participantRepository.save(participant);

        // ✅ 공부 세션 시작
        studySessionService.startStudySession(memberId, "GROUP_STUDY", roomId);
        
        // ✅ PersonalTimer 자동 시작 (공부 상태로 시작)
        try {
            personalTimerService.startTimer(memberId, roomId, false);
        } catch (IllegalStateException e) {
            // 이미 활성 타이머가 있는 경우 무시 (로그는 PersonalTimerService에서 처리)
        }
    }

    // 스터디방 퇴장
    @Transactional
    public void leaveRoom(Long roomId, Long memberId) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));

        // 방장은 퇴장할 수 없음
        if (room.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("방 생성자는 방을 나갈 수 없습니다. 방 삭제를 이용해주세요.");
        }

        // 참여자 확인
        participantRepository.findByStudyRoomIdAndMemberId(roomId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 방입니다"));

        // ✅ 공부 세션 종료 (레벨업 처리)
        com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
            studySessionService.getActiveSession(memberId);
        if (activeSession != null) {
            studySessionService.endStudySession(activeSession.getId());
        }
        
        // ✅ PersonalTimer 종료
        try {
            personalTimerService.endTimer(memberId);
        } catch (IllegalArgumentException e) {
            // 활성 타이머가 없는 경우 무시
        }

        // 퇴장 처리
        room.removeParticipant();
        studyRoomRepository.save(room);

        participantRepository.deleteByStudyRoomIdAndMemberId(roomId, memberId);
    }

    // 스터디방 종료 (SRS 6.1.9, 6.5.3)
    @Transactional
    public void endRoom(Long roomId) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));

        log.info("스터디방 종료 시작 - 방ID: {}, 현재인원: {}", roomId, room.getCurrentMembers());

        // ✅ 모든 참여자의 세션과 타이머 종료 (레벨업 처리)
        List<StudyRoomParticipant> participants = participantRepository.findByStudyRoomId(roomId);
        for (StudyRoomParticipant participant : participants) {
            Long memberId = participant.getMemberId();
            
            // 공부 세션 종료 (레벨업 처리)
            com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
                studySessionService.getActiveSession(memberId);
            if (activeSession != null) {
                com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                    studySessionService.endStudySession(activeSession.getId());
                log.info("방 종료 - 참여자 세션 종료 - 회원ID: {}, 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                         memberId, result.studyMinutes(), result.leveledUp(), result.newLevel());
            }
            
            // PersonalTimer 종료
            try {
                personalTimerService.endTimer(memberId);
                log.info("방 종료 - 참여자 타이머 종료 완료 - 회원ID: {}", memberId);
            } catch (IllegalArgumentException e) {
                log.warn("방 종료 - 참여자 타이머 종료 실패 (활성 타이머 없음) - 회원ID: {}", memberId);
            }
        }

        // 방 종료
        room.end();
        studyRoomRepository.save(room);

        // 모든 참여자 자동 퇴장
        participantRepository.deleteByStudyRoomId(roomId);
        
        log.info("스터디방 종료 완료 - 방ID: {}, 종료된 참여자 수: {}", roomId, participants.size());
    }

    // 타이머 종료된 방 자동 종료 및 삭제
    @Transactional
    public void autoEndExpiredRooms() {
        List<StudyRoom> activeRooms = studyRoomRepository.findAll().stream()
                .filter(room -> "ACTIVE".equals(room.getStatus()))
                .filter(StudyRoom::isEnded)
                .collect(Collectors.toList());

        for (StudyRoom room : activeRooms) {
            log.info("시간 만료된 방 자동 종료 시작 - 방ID: {}, 제목: {}", room.getId(), room.getRoomName());
            
            // ✅ 모든 참여자의 세션과 타이머 종료 (레벨업 처리)
            List<StudyRoomParticipant> participants = participantRepository.findByStudyRoomId(room.getId());
            for (StudyRoomParticipant participant : participants) {
                Long memberId = participant.getMemberId();
                
                // 공부 세션 종료 (레벨업 처리)
                com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
                    studySessionService.getActiveSession(memberId);
                if (activeSession != null) {
                    com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                        studySessionService.endStudySession(activeSession.getId());
                    log.info("시간 만료 방 종료 - 참여자 세션 종료 - 회원ID: {}, 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                             memberId, result.studyMinutes(), result.leveledUp(), result.newLevel());
                }
                
                // PersonalTimer 종료
                try {
                    personalTimerService.endTimer(memberId);
                    log.info("시간 만료 방 종료 - 참여자 타이머 종료 완료 - 회원ID: {}", memberId);
                } catch (IllegalArgumentException e) {
                    log.warn("시간 만료 방 종료 - 참여자 타이머 종료 실패 (활성 타이머 없음) - 회원ID: {}", memberId);
                }
            }
            
            // 모든 참여자 삭제
            participantRepository.deleteByStudyRoomId(room.getId());
            
            // 방 완전 삭제
            studyRoomRepository.delete(room);
            
            log.info("시간 만료된 방 자동 종료 완료 - 방ID: {}, 종료된 참여자 수: {}", room.getId(), participants.size());
        }
    }

    // 스터디방 삭제 (방 생성자만 가능, 본인만 있을 때)
    @Transactional
    public void deleteRoom(Long roomId, Long memberId) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));

        // 1. 방 생성자인지 확인
        if (!room.getCreatorId().equals(memberId)) {
            throw new IllegalArgumentException("방 생성자만 방을 삭제할 수 있습니다");
        }

        // 2. 현재 참여 인원 확인 (방 생성자만 있어야 함)
        long participantCount = participantRepository.countByStudyRoomId(roomId);
        if (participantCount > 1) {
            throw new IllegalArgumentException("방에 다른 멤버가 있을 때는 삭제할 수 없습니다");
        }

        // 3. 방 생성자의 공부 세션 종료 (레벨업 처리)
        com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession activeSession = 
            studySessionService.getActiveSession(memberId);
        if (activeSession != null) {
            com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto result = 
                studySessionService.endStudySession(activeSession.getId());
            log.info("방 삭제 - 방장 세션 종료 - 회원ID: {}, 공부시간: {}분, 레벨업: {}, 새레벨: {}", 
                     memberId, result.studyMinutes(), result.leveledUp(), result.newLevel());
        }
        
        // 4. 방 생성자의 PersonalTimer 종료
        try {
            personalTimerService.endTimer(memberId);
            log.info("방 삭제 - 방장 타이머 종료 완료 - 회원ID: {}", memberId);
        } catch (IllegalArgumentException e) {
            log.warn("방 삭제 - 방장 타이머 종료 실패 (활성 타이머 없음) - 회원ID: {}", memberId);
        }

        // 5. 참여자 삭제 후 방 삭제
        participantRepository.deleteByStudyRoomId(roomId);
        studyRoomRepository.delete(room);
        
        log.info("그룹스터디 방 삭제 완료 - 방ID: {}, 방장ID: {}", roomId, memberId);
    }

    // 스터디방 참여자 목록 조회
    public List<StudyRoomParticipantResponse> getRoomParticipants(Long roomId) {
        // 스터디방 존재 여부 확인
        studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 방을 찾을 수 없습니다"));

        return participantRepository.findByStudyRoomId(roomId).stream()
                .map(StudyRoomParticipantResponse::new)
                .collect(Collectors.toList());
    }
}