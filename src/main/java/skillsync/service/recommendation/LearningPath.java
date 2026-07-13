package skillsync.service.recommendation;

import skillsync.model.Skill;

import java.util.List;

public record LearningPath(List<Skill> currentSkills, List<Skill> missingSkills, List<Skill> recommendedOrder,
                           List<String> suggestedProjects, double careerReadiness) {
    public LearningPath {
        currentSkills = List.copyOf(currentSkills);
        missingSkills = List.copyOf(missingSkills);
        recommendedOrder = List.copyOf(recommendedOrder);
        suggestedProjects = List.copyOf(suggestedProjects);
    }
}
