package com.team.LetsStudyNow_rg.domain.chat.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import com.team.LetsStudyNow_rg.domain.chat.enums.MessageType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatRoomType roomType;

    @Column(nullable = false)
    private Long roomId;      // 방 번호

    @Column(nullable = false)
    private String sender;    // 보낸 사람

    @Column(columnDefinition = "TEXT")
    private String message;   // 메시지 내용

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @CreatedDate
    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime sentAt;

    private Long refId; // 답변 원본 질문 ID

    private Boolean isSolved; // 해결 여부
    private Boolean isSelected; // 채택 여부

    @Builder
    public ChatMessage(ChatRoomType roomType, Long roomId, String sender, String message, MessageType type, Long refId, Boolean isSolved, Boolean isSelected) {
        this.roomType = roomType;
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.type = type;
        this.refId = refId;
        this.isSolved = isSolved;
        this.isSelected = isSelected;
    }

    public void markAsSolved() {
        this.isSolved = true;
    }

    public void markAsSelected() {
        this.isSelected = true;
    }
}
