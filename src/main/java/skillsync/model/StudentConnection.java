package skillsync.model;

import java.time.LocalDateTime;

public class StudentConnection {
    private int studentId;
    private int connectedStudentId;
    private String status;
    private LocalDateTime createdAt;

    public StudentConnection() { }

    public StudentConnection(int studentId, int connectedStudentId, String status, LocalDateTime createdAt) {
        this.studentId = studentId;
        this.connectedStudentId = connectedStudentId;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getConnectedStudentId() { return connectedStudentId; }
    public void setConnectedStudentId(int connectedStudentId) { this.connectedStudentId = connectedStudentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "StudentConnection{studentId=" + studentId + ", connectedStudentId=" + connectedStudentId
                + ", status='" + status + "', createdAt=" + createdAt + "}";
    }
}
