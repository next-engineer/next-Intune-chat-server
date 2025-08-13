package com.next.intune.chat.repository;

import com.next.intune.chat.entity.Match;
import com.next.intune.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match,Long> {
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
