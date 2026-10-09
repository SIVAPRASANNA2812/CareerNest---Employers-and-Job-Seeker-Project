package com.guvi.careernestbackend.Repository;

import com.guvi.careernestbackend.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Used during login to find user by their email
    Optional<User> findByEmail(String email);

    // Used during registration to check if email is already taken
    Boolean existsByEmail(String email);

    // Optional check to verify phone number uniqueness
    Boolean existsByPhoneNumber(String phoneNumber);
}