package skillsync.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecommendationHistory {
    private int id;
    private int studentId;
    private String recommendationType;
    private int targetId;
    private BigDecimal score;
    private String algorithmVersion;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;

    public RecommendationHistory() { }

    public RecommendationHistory(int id, int studentId, String recommendationType, int targetId, BigDecimal score,
                                 String algorithmVersion, LocalDateTime generatedAt, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.recommendationType = recommendationType;
        this.targetId = targetId;
        this.score = score;
        this.algorithmVersion = algorithmVersion;
        this.generatedAt = generatedAt;
        this.createdAt = createdAt;
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
    public String getAlgorithmVersion() { return algorithmVersion; }
    public void setAlgorithmVersion(String algorithmVersion) { this.algorithmVersion = algorithmVersion; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "RecommendationHistory{id=" + id + ", studentId=" + studentId + ", recommendationType='"
                + recommendationType + "', targetId=" + targetId + ", score=" + score
                + ", algorithmVersion='" + algorithmVersion + "', generatedAt=" + generatedAt
                + ", createdAt=" + createdAt + "}";
    }
}
