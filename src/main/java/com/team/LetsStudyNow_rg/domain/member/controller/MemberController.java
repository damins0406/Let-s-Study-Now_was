package com.team.LetsStudyNow_rg.domain.member.controller;

import com.team.LetsStudyNow_rg.domain.member.dto.request.*;
import com.team.LetsStudyNow_rg.domain.member.dto.response.ProfileDto;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.domain.member.service.MemberService;
import com.team.LetsStudyNow_rg.domain.member.service.MemberUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "1. 사용자 인증/계정 API", description = "회원가입, 로그인, 프로필 관리 등 사용자 계정 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberUpdateService memberUpdateService;

    // 로그인 api
    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하고, HttpOnly 쿠키에 JWT를 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패 (아이디/비밀번호 불일치 또는 입력값 오류)")
    })
    @PostMapping("/loginAct")
    public ResponseEntity<String> loginAct(
            @Valid @RequestBody LoginDto req,
            HttpServletResponse response
    ) {
        String token = memberService.loginService(req, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(token);
    }

    // 회원가입 api
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이메일 또는 아이디 중복"), // Handler의 CONFLICT 상태 코드 반영
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패")
    })
    @PostMapping("/registerAct")
    public ResponseEntity<String> registerAct(
            @Valid @RequestBody RegisterDto req
    ) {
        memberService.registerService(req);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입 성공");
    }

    // 마이프로필 api
    @Operation(summary = "마이프로필 조회", description = "현재 로그인된 사용자의 프로필 정보를 조회합니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ResponseEntity<ProfileDto> profile(
            @AuthenticationPrincipal CustomUser customUser
    ) {
        ProfileDto profileDto = memberService.profileService(customUser);
        return ResponseEntity.ok(profileDto);
    }

    // 프로필 수정 api
    @Operation(summary = "마이프로필 수정", description = "현재 로그인된 사용자의 프로필 정보(사진, 공부분야, 자기소개)를 수정합니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping(value = "/update/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileDto> updateProfile(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("data") @Valid ProfileUpdateDto req,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        ProfileDto profileDto = memberUpdateService.updateProfileService(customUser, req, image);
        return ResponseEntity.ok(profileDto);
    }

    // 회원 비밀번호 변경 api
    @Operation(summary = "비밀번호 변경", description = "현재 로그인된 사용자의 비밀번호를 변경합니다. (인증 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치 또는 입력값 오류")
    })
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update/password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody PasswordChangeDto passwordChangeDto,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        memberUpdateService.changePassword(customUser, passwordChangeDto);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 회원 탈퇴 api
    @Operation(summary = "회원 탈퇴", description = "비밀번호 확인 후 계정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "비밀번호 불일치")
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/account")
    public ResponseEntity<String> deleteAccount(
            @Valid @RequestBody AccountDeleteDto req,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        memberUpdateService.deleteAccount(customUser, req);
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }
}