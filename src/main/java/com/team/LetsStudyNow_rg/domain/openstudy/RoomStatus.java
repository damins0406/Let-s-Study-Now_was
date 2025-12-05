package com.team.LetsStudyNow_rg.domain.openstudy;

public enum RoomStatus {
    ACTIVE,           // 정상 활동 중
    PENDING_DELETE,   // 삭제 대기 중 (5분 타이머 진행 중)
    DELETED          // 삭제됨
}
