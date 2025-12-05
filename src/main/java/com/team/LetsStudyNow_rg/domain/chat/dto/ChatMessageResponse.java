package com.team.LetsStudyNow_rg.domain.chat.dto;

import com.team.LetsStudyNow_rg.domain.chat.entity.ChatMessage;
import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import com.team.LetsStudyNow_rg.domain.chat.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private Long messageId;
    private MessageType type;
    private ChatRoomType roomType;
    private Long roomId;
    private String sender;      // 서버가 인증정보로 채워 넣은 진짜 보낸 사람
    private String message;
    private LocalDateTime sentAt;
    private Long refId;
    private Boolean isSolved;
    private Boolean isSelected;

    // Entity -> Response 변환 편의 메서드
    public static ChatMessageResponse from(ChatMessage entity) {
        return ChatMessageResponse.builder()
                .messageId(entity.getId())
                .type(entity.getType())
                .roomType(entity.getRoomType())
                .roomId(entity.getRoomId())
                .sender(entity.getSender())
                .message(entity.getMessage())
                .sentAt(entity.getSentAt())
                .refId(entity.getRefId())
                .isSolved(entity.getIsSolved())
                .isSelected(entity.getIsSelected())
                .build();
    }
}