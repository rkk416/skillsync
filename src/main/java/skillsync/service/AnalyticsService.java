package skillsync.service;

import java.util.Map;

public interface AnalyticsService {
    Map<String, Number> generateSkillStatistics();
    Map<String, Number> generatePlacementStatistics();
}
