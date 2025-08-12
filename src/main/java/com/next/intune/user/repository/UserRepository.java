package com.next.intune.user.repository;

import com.next.intune.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailAndValidTrue(String email);
    List<User> findAllByMbtiInAndGenderAndValidTrue(List<String> mbtiGroup, String oppositeGender);
}
