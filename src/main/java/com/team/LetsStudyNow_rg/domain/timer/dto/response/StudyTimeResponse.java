package com.team.LetsStudyNow_rg.domain.timer.dto.response;

public record StudyTimeResponse(
        Long totalStudySeconds,
        String totalStudyTime,
        Long todayStudySeconds,
        String todayStudyTime
) {
    public static StudyTimeResponse of(Long totalSeconds, Long todaySeconds) {
        return new StudyTimeResponse(
                totalSeconds,
                formatSeconds(totalSeconds),
                todaySeconds,
                formatSeconds(todaySeconds)
        );
    }

    private static String formatSeconds(Long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
