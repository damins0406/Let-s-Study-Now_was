package com.team.LetsStudyNow_rg.domain.member.entity;

import com.team.LetsStudyNow_rg.domain.member.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 이메일

    @Column(unique = true, nullable = false)
    private String username; // 닉네임

    @Column(nullable = false)
    private String password; // 비밀번호

    @Enumerated(EnumType.STRING) // 문자열 그대로 저장
    private Role role; // 권한

    private String profileImage; // 프로필 사진 (임시)
    private String studyField; // 공부 분야

    @Column(length = 500)
    private String bio; // 자기소개

    @Column(nullable = false)
    private Integer level = 1; // 기본 레벨 1

    @Column(nullable = false)
    private Integer totalExp = 0; // 누적 경험치

    @Column(nullable = false)
    private Integer adoptionCount = 0; // 답변 채택 횟수

    @Column(nullable = false)
    private boolean deleted = false;

    @Builder
    public Member(String email, String username, String password, Role role, String studyField, String bio, String profileImage) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.studyField = studyField;
        this.bio = bio;
        this.profileImage = profileImage;
        this.level = 1;
        this.totalExp = 0;
        this.adoptionCount = 0;
    }

    public void increaseAdoptionCount() {
        this.adoptionCount++;
    }
}


