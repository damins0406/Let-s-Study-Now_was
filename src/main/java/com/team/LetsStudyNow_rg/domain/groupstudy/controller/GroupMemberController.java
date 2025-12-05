package com.team.LetsStudyNow_rg.domain.groupstudy.controller;

import com.team.LetsStudyNow_rg.domain.groupstudy.dto.AddGroupMemberRequest;
import com.team.LetsStudyNow_rg.domain.groupstudy.dto.GroupMemberResponse;
import com.team.LetsStudyNow_rg.domain.groupstudy.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "GroupMember", description = "그룹 멤버 관리 API")
@RestController
@RequestMapping("/api/groups/{groupId}/members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    // 생성자 주입
    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    // 멤버 추가
    @Operation(summary = "멤버 추가", description = "그룹에 멤버를 추가합니다")
    @PostMapping
    public ResponseEntity<GroupMemberResponse> addMember(
            @PathVariable Long groupId,
            @RequestParam Long memberId) {
        AddGroupMemberRequest request = new AddGroupMemberRequest(groupId, memberId);
        GroupMemberResponse response = groupMemberService.addMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 멤버 목록 조회 (SRS 6.3.6)
    @Operation(summary = "멤버 목록 조회", description = "그룹의 모든 멤버를 조회합니다")
    @GetMapping
    public ResponseEntity<List<GroupMemberResponse>> getMembers(@PathVariable Long groupId) {
        List<GroupMemberResponse> responses = groupMemberService.getGroupMembers(groupId);
        return ResponseEntity.ok(responses);
    }

    // 멤버 추방 (SRS 6.3.7~6.3.9)
    @Operation(summary = "멤버 추방", description = "그룹에서 멤버를 추방합니다 (그룹 생성자만 가능)")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId) {
        groupMemberService.removeMember(groupId, memberId, requesterId);
        return ResponseEntity.noContent().build();
    }
}