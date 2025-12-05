package com.team.LetsStudyNow_rg.domain.timer.controller;

import com.team.LetsStudyNow_rg.domain.timer.dto.request.PomodoroSettingRequest;
import com.team.LetsStudyNow_rg.domain.timer.dto.response.PomodoroSettingResponse;
import com.team.LetsStudyNow_rg.domain.timer.dto.response.StudyTimeResponse;
import com.team.LetsStudyNow_rg.domain.timer.dto.response.TimerStatusResponse;
import com.team.LetsStudyNow_rg.domain.timer.entity.TimerStatus;
import com.team.LetsStudyNow_rg.domain.timer.service.PersonalTimerService;
import com.team.LetsStudyNow_rg.domain.timer.service.PomodoroSettingService;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PersonalTimer", description = "개인 타이머 API")
@RestController
@RequestMapping("/api/timer")
@RequiredArgsConstructor
public class PersonalTimerController {

    private final PersonalTimerService personalTimerService;
    private final PomodoroSettingService pomodoroSettingService;

    /**
     * 타이머 시작 (방 입장 시)
     */
    @Operation(summary = "타이머 시작", description = "스터디 방 입장 시 개인 타이머를 시작합니다")
    @PostMapping("/start")
    public ResponseEntity<TimerStatusResponse> startTimer(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "false") boolean isRoomCreator) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.startTimer(memberId, roomId, isRoomCreator);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 타이머 종료 (방 퇴장 시)
     */
    @Operation(summary = "타이머 종료", description = "스터디 방 퇴장 시 개인 타이머를 종료합니다")
    @PostMapping("/end")
    public ResponseEntity<Void> endTimer(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        personalTimerService.endTimer(memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 수동 토글 (공부 ↔ 휴식)
     */
    @Operation(summary = "수동 토글", description = "공부/휴식 상태를 수동으로 전환합니다 (기본 모드)")
    @PostMapping("/toggle")
    public ResponseEntity<TimerStatusResponse> toggleTimer(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.toggleTimer(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뽀모도로 모드 시작
     */
    @Operation(summary = "뽀모도로 모드 시작", description = "뽀모도로 모드를 시작합니다")
    @PostMapping("/pomodoro/start")
    public ResponseEntity<TimerStatusResponse> startPomodoroMode(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.startPomodoroMode(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뽀모도로 모드 종료
     */
    @Operation(summary = "뽀모도로 모드 종료", description = "뽀모도로 모드를 종료하고 기본 모드로 전환합니다")
    @PostMapping("/pomodoro/stop")
    public ResponseEntity<TimerStatusResponse> stopPomodoroMode(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.stopPomodoroMode(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뽀모도로 상태 자동 전환 (공부 ↔ 휴식)
     */
    @Operation(summary = "뽀모도로 상태 전환", description = "뽀모도로 타이머 완료 시 상태를 자동으로 전환합니다")
    @PostMapping("/pomodoro/change-status")
    public ResponseEntity<TimerStatusResponse> changePomodoroStatus(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestParam TimerStatus newStatus) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.changePomodoroStatus(memberId, newStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 타이머 상태 조회
     */
    @Operation(summary = "타이머 상태 조회", description = "현재 타이머 상태를 조회합니다")
    @GetMapping("/status")
    public ResponseEntity<TimerStatusResponse> getTimerStatus(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        TimerStatusResponse response = personalTimerService.getTimerStatus(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 누적 공부 시간 조회
     */
    @Operation(summary = "누적 시간 조회", description = "총 누적 시간과 오늘의 공부 시간을 조회합니다")
    @GetMapping("/study-time")
    public ResponseEntity<StudyTimeResponse> getStudyTime(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        StudyTimeResponse response = personalTimerService.getStudyTime(memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 뽀모도로 설정 저장
     */
    @Operation(summary = "뽀모도로 설정 저장", description = "뽀모도로 공부/휴식 시간을 설정합니다")
    @PostMapping("/pomodoro/settings")
    public ResponseEntity<PomodoroSettingResponse> savePomodoroSetting(
            @AuthenticationPrincipal CustomUser customUser,
            @RequestBody PomodoroSettingRequest request) {
        Long memberId = customUser.id;
        PomodoroSettingResponse response = pomodoroSettingService.saveSetting(memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 뽀모도로 설정 조회
     */
    @Operation(summary = "뽀모도로 설정 조회", description = "저장된 뽀모도로 설정을 조회합니다")
    @GetMapping("/pomodoro/settings")
    public ResponseEntity<PomodoroSettingResponse> getPomodoroSetting(@AuthenticationPrincipal CustomUser customUser) {
        Long memberId = customUser.id;
        PomodoroSettingResponse response = pomodoroSettingService.getSetting(memberId);
        
        // 설정이 없으면 204 No Content 반환
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(response);
    }
}
