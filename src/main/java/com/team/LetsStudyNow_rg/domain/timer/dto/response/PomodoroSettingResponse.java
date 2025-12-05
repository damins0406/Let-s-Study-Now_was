package com.team.LetsStudyNow_rg.domain.timer.dto.response;

import com.team.LetsStudyNow_rg.domain.timer.entity.PomodoroSetting;

public record PomodoroSettingResponse(
        Long id,
        Long memberId,
        Integer studyMinutes,
        Integer restMinutes
) {
    public PomodoroSettingResponse(PomodoroSetting setting) {
        this(
                setting.getId(),
                setting.getMemberId(),
                setting.getStudyMinutes(),
                setting.getRestMinutes()
        );
    }
}
