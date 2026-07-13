package skillsync.service.recommendation;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.List;

public final class RecommendationEngine {
    private final SkillRecommendationService skillRecommendations;
    private final CompanyRecommendationService companyRecommendations;
    private final TeamRecommendationService teamRecommendations;
    private final LearningRecommendationService learningRecommendations;

    public RecommendationEngine(SkillRepository skills, CompanyRepository companies, StudentRepository students,
                                TeamRepository teams) {
        ScoreCalculator scores = new ScoreCalculator();
        SkillGapAnalyzer gaps = new SkillGapAnalyzer();
        ExplanationGenerator explanations = new ExplanationGenerator();
        this.skillRecommendations = new SkillRecommendationService(skills, companies, scores, explanations);
        this.companyRecommendations = new CompanyRecommendationService(skills, companies, scores, gaps, explanations);
        this.teamRecommendations = new TeamRecommendationService(students, skills, teams, scores, explanations);
        this.learningRecommendations = new LearningRecommendationService(skills, companies);
    }

    public List<RankedRecommendation<Skill>> recommendSkills(int studentId) throws SQLException {
        return skillRecommendations.recommend(studentId);
    }

    public List<RankedRecommendation<Company>> recommendCompanies(int studentId) throws SQLException {
        return companyRecommendations.recommend(studentId);
    }

    public List<RankedRecommendation<Student>> recommendTeammates(int studentId) throws SQLException {
        return teamRecommendations.recommendBalancedTeammates(studentId);
    }

    public LearningPath learningPath(int studentId) throws SQLException {
        return learningRecommendations.generatePath(studentId);
    }
}
