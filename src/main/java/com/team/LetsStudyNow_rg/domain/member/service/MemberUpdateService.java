package com.team.LetsStudyNow_rg.domain.member.service;

import com.team.LetsStudyNow_rg.domain.member.dto.request.AccountDeleteDto;
import com.team.LetsStudyNow_rg.domain.member.dto.request.PasswordChangeDto;
import com.team.LetsStudyNow_rg.domain.member.dto.request.ProfileUpdateDto;
import com.team.LetsStudyNow_rg.domain.member.dto.response.ProfileDto;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.exception.MemberNotFoundException;
import com.team.LetsStudyNow_rg.domain.member.exception.PasswordMismatchException;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberUpdateService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Value("${custom.s3.default-image-url}")
    private String defaultProfileImageUrl;

    // 마이프로필 수정 로직
    @Transactional
    public ProfileDto updateProfileService(CustomUser customUser, ProfileUpdateDto req, MultipartFile image) {
        Member user = memberRepository.findById(customUser.id)
                .orElseThrow(() -> new MemberNotFoundException(customUser.id));

        // 이미지 업로드
        if (image != null && !image.isEmpty()) {
            String oldImageUrl = user.getProfileImage();

            // 기본 프로필이 아닌 이전 이미지는 삭제
            if (oldImageUrl != null && !oldImageUrl.equals(defaultProfileImageUrl)) {
                s3Service.deleteFile(oldImageUrl);
            }

            // 새 이미지 업로드 및 DB 업데이트
            String newImageUrl = s3Service.uploadFile(image, "profile");
            user.setProfileImage(newImageUrl);

        }

        if (req.studyField() != null) {
            user.setStudyField(req.studyField());
        }
        if (req.bio() != null) {
            user.setBio(req.bio());
        }

        return new ProfileDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getProfileImage(),
                user.getStudyField(),
                user.getBio(),
                user.getLevel()
        );
    }

    // 비밀번호 변경 로직
    @Transactional
    public void changePassword(CustomUser customUser, PasswordChangeDto req) {
        Member user = memberRepository.findById(customUser.id)
                .orElseThrow(() -> new MemberNotFoundException(customUser.id));

        // 커스텀 예외 적용
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!req.newPassword().equals(req.newPasswordCheck())) {
            throw new PasswordMismatchException("새로운 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        var newPassword = passwordEncoder.encode(req.newPassword());
        user.setPassword(newPassword);
    }

    // 회원탈퇴 로직
    @Transactional
    public void deleteAccount(CustomUser customUser, AccountDeleteDto req) {
        Member user = memberRepository.findById(customUser.id)
                .orElseThrow(() -> new MemberNotFoundException(customUser.id));

        // 1. 비밀번호 확인
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new PasswordMismatchException("비밀번호가 일치하지 않습니다.");
        }

        // 2. Soft Delete: deleted 플래그 설정
        user.setDeleted(true);

        // 3. 개인정보 마스킹 및 유니크 키 충돌 방지
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        user.setEmail("deleted_" + user.getId() + "_" + uuid);
        user.setUsername("unknown_" + user.getId() + "_" + uuid);

        // 4. 기타 정보 초기화
        user.setBio("탈퇴한 사용자입니다.");
        user.setProfileImage(defaultProfileImageUrl); // 프로필 이미지 초기화
    }
}