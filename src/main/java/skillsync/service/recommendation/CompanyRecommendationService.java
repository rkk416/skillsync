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

public final class CompanyRecommendationService {
    private final SkillRepository skills;
    private final CompanyRepository companies;
    private final ScoreCalculator scores;
    private final SkillGapAnalyzer gaps;
    private final ExplanationGenerator explanations;

    public CompanyRecommendationService(SkillRepository skills, CompanyRepository companies, ScoreCalculator scores,
                                        SkillGapAnalyzer gaps, ExplanationGenerator explanations) {
        this.skills = skills;
        this.companies = companies;
        this.scores = scores;
        this.gaps = gaps;
        this.explanations = explanations;
    }

    public List<RankedRecommendation<Company>> recommend(int studentId) throws SQLException {
        Map<Integer, Integer> owned = skills.findProficienciesByStudent(studentId);
        List<Skill> catalog = skills.findAll();
        return companies.findAll().stream()
                .map(company -> scoreCompany(company, owned, catalog))
                .sorted(Comparator.comparingDouble((RankedRecommendation<Company> recommendation) -> recommendation.score()).reversed()
                        .thenComparing(recommendation -> recommendation.item().getName(), String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .toList();
    }

    private RankedRecommendation<Company> scoreCompany(Company company, Map<Integer, Integer> owned, List<Skill> catalog) {
        try {
            Map<Integer, Integer> required = skills.findRequirementsByCompany(company.getId());
            SkillGapAnalysis analysis = gaps.analyze(owned, required, catalog);
            double coverage = scores.skillCoverage(owned, required);
            double finalScore = scores.weighted(Map.of(
                    "skillMatch", coverage,
                    "projects", profileSignal(owned.size(), 8),
                    "experience", profileSignal(owned.size(), 6),
                    "interests", company.getIndustry() == null ? 45.0 : 70.0,
                    "education", 70.0,
                    "profileCompletion", profileSignal(owned.size(), 5),
                    "activity", 55.0
            ));
            return new RankedRecommendation<>(company, finalScore, scores.confidence(finalScore),
                    explanations.companyReasons(company, coverage, analysis.missingSkills().size()),
                    explanations.improvements(analysis.missingSkills()));
        } catch (SQLException exception) {
            return new RankedRecommendation<>(company, 0, "Very Low", List.of("Unable to score this company right now."), List.of("Try again after repository data is available."));
        }
    }

    private double profileSignal(int value, int target) {
        return Math.min(100, value * 100.0 / target);
    }
}
