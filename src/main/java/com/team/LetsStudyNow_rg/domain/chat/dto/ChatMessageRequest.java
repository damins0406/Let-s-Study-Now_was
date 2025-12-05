package com.team.LetsStudyNow_rg.domain.chat.dto;

import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import com.team.LetsStudyNow_rg.domain.chat.enums.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequest {
    private MessageType type;      // 메시지 타입
    private ChatRoomType roomType; // 방 타입
    private Long roomId;           // 방 번호
    private String message;        // 내용
    private Long refId;            // 답변일 경우 질문 ID (옵션)

}