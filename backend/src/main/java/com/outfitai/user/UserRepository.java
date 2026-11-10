package com.outfitai.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data generates the implementation from the method names. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSub(String googleSub);

    boolean existsByEmail(String email);
}
