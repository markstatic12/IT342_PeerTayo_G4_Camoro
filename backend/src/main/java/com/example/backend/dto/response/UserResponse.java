package com.example.backend.dto.response;

import java.util.List;

public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;

    public UserResponse() {}

    public UserResponse(Long id, String firstName, String lastName, String email, List<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public List<String> getRoles() { return roles; }

    public void setId(Long id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private List<String> roles;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder roles(List<String> roles) { this.roles = roles; return this; }

        public UserResponse build() { return new UserResponse(id, firstName, lastName, email, roles); }
    }
}
