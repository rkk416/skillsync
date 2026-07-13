package skillsync.service.recommendation;

import java.util.Map;

public record RecommendationWeights(double skillMatch, double projects, double experience, double interests,
                                    double education, double profileCompletion, double activity) {
    public static RecommendationWeights defaults() {
        return new RecommendationWeights(0.35, 0.20, 0.15, 0.10, 0.10, 0.05, 0.05);
    }

    public double weightedScore(Map<String, Double> factors) {
        double score = 0;
        score += factors.getOrDefault("skillMatch", 0.0) * skillMatch;
        score += factors.getOrDefault("projects", 0.0) * projects;
        score += factors.getOrDefault("experience", 0.0) * experience;
        score += factors.getOrDefault("interests", 0.0) * interests;
        score += factors.getOrDefault("education", 0.0) * education;
        score += factors.getOrDefault("profileCompletion", 0.0) * profileCompletion;
        score += factors.getOrDefault("activity", 0.0) * activity;
        return Math.max(0, Math.min(100, score));
    }
}
