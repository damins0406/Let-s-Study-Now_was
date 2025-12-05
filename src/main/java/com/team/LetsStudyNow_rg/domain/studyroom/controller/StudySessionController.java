package com.team.LetsStudyNow_rg.domain.studyroom.controller;

import com.team.LetsStudyNow_rg.domain.studyroom.dto.LevelInfoDto;
import com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionEndResultDto;
import com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionResponseDto;
import com.team.LetsStudyNow_rg.domain.studyroom.dto.SessionStartRequestDto;
import com.team.LetsStudyNow_rg.domain.studyroom.entity.StudySession;
import com.team.LetsStudyNow_rg.domain.studyroom.service.LevelUpService;
import com.team.LetsStudyNow_rg.domain.studyroom.service.StudySessionService;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 공부 세션 관리 컨트롤러
 */
@Tag(name = "StudySession", description = "공부 세션 및 레벨업 API")
@RestController
@RequestMapping("/api/study-sessions")
@RequiredArgsConstructor
public class StudySessionController {
    
    private final StudySessionService studySessionService;
    private final LevelUpService levelUpService;
    
    /**
     * 공부 세션 시작
     * POST /api/study-sessions/start
     */
    @Operation(
        summary = "공부 세션 시작",
        description = "오픈스터디 또는 그룹스터디 방 입장 시 공부 세션을 시작합니다. ")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "세션 시작 성공",
            content = @Content(schema = @Schema(implementation = SessionResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (studyType 또는 roomId 누락)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "회원을 찾을 수 없음",
            content = @Content
        )
    })
    @PostMapping("/start")
    public ResponseEntity<SessionResponseDto> startSession(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "세션 시작 요청 정보",
                required = true,
                content = @Content(schema = @Schema(implementation = SessionStartRequestDto.class))
            )
            @Valid @RequestBody SessionStartRequestDto request) {
        
        StudySession session = studySessionService.startStudySession(
                user.id, 
                request.studyType(), 
                request.roomId()
        );
        
        return ResponseEntity.ok(SessionResponseDto.from(session));
    }
    
    /**
     * 공부 세션 종료
     * POST /api/study-sessions/{sessionId}/end
     */
    @Operation(
        summary = "공부 세션 종료 및 레벨업 처리"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "세션 종료 성공 (레벨업 여부 포함)",
            content = @Content(schema = @Schema(implementation = SessionEndResultDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 종료된 세션",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "세션을 찾을 수 없음",
            content = @Content
        )
    })
    @PostMapping("/{sessionId}/end")
    public ResponseEntity<SessionEndResultDto> endSession(
            @Parameter(description = "종료할 세션 ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        
        SessionEndResultDto result = studySessionService.endStudySession(sessionId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 내 활성 세션 조회
     * GET /api/study-sessions/active
     */
    @Operation(
        summary = "내 활성 세션 조회",
        description = "현재 로그인한 사용자의 진행 중인 공부 세션을 조회합니다. " +
                     "활성 세션이 없으면 204 No Content를 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "활성 세션 조회 성공",
            content = @Content(schema = @Schema(implementation = SessionResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "204",
            description = "활성 세션 없음",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content
        )
    })
    @GetMapping("/active")
    public ResponseEntity<SessionResponseDto> getActiveSession(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser user) {
        
        StudySession session = studySessionService.getActiveSession(user.id);
        
        if (session == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(SessionResponseDto.from(session));
    }
    
    /**
     * 내 레벨 정보 조회
     * GET /api/study-sessions/level
     */
    @Operation(
        summary = "내 레벨 정보 조회",
        description = "현재 레벨, 총 경험치, 다음 레벨까지 필요한 경험치, 진행률 등 레벨 관련 모든 정보를 한 번에 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "레벨 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = LevelInfoDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (로그인 필요)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "회원을 찾을 수 없음",
            content = @Content
        )
    })
    @GetMapping("/level")
    public ResponseEntity<LevelInfoDto> getLevelInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUser user) {
        
        LevelInfoDto levelInfo = levelUpService.getLevelInfo(user.id);
        return ResponseEntity.ok(levelInfo);
    }
}
