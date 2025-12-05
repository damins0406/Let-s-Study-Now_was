package com.team.LetsStudyNow_rg.domain.chat.repository;

import com.team.LetsStudyNow_rg.domain.chat.entity.ChatMessage;
import com.team.LetsStudyNow_rg.domain.chat.enums.ChatRoomType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {

    Slice<ChatMessage> findByRoomIdAndRoomTypeOrderBySentAtDesc(Long roomId, ChatRoomType roomType, Pageable pageable);
}