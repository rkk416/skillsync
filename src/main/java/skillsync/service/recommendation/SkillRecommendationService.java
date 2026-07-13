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

public final class SkillRecommendationService {
    private final SkillRepository skills;
    private final CompanyRepository companies;
    private final ScoreCalculator scores;
    private final ExplanationGenerator explanations;

    public SkillRecommendationService(SkillRepository skills, CompanyRepository companies, ScoreCalculator scores,
                                      ExplanationGenerator explanations) {
        this.skills = skills;
        this.companies = companies;
        this.scores = scores;
        this.explanations = explanations;
    }

    public List<RankedRecommendation<Skill>> recommend(int studentId) throws SQLException {
        Map<Integer, Integer> owned = skills.findProficienciesByStudent(studentId);
        Map<Integer, Integer> demand = demandBySkill();
        return skills.findAll().stream()
                .filter(skill -> !owned.containsKey(skill.getId()))
                .filter(skill -> demand.containsKey(skill.getId()))
                .map(skill -> scoreSkill(skill, demand.getOrDefault(skill.getId(), 0), demand.size()))
                .sorted(Comparator.comparingDouble((RankedRecommendation<Skill> recommendation) -> recommendation.score()).reversed()
                        .thenComparing(recommendation -> recommendation.item().getName(), String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .toList();
    }

    private RankedRecommendation<Skill> scoreSkill(Skill skill, int demandCount, int totalDemandedSkills) {
        double demandScore = scores.ratio(demandCount, Math.max(1, totalDemandedSkills));
        double categorySignal = skill.getCategory() == null || skill.getCategory().isBlank() ? 50 : 75;
        double finalScore = scores.weighted(Map.of(
                "skillMatch", demandScore,
                "projects", categorySignal,
                "experience", 60.0,
                "interests", categorySignal,
                "education", 60.0,
                "profileCompletion", 55.0,
                "activity", 50.0
        ));
        return new RankedRecommendation<>(skill, finalScore, scores.confidence(finalScore),
                explanations.skillReasons(skill, demandCount),
                List.of("Practice " + skill.getName() + " in a portfolio project.", "Add proof of proficiency after learning."));
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
}
