package skillsync.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Number> generateSkillStatistics();
    Map<String, Number> generatePlacementStatistics();
    Map<String, Number> generatePlacementStatusDistribution();
    Map<String, Number> generateIndustryDistribution();
    Map<String, Number> generateRecommendationDistribution();
    Map<String, Number> generateCollaborationTrend();
    Map<String, Number> generateLearningTrend();
    long countRecommendations();
    long countApplications();
    long countSkills();
    long countActiveCollaborationsThisWeek();
    double countPlacementSuccessRate();
    long countActiveTeams();
    java.util.List<String> getTopCompanyNames();
}
