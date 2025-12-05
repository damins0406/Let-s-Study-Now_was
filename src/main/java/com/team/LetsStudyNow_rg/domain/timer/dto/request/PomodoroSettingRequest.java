package com.team.LetsStudyNow_rg.domain.timer.dto.request;

public record PomodoroSettingRequest(
        Integer studyMinutes,  // 공부 시간 (분)
        Integer restMinutes    // 휴식 시간 (분)
) {
}
