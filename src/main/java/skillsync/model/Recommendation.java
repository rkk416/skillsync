package skillsync.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Recommendation {
    private int id;
    private int studentId;
    private String recommendationType;
    private int targetId;
    private BigDecimal score;
    private String reason;
    private LocalDateTime createdAt;

    public Recommendation() { }
    public Recommendation(int id, int studentId, String recommendationType, int targetId, BigDecimal score, String reason, LocalDateTime createdAt) {
        this.id = id; this.studentId = studentId; this.recommendationType = recommendationType; this.targetId = targetId;
        this.score = score; this.reason = reason; this.createdAt = createdAt;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRecommendationType() { return recommendationType; }
    public void setRecommendationType(String recommendationType) { this.recommendationType = recommendationType; }
    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    @Override public String toString() { return "Recommendation{id=" + id + ", studentId=" + studentId + ", recommendationType='" + recommendationType + "', targetId=" + targetId + ", score=" + score + ", reason='" + reason + "', createdAt=" + createdAt + "}"; }
}
