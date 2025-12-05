package com.team.LetsStudyNow_rg.domain.openstudy.exception;

public class RoomDeletingException extends RuntimeException {
    public RoomDeletingException() {
        super("곧 삭제될 방입니다. 다른 방을 이용해 주세요");
    }
    
    public RoomDeletingException(String message) {
        super(message);
    }
}
