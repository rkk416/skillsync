package skillsync.recommendation;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.service.RecommendationServiceImpl;
import skillsync.service.recommendation.RankedRecommendation;
import skillsync.utils.ControllerSupport;

import java.util.List;

public final class RecommendationController extends ControllerSupport {
    private final RecommendationServiceImpl service = new RecommendationServiceImpl();
    public List<Skill> skills() { return service.recommendSkills(currentStudentId()); }
    public List<Company> companies() { return service.recommendCompanies(currentStudentId()); }
    public List<Student> teammates() { return service.recommendTeammates(currentStudentId()); }
    public List<RankedRecommendation<Skill>> skillRecommendations() { return service.explainableSkillRecommendations(currentStudentId()); }
    public List<RankedRecommendation<Company>> companyRecommendations() { return service.explainableCompanyRecommendations(currentStudentId()); }
    public List<RankedRecommendation<Student>> teammateRecommendations() { return service.explainableTeammateRecommendations(currentStudentId()); }
}
