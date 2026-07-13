package skillsync.service.recommendation;

import java.util.List;

public record RankedRecommendation<T>(T item, double score, String confidence, List<String> reasons,
                                      List<String> suggestedImprovements) {
    public RankedRecommendation {
        reasons = List.copyOf(reasons);
        suggestedImprovements = List.copyOf(suggestedImprovements);
    }
}
