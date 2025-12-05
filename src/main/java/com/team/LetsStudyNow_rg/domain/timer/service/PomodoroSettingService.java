package com.team.LetsStudyNow_rg.domain.timer.service;

import com.team.LetsStudyNow_rg.domain.timer.dto.request.PomodoroSettingRequest;
import com.team.LetsStudyNow_rg.domain.timer.dto.response.PomodoroSettingResponse;
import com.team.LetsStudyNow_rg.domain.timer.entity.PomodoroSetting;
import com.team.LetsStudyNow_rg.domain.timer.repository.PomodoroSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PomodoroSettingService {

    private final PomodoroSettingRepository pomodoroSettingRepository;

    /**
     * 뽀모도로 설정 저장
     * - 첫 설정 시 새로 생성
     * - 이미 설정이 있으면 업데이트
     */
    @Transactional
    public PomodoroSettingResponse saveSetting(Long memberId, PomodoroSettingRequest request) {
        PomodoroSetting setting = pomodoroSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> new PomodoroSetting(memberId, request.studyMinutes(), request.restMinutes()));

        // 기존 설정이 있으면 업데이트
        if (setting.getId() != null) {
            setting.updateSetting(request.studyMinutes(), request.restMinutes());
        }

        PomodoroSetting savedSetting = pomodoroSettingRepository.save(setting);
        return new PomodoroSettingResponse(savedSetting);
    }

    /**
     * 뽀모도로 설정 조회
     * - 이전 설정 유지
     */
    public PomodoroSettingResponse getSetting(Long memberId) {
        PomodoroSetting setting = pomodoroSettingRepository.findByMemberId(memberId)
                .orElse(null);

        // 첫 사용 시 설정이 비어있음
        if (setting == null) {
            return null;
        }

        return new PomodoroSettingResponse(setting);
    }

    /**
     * 뽀모도로 설정 존재 여부 확인
     */
    public boolean hasSettings(Long memberId) {
        return pomodoroSettingRepository.existsByMemberId(memberId);
    }
}
