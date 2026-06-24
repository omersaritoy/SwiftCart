package com.cavcav.swiftcart.user.repository;

import com.cavcav.swiftcart.user.model.User;
import com.cavcav.swiftcart.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    boolean existsByUserId(String userId);

    Optional<UserProfile> getUserProfileByUser(User user);

    Optional<UserProfile> findByUserId(String id);
}
