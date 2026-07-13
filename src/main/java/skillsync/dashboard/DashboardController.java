package skillsync.dashboard;

import skillsync.repository.SkillRepository;
import skillsync.repository.TeamRepository;
import skillsync.service.PlacementService;
import skillsync.service.PlacementServiceImpl;
import skillsync.service.RecommendationService;
import skillsync.service.RecommendationServiceImpl;
import skillsync.service.DashboardIntelligenceService;
import skillsync.service.DashboardIntelligenceService.DashboardIntelligence;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DashboardController extends ControllerSupport {
    private final PlacementService placement = new PlacementServiceImpl();
    private final RecommendationService recommendations = new RecommendationServiceImpl();
    private final DashboardIntelligenceService intelligence = new DashboardIntelligenceService();
    private final SkillRepository skills = new SkillRepository();
    private final TeamRepository teams = new TeamRepository();
    
    public DashboardOverview overview() {
        int studentId = currentStudentId();
        return new DashboardOverview(metricsFor(studentId), intelligence.generate(studentId));
    }

    public Map<String, Number> metrics() {
        return metricsFor(currentStudentId());
    }

    public DashboardIntelligence intelligence() {
        return intelligence.generate(currentStudentId());
    }

    private Map<String, Number> metricsFor(int studentId) {
        try {
            Map<String, Number> values = new LinkedHashMap<>();
            values.put("Placement Score", placement.calculatePlacementScore(studentId));
            values.put("Skills Count", skills.findByStudentId(studentId).size());
            values.put("Recommendations", recommendations.recommendSkills(studentId).size()
                    + recommendations.recommendCompanies(studentId).size()
                    + recommendations.recommendTeammates(studentId).size());
            values.put("Team Count", teams.countByStudentId(studentId));
            return values;
        } catch (SQLException e) { 
            throw new ServiceException("Unable to load dashboard", e); 
        }
    }

    public record DashboardOverview(Map<String, Number> metrics, DashboardIntelligence intelligence) { }
}
