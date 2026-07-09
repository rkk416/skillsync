package skillsync.dashboard;

import skillsync.repository.SkillRepository;
import skillsync.repository.TeamRepository;
import skillsync.service.PlacementService;
import skillsync.service.PlacementServiceImpl;
import skillsync.service.RecommendationService;
import skillsync.service.RecommendationServiceImpl;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DashboardController extends ControllerSupport {
    private final PlacementService placement = new PlacementServiceImpl();
    private final RecommendationService recommendations = new RecommendationServiceImpl();
    private final SkillRepository skills = new SkillRepository();
    private final TeamRepository teams = new TeamRepository();
    
    public Map<String, Number> metrics() {
        try {
            int id = currentStudentId(); 
            Map<String, Number> values = new LinkedHashMap<>();
            values.put("Placement Score", placement.calculatePlacementScore(id)); 
            values.put("Skills Count", skills.findByStudentId(id).size());
            values.put("Recommendations", recommendations.recommendSkills(id).size() + recommendations.recommendCompanies(id).size() + recommendations.recommendTeammates(id).size());
            values.put("Team Count", teams.countByStudentId(id)); 
            return values;
        } catch (SQLException e) { 
            throw new ServiceException("Unable to load dashboard", e); 
        }
    }
}
