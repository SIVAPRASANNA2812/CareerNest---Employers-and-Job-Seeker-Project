package com.guvi.careernestbackend.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "refresh_tokens")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token; // Cryptographically unique UUID

    @Indexed
    private String userId; // Reference to User id

    @Indexed // MongoDB TTL index: automatically deletes expired tokens
    private Instant expiryDate;

    private Instant createdAt;
}