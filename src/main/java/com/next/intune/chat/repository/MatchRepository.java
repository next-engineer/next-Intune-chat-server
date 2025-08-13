package com.next.intune.chat.repository;

import com.next.intune.chat.entity.Match;
import com.next.intune.chat.repository.mapping.MatchRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match,Long> {
    @Query(value = """
    SELECT m.`match_id`     AS matchId,
           m.`requester_id` AS requesterId,
           m.`responder_id` AS responderId,
           m.`is_approved`  AS approved,
           m.`is_valid`     AS valid,
           m.`created_at`   AS createdAt,
           m.`updated_at`   AS updatedAt
    FROM `intune-chat`.`matches` m
    WHERE (m.`requester_id` = :userId OR m.`responder_id` = :userId)
      AND m.`is_valid` = 1
    ORDER BY m.`updated_at` DESC
    """, nativeQuery = true)
    List<MatchRow> findAllValidRowsByUser(@Param("userId") Long userId);
    @Query(
            value = "SELECT EXISTS( " +
                    "  SELECT 1 " +
                    "  FROM `intune-chat`.`matches` m " +
                    "  WHERE `m`.`is_valid` = 1 " +
                    "    AND ( " +
                    "      (`m`.`requester_id` = :a AND `m`.`responder_id` = :b) " +
                    "      OR " +
                    "      (`m`.`requester_id` = :b AND `m`.`responder_id` = :a) " +
                    "    ) " +
                    ")",
            nativeQuery = true
    )
    boolean existsAnyDirection(@Param("a") Long a, @Param("b") Long b);
}
