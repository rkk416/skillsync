package skillsync.service.recommendation;

import skillsync.model.Skill;

import java.util.List;

public record SkillGapAnalysis(List<Skill> strongSkills, List<Skill> weakSkills, List<Skill> missingSkills,
                               List<Skill> suggestedSkills) {
    public SkillGapAnalysis {
        strongSkills = List.copyOf(strongSkills);
        weakSkills = List.copyOf(weakSkills);
        missingSkills = List.copyOf(missingSkills);
        suggestedSkills = List.copyOf(suggestedSkills);
    }
}
