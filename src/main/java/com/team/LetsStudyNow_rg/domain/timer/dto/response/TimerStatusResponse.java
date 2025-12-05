package com.team.LetsStudyNow_rg.domain.timer.dto.response;

import com.team.LetsStudyNow_rg.domain.timer.entity.PersonalTimer;
import com.team.LetsStudyNow_rg.domain.timer.entity.TimerMode;
import com.team.LetsStudyNow_rg.domain.timer.entity.TimerStatus;

public record TimerStatusResponse(
        Long timerId,
        Long memberId,
        Long roomId,
        TimerMode timerMode,
        TimerStatus timerStatus,
        Long currentSessionSeconds,
        Long totalStudySeconds,
        String totalStudyTime
) {
    public TimerStatusResponse(PersonalTimer timer) {
        this(
                timer.getId(),
                timer.getMemberId(),
                timer.getRoomId(),
                timer.getTimerMode(),
                timer.getTimerStatus(),
                timer.getCurrentSessionSeconds(),
                timer.getTotalStudySeconds(),
                formatSeconds(timer.getTotalStudySeconds())
        );
    }

    private static String formatSeconds(Long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
