package com.next.intune.chat.repository;

import com.next.intune.chat.entity.Match;
import com.next.intune.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match,Long> {
    boolean existsByRequesterAndResponderAndValidTrue(User requester, User responder);
}
