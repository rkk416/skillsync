package skillsync.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;

    public User() { }

    public User(int id, String email, String passwordHash, String fullName, String role, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', fullName='" + fullName + "', role='" + role + "', createdAt=" + createdAt + '}';
    }
}
