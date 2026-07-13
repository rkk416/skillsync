package skillsync.service.recommendation;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LearningRecommendationService {
    private final SkillRepository skills;
    private final CompanyRepository companies;

    public LearningRecommendationService(SkillRepository skills, CompanyRepository companies) {
        this.skills = skills;
        this.companies = companies;
    }

    public LearningPath generatePath(int studentId) throws SQLException {
        Map<Integer, Integer> owned = skills.findProficienciesByStudent(studentId);
        List<Skill> catalog = skills.findAll();
        Map<Integer, Integer> demand = demandBySkill();
        List<Skill> current = catalog.stream().filter(skill -> owned.containsKey(skill.getId())).toList();
        List<Skill> missing = catalog.stream().filter(skill -> demand.containsKey(skill.getId()) && !owned.containsKey(skill.getId())).toList();
        List<Skill> ordered = missing.stream()
                .sorted(Comparator.comparing((Skill skill) -> demand.getOrDefault(skill.getId(), 0)).reversed()
                        .thenComparing(Skill::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .toList();
        double readiness = demand.isEmpty() ? 100 : Math.round((demand.size() - missing.size()) * 10_000.0 / demand.size()) / 100.0;
        return new LearningPath(current, missing, ordered, projectIdeas(ordered), readiness);
    }

    private Map<Integer, Integer> demandBySkill() throws SQLException {
        Map<Integer, Integer> demand = new HashMap<>();
        for (Company company : companies.findAll()) {
            for (int skillId : skills.findRequirementsByCompany(company.getId()).keySet()) {
                demand.merge(skillId, 1, Integer::sum);
            }
        }
        return demand;
    }

    private List<String> projectIdeas(List<Skill> ordered) {
        if (ordered.isEmpty()) return List.of("Build a polished portfolio case study using existing strong skills.");
        return ordered.stream()
                .limit(3)
                .map(skill -> "Create a focused project demonstrating " + skill.getName() + ".")
                .toList();
    }
}
