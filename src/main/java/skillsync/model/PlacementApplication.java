package skillsync.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlacementApplication {
    private int id;
    private int studentId;
    private int companyId;
    private String status;
    private BigDecimal placementScore;
    private LocalDateTime appliedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PlacementApplication() { }

    public PlacementApplication(int id, int studentId, int companyId, String status, BigDecimal placementScore,
                                LocalDateTime appliedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.companyId = companyId;
        this.status = status;
        this.placementScore = placementScore;
        this.appliedAt = appliedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getPlacementScore() { return placementScore; }
    public void setPlacementScore(BigDecimal placementScore) { this.placementScore = placementScore; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override public String toString() {
        return "PlacementApplication{id=" + id + ", studentId=" + studentId + ", companyId=" + companyId
                + ", status='" + status + "', placementScore=" + placementScore + ", appliedAt=" + appliedAt
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "}";
    }
}
