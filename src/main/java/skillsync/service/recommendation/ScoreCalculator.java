package skillsync.service.recommendation;

import java.util.Map;

public final class ScoreCalculator {
    private final RecommendationWeights weights;

    public ScoreCalculator() {
        this(RecommendationWeights.defaults());
    }

    public ScoreCalculator(RecommendationWeights weights) {
        this.weights = weights;
    }

    public double weighted(Map<String, Double> factors) {
        return round(weights.weightedScore(factors));
    }

    public double skillCoverage(Map<Integer, Integer> owned, Map<Integer, Integer> required) {
        if (required.isEmpty()) return 100;
        double achieved = 0;
        for (Map.Entry<Integer, Integer> requirement : required.entrySet()) {
            int ownedLevel = owned.getOrDefault(requirement.getKey(), 0);
            achieved += Math.min(1.0, ownedLevel / (double) requirement.getValue());
        }
        return round(achieved * 100 / required.size());
    }

    public double ratio(int value, int total) {
        return total == 0 ? 0 : round(value * 100.0 / total);
    }

    public String confidence(double score) {
        if (score >= 95) return "Very High";
        if (score >= 80) return "High";
        if (score >= 65) return "Medium";
        if (score >= 50) return "Low";
        return "Very Low";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
