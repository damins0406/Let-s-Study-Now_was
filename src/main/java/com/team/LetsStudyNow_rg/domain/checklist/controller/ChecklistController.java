package com.team.LetsStudyNow_rg.domain.checklist.controller;

import com.team.LetsStudyNow_rg.domain.checklist.dto.request.ChecklistCreateDto;
import com.team.LetsStudyNow_rg.domain.checklist.dto.request.ChecklistUpdateDto;
import com.team.LetsStudyNow_rg.domain.checklist.dto.response.ChecklistResponseDto;
import com.team.LetsStudyNow_rg.domain.checklist.service.ChecklistService;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "4. 체크리스트 API", description = "체크리스트 CRUD 관련 API")
@RestController
@RequestMapping("/api/checklist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ChecklistController {
    private final ChecklistService checklistService;

    @Operation(summary = "체크리스트 생성", description = "선택한 날짜에 새로운 할 일을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "체크리스트 생성 성공",
                    content = @Content(schema = @Schema(implementation = ChecklistResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping
    public ResponseEntity<ChecklistResponseDto> createChecklist(
            @Valid @RequestBody ChecklistCreateDto dto,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        // 예외 처리는 Handler가 담당
        ChecklistResponseDto responseDto = checklistService.createChecklist(customUser, dto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @Operation(summary = "특정 날짜 체크리스트 조회", description = "선택한 날짜의 모든 체크리스트를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ChecklistResponseDto>> getChecklistByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        List<ChecklistResponseDto> checklist = checklistService.getChecklistByDate(customUser, date);
        return ResponseEntity.ok(checklist);
    }

    @Operation(summary = "월별 체크리스트 존재 날짜 조회", description = "달력 표시용: 해당 월에 체크리스트가 있는 날짜 목록 조회")
    @GetMapping("/month-summary")
    public ResponseEntity<List<Integer>> getDaysWithChecklist(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        return ResponseEntity.ok(checklistService.getDaysWithChecklistByMonth(customUser, year, month));
    }

    @Operation(summary = "체크리스트 내용 수정", description = "기존 체크리스트의 내용을 수정합니다.")
    @PutMapping("/{checklistId}")
    public ResponseEntity<ChecklistResponseDto> updateChecklist(
            @PathVariable("checklistId") Long checklistId,
            @Valid @RequestBody ChecklistUpdateDto dto,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        ChecklistResponseDto responseDto = checklistService.updateChecklist(customUser, checklistId, dto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "체크리스트 삭제", description = "특정 체크리스트를 삭제합니다.")
    @DeleteMapping("/{checklistId}")
    public ResponseEntity<String> deleteChecklist(
            @PathVariable("checklistId") Long checklistId,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        checklistService.deleteChecklist(customUser, checklistId);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    @Operation(summary = "체크리스트 완료/미완료 설정", description = "체크리스트의 완료 상태를 변경합니다.")
    @PatchMapping("/{checklistId}/toggle")
    public ResponseEntity<ChecklistResponseDto> toggleChecklist(
            @PathVariable("checklistId") Long checklistId,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        ChecklistResponseDto responseDto = checklistService.toggleChecklist(customUser, checklistId);
        return ResponseEntity.ok(responseDto);
    }
}