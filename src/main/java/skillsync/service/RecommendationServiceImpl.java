package skillsync.service;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;
import skillsync.service.recommendation.LearningPath;
import skillsync.service.recommendation.RankedRecommendation;
import skillsync.service.recommendation.RecommendationEngine;

import java.sql.SQLException;
import java.util.List;

public class RecommendationServiceImpl implements RecommendationService {
    private final RecommendationEngine engine;

    public RecommendationServiceImpl() {
        this(new SkillRepository(), new CompanyRepository(), new StudentRepository(), new TeamRepository());
    }

    public RecommendationServiceImpl(SkillRepository skills, CompanyRepository companies, StudentRepository students, TeamRepository teams) {
        this.engine = new RecommendationEngine(skills, companies, students, teams);
    }

    @Override public List<Skill> recommendSkills(int studentId) {
        validate(studentId);
        try {
            return engine.recommendSkills(studentId).stream().map(RankedRecommendation::item).toList();
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend skills", exception); }
    }

    @Override public List<Company> recommendCompanies(int studentId) {
        validate(studentId);
        try {
            return engine.recommendCompanies(studentId).stream().map(RankedRecommendation::item).toList();
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend companies", exception); }
    }

    @Override public List<Student> recommendTeammates(int studentId) {
        validate(studentId);
        try {
            return engine.recommendTeammates(studentId).stream().map(RankedRecommendation::item).toList();
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend teammates", exception); }
    }

    public List<RankedRecommendation<Skill>> explainableSkillRecommendations(int studentId) {
        validate(studentId);
        try { return engine.recommendSkills(studentId); }
        catch (SQLException exception) { throw new ServiceException("Unable to recommend skills", exception); }
    }

    public List<RankedRecommendation<Company>> explainableCompanyRecommendations(int studentId) {
        validate(studentId);
        try { return engine.recommendCompanies(studentId); }
        catch (SQLException exception) { throw new ServiceException("Unable to recommend companies", exception); }
    }

    public List<RankedRecommendation<Student>> explainableTeammateRecommendations(int studentId) {
        validate(studentId);
        try { return engine.recommendTeammates(studentId); }
        catch (SQLException exception) { throw new ServiceException("Unable to recommend teammates", exception); }
    }

    public LearningPath learningPath(int studentId) {
        validate(studentId);
        try { return engine.learningPath(studentId); }
        catch (SQLException exception) { throw new ServiceException("Unable to generate a learning path", exception); }
    }

    private void validate(int studentId) { if (studentId <= 0) throw new IllegalArgumentException("Student id must be positive"); }
}
