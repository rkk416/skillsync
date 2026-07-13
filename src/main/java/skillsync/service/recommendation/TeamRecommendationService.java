package skillsync.service.recommendation;

import skillsync.model.Student;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

public final class TeamRecommendationService {
    private final StudentRecommendationService students;

    public TeamRecommendationService(StudentRepository studentRepository, SkillRepository skillRepository,
                                     TeamRepository teamRepository, ScoreCalculator scores,
                                     ExplanationGenerator explanations) {
        this.students = new StudentRecommendationService(studentRepository, skillRepository, teamRepository, scores, explanations);
    }

    public java.util.List<RankedRecommendation<Student>> recommendBalancedTeammates(int studentId) throws java.sql.SQLException {
        return students.recommend(studentId);
    }
}
