package com.team.LetsStudyNow_rg.domain.studyroom.service;

import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.exception.MemberNotFoundException;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import com.team.LetsStudyNow_rg.domain.studyroom.dto.LevelInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 레벨업 서비스
 * 공부 시간을 경험치로 변환하고 레벨업을 처리
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LevelUpService {
    
    private final MemberRepository memberRepository;
    
    /**
     * 특정 레벨에서 다음 레벨로 올라가기 위해 필요한 시간 계산 (분 단위)
     * 레벨의 10의 자리수 + 1 × 10시간
     * 
     * 1~9레벨: 10시간 (600분)
     * 10~19레벨: 20시간 (1200분)
     * 20~29레벨: 30시간 (1800분)
     * 30~39레벨: 40시간 (2400분)
     * 
     * @param currentLevel 현재 레벨
     * @return 다음 레벨까지 필요한 시간 (분)
     */
    public int getRequiredMinutesForLevel(int currentLevel) {
        int tens = currentLevel / 10; // 10의 자리수 (0, 1, 2, 3, ...)
        return (tens + 1) * 600; // 600, 1200, 1800, 2400, ...
    }
    
    /**
     * 특정 레벨에 도달하기 위해 필요한 총 누적 경험치 계산
     * 
     * @param targetLevel 목표 레벨
     * @return 필요한 총 누적 경험치 (분)
     */
    private int calculateTotalExpForLevel(int targetLevel) {
        int totalExp = 0;
        for (int level = 1; level < targetLevel; level++) {
            totalExp += getRequiredMinutesForLevel(level);
        }
        return totalExp;
    }
    
    /**
     * 공부 시간 추가 및 레벨업 처리
     * 
     * @param memberId 회원 ID
     * @param studyMinutes 추가할 공부 시간 (분)
     * @return 레벨업 여부
     */
    public boolean addStudyTimeAndCheckLevelUp(Long memberId, int studyMinutes) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        // 총 경험치 증가
        int newTotalExp = member.getTotalExp() + studyMinutes;
        member.setTotalExp(newTotalExp);
        
        log.info("회원 {}의 공부 시간 추가: {}분, 총 경험치: {}분", 
                 member.getUsername(), studyMinutes, newTotalExp);
        
        boolean leveledUp = false;
        int originalLevel = member.getLevel();
        
        // 레벨업 가능 여부 확인 및 처리 (여러 레벨 한번에 올라갈 수 있음)
        while (true) {
            int totalRequiredForNextLevel = calculateTotalExpForLevel(member.getLevel() + 1);
            
            if (member.getTotalExp() >= totalRequiredForNextLevel) {
                member.setLevel(member.getLevel() + 1);
                leveledUp = true;
                log.info("회원 {}가 레벨업! {} -> {} 레벨", 
                         member.getUsername(), member.getLevel() - 1, member.getLevel());
            } else {
                break;
            }
        }
        
        if (leveledUp) {
            log.info("회원 {}의 최종 레벨: {} ({}레벨 상승)", 
                     member.getUsername(), member.getLevel(), member.getLevel() - originalLevel);
        }
        
        memberRepository.save(member);
        return leveledUp;
    }
    
    /**
     * 회원의 레벨 정보 상세 조회
     * 
     * @param memberId 회원 ID
     * @return 레벨 정보 DTO
     */
    @Transactional(readOnly = true)
    public LevelInfoDto getLevelInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(memberId));
        
        int currentLevel = member.getLevel();
        int totalExp = member.getTotalExp();
        
        int totalRequiredForCurrentLevel = calculateTotalExpForLevel(currentLevel);
        int totalRequiredForNextLevel = calculateTotalExpForLevel(currentLevel + 1);
        
        int currentLevelExp = totalExp - totalRequiredForCurrentLevel;
        int requiredExpForNextLevel = totalRequiredForNextLevel - totalRequiredForCurrentLevel;
        int remainingExp = totalRequiredForNextLevel - totalExp;
        
        double progress = (double) currentLevelExp / requiredExpForNextLevel * 100.0;
        
        return LevelInfoDto.builder()
                .memberId(memberId)
                .username(member.getUsername())
                .currentLevel(currentLevel)
                .totalExp(totalExp)
                .currentLevelExp(currentLevelExp)
                .requiredExpForNextLevel(requiredExpForNextLevel)
                .remainingExp(remainingExp)
                .progress(Math.round(progress * 10.0) / 10.0) // 소수점 첫째자리까지
                .build();
    }
}
