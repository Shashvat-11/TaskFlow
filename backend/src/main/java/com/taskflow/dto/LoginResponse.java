package com.taskflow.dto;

public record LoginResponse(String token, String tokenType, AuthenticatedUserResponse user) {
}
