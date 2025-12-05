package com.team.LetsStudyNow_rg.domain.groupstudy.controller;

import com.team.LetsStudyNow_rg.domain.groupstudy.dto.CreateGroupRequest;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupMemberResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.service.GroupService;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Group", description = "그룹 관리 API")
@RestController  // REST API 컨트롤러
@RequestMapping("/api/groups")  // 기본 경로
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 그룹 생성 (SRS 6.2.1, 6.2.2)
    @Operation(summary = "그룹 생성", description = "새로운 그룹을 생성합니다")
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal CustomUser customUser) {
        Long leaderId = customUser.id;
        GroupResponse response = groupService.createGroup(request.groupName(), leaderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 그룹 조회
    @Operation(summary = "그룹 조회", description = "그룹 ID로 그룹을 조회합니다")
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId) {
        GroupResponse response = groupService.getGroup(groupId);
        return ResponseEntity.ok(response);
    }

    // 전체 그룹 목록
    @Operation(summary = "전체 그룹 목록", description = "모든 그룹을 조회합니다")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<GroupResponse> responses = groupService.getAllGroups();
        return ResponseEntity.ok(responses);
    }

    // 내가 만든 그룹 목록
    @Operation(summary = "내 그룹 목록", description = "내가 만든 그룹 목록을 조회합니다")
    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal CustomUser customUser) {
        Long leaderId = customUser.id;
        List<GroupResponse> responses = groupService.getMyGroups(leaderId);
        return ResponseEntity.ok(responses);
    }

    // 그룹 삭제 (SRS 6.2.4)
    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다 (방 생성자만 가능)")
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.id;
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}