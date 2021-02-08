package com.banchango.domain.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET password=sha2(?, '256') WHERE email=?", nativeQuery = true)
    void updatePassword(String password, String email);

    Optional<User> findByEmailAndPasswordAndRole(String email, String password, UserRole admin);
}