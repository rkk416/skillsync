package skillsync.service.recommendation;

import skillsync.model.Student;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StudentRecommendationService {
    private final StudentRepository students;
    private final SkillRepository skills;
    private final TeamRepository teams;
    private final ScoreCalculator scores;
    private final ExplanationGenerator explanations;

    public StudentRecommendationService(StudentRepository students, SkillRepository skills, TeamRepository teams,
                                        ScoreCalculator scores, ExplanationGenerator explanations) {
        this.students = students;
        this.skills = skills;
        this.teams = teams;
        this.scores = scores;
        this.explanations = explanations;
    }

    public List<RankedRecommendation<Student>> recommend(int studentId) throws SQLException {
        Student source = students.findById(studentId).orElse(null);
        Map<Integer, Integer> sourceSkills = skills.findProficienciesByStudent(studentId);
        Set<Integer> previousCollaborators = new HashSet<>(teams.loadMembershipGraph().getOrDefault(studentId, List.of()));
        return students.findAll().stream()
                .filter(candidate -> candidate.getId() != studentId)
                .filter(candidate -> !previousCollaborators.contains(candidate.getId()))
                .map(candidate -> scoreStudent(source, candidate, sourceSkills))
                .sorted(Comparator.comparingDouble((RankedRecommendation<Student> recommendation) -> recommendation.score()).reversed()
                        .thenComparing(recommendation -> displayName(recommendation.item()), String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .toList();
    }

    private RankedRecommendation<Student> scoreStudent(Student source, Student candidate, Map<Integer, Integer> sourceSkills) {
        try {
            Map<Integer, Integer> candidateSkills = skills.findProficienciesByStudent(candidate.getId());
            Set<Integer> shared = new HashSet<>(sourceSkills.keySet());
            shared.retainAll(candidateSkills.keySet());
            Set<Integer> complementary = new HashSet<>(candidateSkills.keySet());
            complementary.removeAll(sourceSkills.keySet());
            boolean educationMatch = source != null && safeEquals(source.getDegree(), candidate.getDegree());
            boolean departmentMatch = source != null && safeEquals(source.getBranch(), candidate.getBranch());
            double finalScore = scores.weighted(Map.of(
                    "skillMatch", scores.ratio(shared.size(), Math.max(1, sourceSkills.size())),
                    "projects", scores.ratio(complementary.size(), Math.max(1, candidateSkills.size())),
                    "experience", candidate.getCgpa() == null ? 55.0 : Math.min(100, candidate.getCgpa() * 10),
                    "interests", departmentMatch ? 80.0 : 55.0,
                    "education", educationMatch ? 90.0 : 55.0,
                    "profileCompletion", profileCompletion(candidate),
                    "activity", 55.0
            ));
            return new RankedRecommendation<>(candidate, finalScore, scores.confidence(finalScore),
                    explanations.studentReasons(candidate, shared.size(), complementary.size(), educationMatch),
                    complementary.isEmpty() ? List.of("Explore shared project goals.") : List.of("Use complementary skills to divide project responsibilities."));
        } catch (SQLException exception) {
            return new RankedRecommendation<>(candidate, 0, "Very Low", List.of("Unable to score this student right now."), List.of("Try again after skill data is available."));
        }
    }

    private double profileCompletion(Student student) {
        int complete = 0;
        if (student.getFullName() != null && !student.getFullName().isBlank()) complete++;
        if (student.getUniversity() != null && !student.getUniversity().isBlank()) complete++;
        if (student.getDegree() != null && !student.getDegree().isBlank()) complete++;
        if (student.getBranch() != null && !student.getBranch().isBlank()) complete++;
        if (student.getBio() != null && !student.getBio().isBlank()) complete++;
        if (student.getCgpa() != null) complete++;
        return scores.ratio(complete, 6);
    }

    private boolean safeEquals(String first, String second) {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }

    private String displayName(Student student) {
        return student.getFullName() == null || student.getFullName().isBlank() ? "Student " + student.getId() : student.getFullName();
    }
}
