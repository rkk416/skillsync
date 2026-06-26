package skillsync.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private int id;
    private int studentId;
    private String activityType;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLog() { }

    public ActivityLog(int id, int studentId, String activityType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.activityType = activityType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "ActivityLog{id=" + id + ", studentId=" + studentId + ", activityType='" + activityType
                + "', description='" + description + "', createdAt=" + createdAt + "}";
    }
}
