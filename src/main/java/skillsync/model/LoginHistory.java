package skillsync.model;

import java.time.LocalDateTime;

public class LoginHistory {
    private int id;
    private int userId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private LocalDateTime createdAt;

    public LoginHistory() { }

    public LoginHistory(int id, int userId, LocalDateTime loginTime, LocalDateTime logoutTime, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "LoginHistory{id=" + id + ", userId=" + userId + ", loginTime=" + loginTime
                + ", logoutTime=" + logoutTime + ", createdAt=" + createdAt + "}";
    }
}
