package com.team.LetsStudyNow_rg.domain.chat.controller;

import com.team.LetsStudyNow_rg.domain.chat.dto.ChatMessageRequest;
import com.team.LetsStudyNow_rg.domain.chat.dto.ChatMessageResponse;
import com.team.LetsStudyNow_rg.domain.chat.entity.ChatMessage;
import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import com.team.LetsStudyNow_rg.domain.chat.enums.MessageType;
import com.team.LetsStudyNow_rg.domain.chat.service.ChatService;
import com.team.LetsStudyNow_rg.global.auth.CustomUser;
import com.team.LetsStudyNow_rg.global.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;
    private final S3Service s3Service;

    // 메시지 전송 처리 (클라이언트: /pub/chat/message)
    @MessageMapping("/chat/message")
    public void message(
            ChatMessageRequest request,
            Principal principal
    ) {
        try {
            log.info(">>> 컨트롤러 진입! 타입: {}", request.getType());

            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            CustomUser user = (CustomUser) auth.getPrincipal();

            ChatMessageResponse response = chatService.saveMessage(request, user.username);

            log.info(">>> 서비스 저장 성공! ID: {}", response.getMessageId());

            String destination = "/sub/chat/" + request.getRoomType().name().toLowerCase() + "/" + request.getRoomId();
            messagingTemplate.convertAndSend(destination, response);

        } catch (Exception e) {
            log.error(">>> [치명적 에러 발생] 메시지 처리 중 오류: ", e);
            e.printStackTrace();
        }

    }

    // 질문 해결 및 답변 채택 API
    @Operation(summary = "질문 해결 및 답변 채택", description = "질문을 해결 상태로 변경하고, 선택적으로 답변을 채택합니다.")
    @PatchMapping("/api/chat/message/{messageId}/solve")
    public ResponseEntity<String> solveQuestion(
            @PathVariable(value = "messageId") Long messageId,             // 질문 ID
            @RequestParam(value = "answerId", required = false) Long answerId, // 채택할 답변 ID (없으면 단순 해결)
            @AuthenticationPrincipal CustomUser customUser
    ) {
        // 서비스 호출
        ChatMessage solvedQuestion = chatService.solveQuestion(messageId, answerId, customUser.username);

        // 시스템 알림 메시지 내용 결정
        String msgContent = "질문이 해결되었습니다.";
        if (answerId != null) {
            msgContent = "질문이 해결되었으며, 답변이 채택되었습니다.";
        }

        // 실시간 알림 생성 (소켓용)
        ChatMessageResponse notification = ChatMessageResponse.builder()
                .type(MessageType.SOLVE)
                .roomId(solvedQuestion.getRoomId())
                .roomType(solvedQuestion.getRoomType())
                .messageId(solvedQuestion.getId()) // 해결된 질문 ID
                .sender("SYSTEM")
                .message(msgContent)
                .isSolved(true)
                .build();

        // 방 전체에 알림 전송
        String destination = "/sub/chat/" + solvedQuestion.getRoomType().name().toLowerCase() + "/" + solvedQuestion.getRoomId();
        messagingTemplate.convertAndSend(destination, notification);

        return ResponseEntity.ok("질문 해결 및 채택 처리가 완료되었습니다.");
    }

    // 메시지 삭제 api
    @DeleteMapping("/api/chat/message/{messageId}")
    @Operation(summary = "채팅 메시지 삭제", description = "채팅 메세지를 삭제합니다.")
    public ResponseEntity<String> deleteMessage(
            @PathVariable(value = "messageId") Long messageId,
            @AuthenticationPrincipal CustomUser customUser
    ) {
        chatService.deleteMessage(messageId, customUser.username);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    // 이전 채팅 가져옴 (크기: 20)
    @Operation(summary = "채팅 내역 조회", description = "특정 방의 이전 채팅 내역을 최신순으로 조회합니다.")
    @GetMapping("/api/chat/room/{roomId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable(value = "roomId") Long roomId,
            @RequestParam(value = "roomType") ChatRoomType roomType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {

        // 리스트로 변환 (최신순)
        List<ChatMessage> chatList = chatService.getChatHistory(roomId, roomType, page, size);

        return ResponseEntity.ok(chatList);
    }

    @PostMapping(value = "/api/chat/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadChatImage(
            @RequestPart("file") MultipartFile file
    ) {
        String imageUrl = s3Service.uploadFile(file, "chat");

        return ResponseEntity.ok(imageUrl);
    }
}

