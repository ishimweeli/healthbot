package com.hiv.predictionbot.repository;


import com.hiv.predictionbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    Boolean existsByEmail(String email);

    List<User> findAllByOrderByPredictedLifespanAsc();
}
