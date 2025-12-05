package com.team.LetsStudyNow_rg.domain.checklist.service;

import com.team.LetsStudyNow_rg.domain.checklist.dto.request.ChecklistCreateDto;
import com.team.LetsStudyNow_rg.domain.checklist.dto.request.ChecklistUpdateDto;
import com.team.LetsStudyNow_rg.domain.checklist.dto.response.ChecklistResponseDto;
import com.team.LetsStudyNow_rg.domain.checklist.entity.Checklist;
import com.team.LetsStudyNow_rg.domain.checklist.exception.ChecklistNotFoundException;
import com.team.LetsStudyNow_rg.domain.checklist.repository.ChecklistRepository;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.exception.MemberNotFoundException;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final ChecklistRepository checklistRepository;
    private final MemberRepository memberRepository;

    // 체크리스트 생성 로직
    @Transactional
    public ChecklistResponseDto createChecklist(CustomUser customUser, ChecklistCreateDto req) {
        Member member = memberRepository.findById(customUser.id)
                .orElseThrow(() -> new MemberNotFoundException(customUser.id)); // Member 예외 재사용

        Checklist checklist = new Checklist();
        checklist.setMember(member);
        checklist.setTargetDate(req.targetDate());
        checklist.setContent(req.content());

        var savedChecklist = checklistRepository.save(checklist);

        return new ChecklistResponseDto(
                savedChecklist.getId(),
                savedChecklist.getTargetDate(),
                savedChecklist.getContent(),
                savedChecklist.isCompleted()
        );
    }

    // 특정 날짜 체크리스트 조회
    @Transactional(readOnly = true)
    public List<ChecklistResponseDto> getChecklistByDate(CustomUser customUser, LocalDate date) {
        List<Checklist> checklists = checklistRepository.findByMemberIdAndTargetDate(customUser.id, date);

        return checklists.stream()
                .map(checklist -> new ChecklistResponseDto(
                        checklist.getId(),
                        checklist.getTargetDate(),
                        checklist.getContent(),
                        checklist.isCompleted()
                ))
                .collect(Collectors.toList());
    }

    // 월별 체크리스트 존재 날짜 조회
    @Transactional(readOnly = true)
    public List<Integer> getDaysWithChecklistByMonth(CustomUser customUser, int year, int month) {
        return checklistRepository.findDaysWithChecklistByMonth(customUser.id, year, month);
    }

    // 체크리스트 수정
    @Transactional
    public ChecklistResponseDto updateChecklist(CustomUser customUser, Long checklistId, ChecklistUpdateDto dto) {
        Checklist checklist = findChecklist(checklistId, customUser.id);
        checklist.setContent(dto.content());

        return new ChecklistResponseDto(
                checklist.getId(),
                checklist.getTargetDate(),
                checklist.getContent(),
                checklist.isCompleted()
        );
    }

    // 체크리스트 삭제
    @Transactional
    public void deleteChecklist(CustomUser customUser, Long checklistId) {
        Checklist checklist = findChecklist(checklistId, customUser.id);
        checklistRepository.delete(checklist);
    }

    // 체크리스트 완료/미완료 토글
    @Transactional
    public ChecklistResponseDto toggleChecklist(CustomUser customUser, Long checklistId) {
        Checklist checklist = findChecklist(checklistId, customUser.id);

        // 상태 반전
        checklist.setCompleted(!checklist.isCompleted());

        return new ChecklistResponseDto(
                checklist.getId(),
                checklist.getTargetDate(),
                checklist.getContent(),
                checklist.isCompleted()
        );
    }

    // [Private Helper] 중복되는 조회 검증 로직 추출
    private Checklist findChecklist(Long checklistId, Long memberId) {
        return checklistRepository.findByIdAndMemberId(checklistId, memberId)
                .orElseThrow(() -> new ChecklistNotFoundException(checklistId));
    }
}