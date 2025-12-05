package com.team.LetsStudyNow_rg.domain.chat.service;

import com.team.LetsStudyNow_rg.domain.chat.dto.ChatMessageRequest;
import com.team.LetsStudyNow_rg.domain.chat.dto.ChatMessageResponse;
import com.team.LetsStudyNow_rg.domain.chat.entity.ChatMessage;
import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import com.team.LetsStudyNow_rg.domain.chat.enums.MessageType;
import com.team.LetsStudyNow_rg.domain.chat.repository.ChatRepository;
import com.team.LetsStudyNow_rg.domain.member.entity.Member;
import com.team.LetsStudyNow_rg.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ChatMessageResponse saveMessage(ChatMessageRequest request, String sender) {
        String content = request.getMessage();

        // 입장 메시지 처리
        if (MessageType.ENTER.equals(request.getType())) {
            content = sender + "님이 입장하셨습니다.";
        }

        if (MessageType.ANSWER.equals(request.getType())) {
            Long targetId = request.getRefId();

            if (targetId == null) {
                throw new IllegalArgumentException("답변에는 원본 질문 ID(refId)가 필수입니다.");
            }

            // 원본 메시지 조회
            ChatMessage targetMessage = chatRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("답변하려는 원본 메시지를 찾을 수 없습니다."));

            // 원본이 '질문' 타입인지 확인
            if (!MessageType.QUESTION.equals(targetMessage.getType())) {
                throw new IllegalArgumentException("질문(QUESTION)에만 답변을 달 수 있습니다.");
            }

            if (!targetMessage.getRoomId().equals(request.getRoomId()) ||
                    !targetMessage.getRoomType().equals(request.getRoomType())) {
                throw new IllegalArgumentException("다른 방의 질문에는 답변할 수 없습니다.");
            }
        }

        // 질문 (질문 제외 한 메시지 null)
        Boolean isSolved = null;
        if (MessageType.QUESTION.equals(request.getType())) {
            isSolved = false;
        }

        ChatMessage chatEntity = ChatMessage.builder()
                .roomType(request.getRoomType())
                .roomId(request.getRoomId())
                .sender(sender)
                .message(content)
                .type(request.getType())
                .refId(request.getRefId())
                .isSolved(isSolved)
                .isSelected(false)
                .build();

        ChatMessage savedMessage = chatRepository.save(chatEntity);
        return ChatMessageResponse.from(savedMessage);
    }

    // 질문 해결 완료 처리
    @Transactional
    public ChatMessage solveQuestion(Long questionId, Long answerId, String sender) {
        ChatMessage question = chatRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

        if (!MessageType.QUESTION.equals(question.getType())) {
            throw new IllegalArgumentException("질문 메시지만 해결 처리할 수 있습니다.");
        }

        if (!question.getSender().equals(sender)) {
            throw new IllegalArgumentException("질문 작성자만 해결할 수 있습니다.");
        }

        if (Boolean.TRUE.equals(question.getIsSolved())) {
            throw new IllegalArgumentException("이미 해결된 질문입니다.");
        }

        question.markAsSolved();

        if (answerId != null) {
            ChatMessage answer = chatRepository.findById(answerId)
                    .orElseThrow(() -> new IllegalArgumentException("채택할 답변을 찾을 수 없습니다."));

            if (answer.getRefId() == null || !answer.getRefId().equals(question.getId())) {
                throw new IllegalArgumentException("이 질문에 달린 답변이 아닙니다.");
            }

            if (question.getSender().equals(answer.getSender())) {
                throw new IllegalArgumentException("본인의 답변은 채택할 수 없습니다.");
            }

            answer.markAsSelected();

            Member answerer = memberRepository.findByUsername(answer.getSender())
                    .orElseThrow(() -> new IllegalArgumentException("답변 작성자를 찾을 수 없습니다."));

            answerer.increaseAdoptionCount();
        }

        return question;
    }

    // 메시지 삭제 처리
    @Transactional
    public void deleteMessage(Long messageId, String sender) {
        ChatMessage message = chatRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        if (!message.getSender().equals(sender)) {
            throw new IllegalArgumentException("작성자만 삭제할 수 있습니다.");
        }

        chatRepository.delete(message);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(Long roomId, ChatRoomType roomType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));

        Slice<ChatMessage> chatSlice = chatRepository.findByRoomIdAndRoomTypeOrderBySentAtDesc(roomId, roomType, pageable);

        return chatSlice.getContent();
    }
}
