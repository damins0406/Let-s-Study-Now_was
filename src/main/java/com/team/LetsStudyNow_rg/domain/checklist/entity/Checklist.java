package com.team.LetsStudyNow_rg.domain.checklist.entity;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false, length = 200)
    private String content;

    @Column(nullable = false)
    private boolean isCompleted = false; // 완료 여부 (체크박스)
}
