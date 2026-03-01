package com.example.backend.dto.response;

public class AuthResponse {

    private UserResponse user;
    private String token;

    public AuthResponse() {}

    public AuthResponse(UserResponse user, String token) {
        this.user = user;
        this.token = token;
    }

    public UserResponse getUser() { return user; }
    public String getToken() { return token; }
    public void setUser(UserResponse user) { this.user = user; }
    public void setToken(String token) { this.token = token; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UserResponse user;
        private String token;

        public Builder user(UserResponse user) { this.user = user; return this; }
        public Builder token(String token) { this.token = token; return this; }

        public AuthResponse build() { return new AuthResponse(user, token); }
    }
}
