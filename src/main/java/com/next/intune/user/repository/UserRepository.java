package com.next.intune.user.repository;

import com.next.intune.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailAndValidTrue(String email);
    List<User> findAllByMbtiInAndGenderAndValidTrue(List<String> mbtiGroup, String oppositeGender);
    @Query(value = """
        SELECT
        `u`.`user_id`,
        `u`.`email`,
        `u`.`password`,
        `u`.`name`,
        `u`.`mbti`,
        `u`.`gender`,
        `u`.`authority`,
        `u`.`profile_image_id`,
        `u`.`address`,
        `u`.`via`,
        `u`.`is_valid`,
        `u`.`created_at`,
        `u`.`updated_at`
        FROM `intune-member`.`users` `u`
        WHERE `u`.`mbti` IN (:mbtis)
          AND `u`.`gender` = :gender
          AND `u`.`is_valid` = 1
          AND `u`.`user_id` <> :excludeUserId
          AND `u`.`user_id` NOT IN (
              SELECT `m`.`responder_id`
              FROM `intune-chat`.`matches` `m`
              WHERE `m`.`requester_id` = :requesterId
                AND `m`.`is_valid` = 1
          )
        ORDER BY RAND()
        LIMIT 1
        """, nativeQuery = true)
    Optional<Long> pickRandomFreshUserId(
            @Param("requesterId") Long requesterId,
            @Param("mbtis") List<String> mbtis,
            @Param("gender") String gender,
            @Param("excludeUserId") Long excludeUserId
    );

}
