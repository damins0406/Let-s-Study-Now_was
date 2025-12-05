package com.team.LetsStudyNow_rg.domain.openstudy;

import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.OpenStudyRoomCreateDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.OpenStudyRoomListDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.ParticipantResponseDto;
import com.team.LetsStudyNow_rg.domain.openstudy.dto.RoomJoinResultDto;
import com.team.LetsStudyNow_rg.domain.openstudy.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "2. 오픈 스터디 룸 API", description = "오픈 스터디 방 생성, 조회, 참여, 나가기 등 오픈 스터디 관련 API")
@RestController
@RequestMapping("/api/open-study")
@RequiredArgsConstructor
@Slf4j
public class OpenStudyRoomController {
    
    private final OpenStudyRoomService openStudyRoomService;
    private final MemberRepository memberRepository;
    private final ParticipantService participantService;
    
    /**
     * 공부 분야 목록 조회
     */
    @Operation(
        summary = "공부 분야 목록 조회", 
        description = "오픈 스터디 방 생성 및 필터링에 사용 가능한 공부 분야 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공부 분야 목록 조회 성공")
    })
    @GetMapping("/study-fields")
    public ResponseEntity<?> getStudyFields() {
        List<String> studyFields = Arrays.stream(StudyField.values())
            .map(StudyField::getDescription)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "studyFields", studyFields
        ));
    }
    
    /**
     * 오픈 스터디 방 생성
     */
    @Operation(summary = "오픈 스터디 방 생성", description = "새로운 오픈 스터디 방을 생성합니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "방 생성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(
        @Valid @RequestBody OpenStudyRoomCreateDto dto,
        @AuthenticationPrincipal CustomUser user
    ) {
        Member member = memberRepository.findById(user.id)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));
        
        OpenStudyRoom room = openStudyRoomService.createRoom(dto, member);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "success", true,
                "message", "방이 생성되었습니다",
                "roomId", room.getId()
            ));
    }
    
    /**
     * 활성 방 목록 조회 (공부 분야 필터링 및 페이지네이션 지원)
     */
    @Operation(
        summary = "활성 방 목록 조회 (필터링 및 페이지네이션 지원)", 
        description = "현재 활성화된 오픈 스터디 방 목록을 페이지 단위로 조회합니다. " +
                      "studyField 파라미터로 공부 분야별 필터링이 가능하며, 생략 시 최신 생성 순으로 전체 조회합니다. " +
                      "한 페이지당 10개의 방을 표시하며, page 파라미터로 페이지 번호를 지정할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 공부 분야 또는 페이지 번호)")
    })
    @GetMapping("/rooms")
    public ResponseEntity<?> getRoomList(
        @RequestParam(required = false) String studyField,
        @RequestParam(defaultValue = "1") int page
    ) {
        log.info("방 목록 조회 요청 - studyField: {}, page: {}", studyField, page);
        
        try {
            var pageResponse = openStudyRoomService.getRoomListByStudyFieldWithPagination(studyField, page);
            
            log.info("방 목록 조회 완료 - 결과 개수: {}, 전체 페이지: {}", 
                    pageResponse.content().size(), pageResponse.totalPages());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", pageResponse
            ));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("방 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "방 목록 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
    
    /**
     * 방 참여
     */
    @Operation(summary = "방 참여", description = "지정된 오픈 스터디 방에 참여합니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 참여 성공"),
            @ApiResponse(responseCode = "400", description = "방이 가득 찼거나 이미 참여 중인 경우"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<?> joinRoom(
        @PathVariable Long roomId,
        @AuthenticationPrincipal CustomUser user
    ) {
        Member member = memberRepository.findById(user.id)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));
        
        RoomJoinResultDto result = openStudyRoomService.joinRoom(roomId, member);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 방 나가기
     */
    @Operation(summary = "방 나가기", description = "현재 참여 중인 오픈 스터디 방에서 나갑니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 나가기 성공"),
            @ApiResponse(responseCode = "400", description = "참여하지 않은 방이거나 잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(
        @PathVariable Long roomId,
        @AuthenticationPrincipal CustomUser user
    ) {
        Member member = memberRepository.findById(user.id)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));
        
        openStudyRoomService.leaveRoom(roomId, member);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "방에서 나갔습니다"
        ));
    }
    
    /**
     * 방 상세 조회
     */
    @Operation(summary = "방 상세 조회", description = "특정 오픈 스터디 방의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long roomId) {
        OpenStudyRoom room = openStudyRoomService.getRoomById(roomId);
        return ResponseEntity.ok(OpenStudyRoomListDto.from(room));
    }
    
    /**
     * 오픈스터디방 참여자 목록 조회
     */
    @Operation(
        summary = "오픈스터디방 참여자 목록 조회",
        description = "특정 오픈스터디방에 현재 참여 중인 모든 참여자의 정보를 조회합니다. " +
                     "각 참여자의 memberId, username, profileImage, 공부/휴식 상태(TimerStatus)를 포함합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 roomId")
    })
    @GetMapping("/rooms/{roomId}/participants")
    public ResponseEntity<List<ParticipantResponseDto>> getParticipants(
            @Parameter(description = "오픈스터디방 ID", required = true, example = "1")
            @PathVariable Long roomId) {
        
        List<ParticipantResponseDto> participants = participantService.getParticipantsByRoomId(roomId);
        
        return ResponseEntity.ok(participants);
    }
}
