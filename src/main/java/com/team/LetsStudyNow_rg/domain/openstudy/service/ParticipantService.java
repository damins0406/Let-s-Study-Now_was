package com.team.LetsStudyNow_rg.domain.openstudy.service;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.openstudy.RoomParticipant;
import com.team.LetsStudyNow_rg.domain.openstudy.RoomParticipantRepository;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.ParticipantResponseDto;
import com.team.LetsStudyNow_rg.domain.timer.entity.PersonalTimer;
import com.team.LetsStudyNow_rg.domain.timer.entity.TimerStatus;
import com.team.LetsStudyNow_rg.domain.timer.repository.PersonalTimerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 오픈스터디방 참여자 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipantService {
    
    private final RoomParticipantRepository roomParticipantRepository;
    private final PersonalTimerRepository personalTimerRepository;
    
    /**
     * 특정 오픈스터디방의 참여자 목록 조회
     * 
     * @param roomId 오픈스터디방 ID
     * @return 참여자 목록
     */
    public List<ParticipantResponseDto> getParticipantsByRoomId(Long roomId) {
        // 해당 방의 모든 RoomParticipant 조회
        List<RoomParticipant> participants = roomParticipantRepository.findByRoomId(roomId);
        
        // RoomParticipant와 PersonalTimer 정보를 결합하여 DTO 생성
        return participants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * RoomParticipant를 ParticipantResponseDto로 변환
     */
    private ParticipantResponseDto convertToDto(RoomParticipant participant) {
        Member member = participant.getMember();
        
        // PersonalTimer에서 타이머 상태 조회
        Optional<PersonalTimer> timerOpt = personalTimerRepository.findByMemberId(member.getId());
        
        // 타이머가 없으면 기본값으로 RESTING 설정
        TimerStatus timerStatus = timerOpt
                .map(PersonalTimer::getTimerStatus)
                .orElse(TimerStatus.RESTING);
        
        // DTO 생성
        return ParticipantResponseDto.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .profileImage(member.getProfileImage())
                .timerStatus(timerStatus)
                .build();
    }
}
