package skillsync.service;

import skillsync.model.ActivityLog;
import skillsync.model.Recommendation;
import skillsync.model.Skill;
import skillsync.repository.ActivityLogRepository;
import skillsync.repository.CompanyRepository;
import skillsync.repository.RecommendationRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardIntelligenceService {
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final SkillRepository skills;
    private final TeamRepository teams;
    private final RecommendationRepository recommendations;
    private final ActivityLogRepository activities;

    public DashboardIntelligenceService() {
        this(new StudentRepository(), new CompanyRepository(), new SkillRepository(), new TeamRepository(),
                new RecommendationRepository(), new ActivityLogRepository());
    }

    public DashboardIntelligenceService(StudentRepository students, CompanyRepository companies, SkillRepository skills,
                                       TeamRepository teams, RecommendationRepository recommendations,
                                       ActivityLogRepository activities) {
        this.students = Objects.requireNonNull(students);
        this.companies = Objects.requireNonNull(companies);
        this.skills = Objects.requireNonNull(skills);
        this.teams = Objects.requireNonNull(teams);
        this.recommendations = Objects.requireNonNull(recommendations);
        this.activities = Objects.requireNonNull(activities);
    }

    public DashboardIntelligence generate(int studentId) {
        if (studentId <= 0) throw new IllegalArgumentException("Student id must be positive");
        try {
            List<Skill> allSkills = skills.findAll();
            List<Recommendation> allRecommendations = recommendations.findAll();
            Map<String, Number> metrics = new LinkedHashMap<>();
            metrics.put("Students", students.findAll().size());
            metrics.put("Companies", companies.findAll().size());
            metrics.put("Skills", allSkills.size());
            metrics.put("Teams", teams.findAll().size());
            metrics.put("Recommendations", allRecommendations.size());
            metrics.put("Collaborations", teams.countByStudentId(studentId));
            return new DashboardIntelligence(metrics, recentActivities(studentId), popularSkills(allSkills), insights(allSkills, allRecommendations));
        } catch (SQLException exception) {
            throw new ServiceException("Unable to generate dashboard intelligence", exception);
        }
    }

    private List<String> recentActivities(int studentId) throws SQLException {
        List<String> logs = activities.findByStudentId(studentId).stream()
                .limit(3)
                .map(this::formatActivity)
                .toList();
        return logs.isEmpty() ? List.of("No recent activity recorded yet.", "Complete profile details to unlock richer insights.") : logs;
    }

    private List<String> popularSkills(List<Skill> allSkills) {
        List<String> names = allSkills.stream().map(Skill::getName).limit(8).toList();
        return names.isEmpty() ? List.of("Java", "SQL", "PostgreSQL", "JavaFX") : names;
    }

    private List<String> insights(List<Skill> allSkills, List<Recommendation> allRecommendations) {
        String topSkill = allSkills.isEmpty() ? "No demanded skill has enough data yet." : "Trending technology: " + allSkills.getFirst().getName() + ".";
        String recommendationCount = allRecommendations.size() + " recommendation signal(s) are available for review.";
        return List.of(topSkill, recommendationCount, "SkillSync is ready for deeper profile and placement integration.");
    }

    private String formatActivity(ActivityLog activity) {
        String type = activity.getActivityType() == null || activity.getActivityType().isBlank() ? "Activity" : activity.getActivityType();
        String description = activity.getDescription() == null || activity.getDescription().isBlank() ? "No description provided." : activity.getDescription();
        return type + ": " + description;
    }

    public record DashboardIntelligence(Map<String, Number> metrics, List<String> recentActivities,
                                        List<String> popularSkills, List<String> quickInsights) {
        public DashboardIntelligence {
            metrics = new LinkedHashMap<>(metrics);
            recentActivities = List.copyOf(recentActivities);
            popularSkills = List.copyOf(popularSkills);
            quickInsights = List.copyOf(quickInsights);
        }
    }
}
