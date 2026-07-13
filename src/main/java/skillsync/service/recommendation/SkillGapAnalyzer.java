package skillsync.service.recommendation;

import skillsync.model.Skill;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class SkillGapAnalyzer {
    public SkillGapAnalysis analyze(Map<Integer, Integer> owned, Map<Integer, Integer> required, List<Skill> catalog) {
        Map<Integer, Skill> byId = catalog.stream().collect(java.util.stream.Collectors.toMap(Skill::getId, skill -> skill, (first, second) -> first));
        List<Skill> strong = owned.entrySet().stream()
                .filter(entry -> entry.getValue() >= 4)
                .map(entry -> byId.get(entry.getKey()))
                .filter(skill -> skill != null)
                .sorted(byName())
                .toList();
        List<Skill> weak = owned.entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && entry.getValue() < 3)
                .map(entry -> byId.get(entry.getKey()))
                .filter(skill -> skill != null)
                .sorted(byName())
                .toList();
        List<Skill> missing = required.entrySet().stream()
                .filter(entry -> owned.getOrDefault(entry.getKey(), 0) < entry.getValue())
                .map(entry -> byId.get(entry.getKey()))
                .filter(skill -> skill != null)
                .sorted(byName())
                .toList();
        List<Skill> suggested = missing.stream().limit(5).toList();
        return new SkillGapAnalysis(strong, weak, missing, suggested);
    }

    private Comparator<Skill> byName() {
        return Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER);
    }
}
