package com.next.intune.chat.repository;

import com.next.intune.chat.entity.ChatHistory;
import com.next.intune.chat.repository.mapping.ChatRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory,Long> {
    @Query(value = """
    SELECT m.`chat_id`      AS chatId,
           m.`match_id`     AS matchId,
           m.`user_id`      AS userId,
           m.`chat`         AS chat,
           m.`created_at`   AS createdAt
    FROM `intune-chat`.`chats_history` m
    WHERE  m.`match_id` = :matchId
    ORDER BY m.`created_at` DESC
    """, nativeQuery = true)
    List<ChatRow> findAllRowsByChat(@Param("matchId") Long matchId);
}
