package skillsync.recommendation;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.service.RecommendationService;
import skillsync.service.RecommendationServiceImpl;
import skillsync.utils.ControllerSupport;

import java.util.List;

public final class RecommendationController extends ControllerSupport {
    private final RecommendationService service = new RecommendationServiceImpl();
    public List<Skill> skills() { return service.recommendSkills(currentStudentId()); }
    public List<Company> companies() { return service.recommendCompanies(currentStudentId()); }
    public List<Student> teammates() { return service.recommendTeammates(currentStudentId()); }
}
