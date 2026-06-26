package skillsync.model;

import java.time.LocalDateTime;

public class TeamJoinRequest {
    private int id;
    private int teamId;
    private int studentId;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TeamJoinRequest() { }

    public TeamJoinRequest(int id, int teamId, int studentId, String status, LocalDateTime requestedAt,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.teamId = teamId;
        this.studentId = studentId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override public String toString() {
        return "TeamJoinRequest{id=" + id + ", teamId=" + teamId + ", studentId=" + studentId
                + ", status='" + status + "', requestedAt=" + requestedAt + ", createdAt=" + createdAt
                + ", updatedAt=" + updatedAt + "}";
    }
}
