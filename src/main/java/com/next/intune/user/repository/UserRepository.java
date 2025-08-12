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
        SELECT u.* 
        FROM users u
        WHERE u.mbti IN (:mbtis)
          AND u.gender = :gender
          AND u.is_valid = true
          AND u.user_id <> :excludeUserId
        ORDER BY RAND()
        LIMIT 1
        """, nativeQuery = true)
    Optional<User> pickOneRandomByMbtiInAndGenderAndValidTrue(
            @Param("mbtis") List<String> mbtis,
            @Param("gender") String gender,
            @Param("excludeUserId") Long excludeUserId);

}
