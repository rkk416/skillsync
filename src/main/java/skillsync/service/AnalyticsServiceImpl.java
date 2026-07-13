package skillsync.service;

import skillsync.repository.ActivityLogRepository;
import skillsync.repository.CompanyRepository;
import skillsync.repository.PlacementApplicationRepository;
import skillsync.repository.RecommendationRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnalyticsServiceImpl implements AnalyticsService {
    private final SkillRepository skills;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final TeamRepository teams;
    private final RecommendationRepository recommendations;
    private final PlacementApplicationRepository applications;
    private final ActivityLogRepository activityLogs;

    public AnalyticsServiceImpl() {
        this(new SkillRepository(), new StudentRepository(), new CompanyRepository(),
             new TeamRepository(), new RecommendationRepository(),
             new PlacementApplicationRepository(), new ActivityLogRepository());
    }

    public AnalyticsServiceImpl(SkillRepository skills, StudentRepository students,
                                CompanyRepository companies, TeamRepository teams,
                                RecommendationRepository recommendations,
                                PlacementApplicationRepository applications,
                                ActivityLogRepository activityLogs) {
        this.skills = skills;
        this.students = students;
        this.companies = companies;
        this.teams = teams;
        this.recommendations = recommendations;
        this.applications = applications;
        this.activityLogs = activityLogs;
    }

    @Override
    public Map<String, Number> generateSkillStatistics() {
        try {
            return new LinkedHashMap<>(skills.getSkillDistribution());
        } catch (SQLException e) { throw new ServiceException("Unable to generate skill statistics", e); }
    }

    @Override
    public Map<String, Number> generatePlacementStatistics() {
        try {
            long studentCount = students.count();
            long companyCount = companies.count();
            long totalPairs = studentCount * companyCount;
            long eligiblePairs = students.countEligiblePairs();
            Map<String, Number> result = new LinkedHashMap<>();
            result.put("Students", studentCount);
            result.put("Companies", companyCount);
            result.put("Eligibility Rate", totalPairs == 0 ? 0 : Math.round(eligiblePairs * 10_000.0 / totalPairs) / 100.0);
            result.put("Teams", teams.count());
            return result;
        } catch (SQLException e) { throw new ServiceException("Unable to generate placement statistics", e); }
    }

    @Override
    public Map<String, Number> generateIndustryDistribution() {
        try {
            return new LinkedHashMap<>(companies.getIndustryDistribution());
        } catch (SQLException e) { throw new ServiceException("Unable to generate industry distribution", e); }
    }

    @Override
    public Map<String, Number> generateRecommendationDistribution() {
        try {
            Map<String, Integer> raw = recommendations.getRecommendationTypeDistribution();
            if (!raw.isEmpty()) return new LinkedHashMap<>(raw);
            // Fallback to placement status when no recommendations exist yet
            return new LinkedHashMap<>(applications.getPlacementStatusDistribution());
        } catch (SQLException e) { throw new ServiceException("Unable to generate recommendation distribution", e); }
    }

    @Override
    public Map<String, Number> generateCollaborationTrend() {
        try {
            Map<String, Integer> raw = activityLogs.getDailyActivityThisWeek();
            return new LinkedHashMap<>(raw);
        } catch (SQLException e) { throw new ServiceException("Unable to generate collaboration trend", e); }
    }

    @Override
    public Map<String, Number> generateLearningTrend() {
        try {
            Map<String, Integer> raw = activityLogs.getWeeklyActivityTimeline();
            return new LinkedHashMap<>(raw);
        } catch (SQLException e) { throw new ServiceException("Unable to generate learning trend", e); }
    }

    @Override
    public long countRecommendations() {
        try { return recommendations.count(); }
        catch (SQLException e) { throw new ServiceException("Unable to count recommendations", e); }
    }

    @Override
    public long countApplications() {
        try { return applications.count(); }
        catch (SQLException e) { throw new ServiceException("Unable to count applications", e); }
    }

    @Override
    public long countSkills() {
        try { return skills.findAll().size(); }
        catch (SQLException e) { throw new ServiceException("Unable to count skills", e); }
    }

    @Override
    public long countActiveCollaborationsThisWeek() {
        try { return activityLogs.countThisWeek(); }
        catch (SQLException e) { throw new ServiceException("Unable to count active collaborations", e); }
    }
}
