package com.cavcav.swiftcart.user.repository;

import com.cavcav.swiftcart.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile,String> {
}
