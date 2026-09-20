package com.taskflow.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taskflow.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

	private final String secret;
	private final long expiration;

	public JwtService(
			@Value("${taskflow.jwt.secret}") String secret,
			@Value("${taskflow.jwt.expiration}") long expiration) {
		this.secret = secret;
		this.expiration = expiration;
	}

	@PostConstruct
	void validateConfiguration() {
		if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
			throw new IllegalStateException("TASKFLOW_JWT_SECRET must contain at least 32 characters.");
		}
		if (expiration <= 0) {
			throw new IllegalStateException("TASKFLOW_JWT_EXPIRATION must be greater than zero.");
		}
	}

	public String generateToken(User user) {
		Instant issuedAt = Instant.now();
		return Jwts.builder()
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(issuedAt.plusMillis(expiration)))
				.signWith(signingKey())
				.compact();
	}

	public String extractEmail(String token) {
		return claims(token).getSubject();
	}

	private Claims claims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey signingKey() {
		return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}
}
