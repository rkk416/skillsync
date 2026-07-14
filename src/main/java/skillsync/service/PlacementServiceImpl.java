package skillsync.service;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PlacementServiceImpl implements PlacementService {
    private final SkillRepository skills;
    private final CompanyRepository companies;

    public PlacementServiceImpl() { this(new SkillRepository(), new CompanyRepository()); }
    public PlacementServiceImpl(SkillRepository skills, CompanyRepository companies) { this.skills = skills; this.companies = companies; }

    @Override public double calculatePlacementScore(int studentId) {
        requirePositive(studentId);
        try {
            Map<Integer, Integer> owned = skills.findProficienciesByStudent(studentId);
            List<Company> allCompanies = companies.findAll();
            int required = 0; double achieved = 0;
            for (Company company : allCompanies) {
                for (Map.Entry<Integer, Integer> requirement : skills.findRequirementsByCompany(company.getId()).entrySet()) {
                    required++;
                    achieved += Math.min(1.0, owned.getOrDefault(requirement.getKey(), 0) / (double) requirement.getValue());
                }
            }
            return required == 0 ? 0 : Math.round(achieved * 10_000.0 / required) / 100.0;
        } catch (SQLException exception) { throw new ServiceException("Unable to calculate placement score", exception); }
    }

    @Override public List<Skill> findSkillGap(int studentId, int companyId) {
        requirePositive(studentId); requirePositive(companyId);
        try {
            List<Skill> result = new ArrayList<>(skills.findMissingForCompany(studentId, companyId));
            result.sort(Comparator.comparing(Skill::getName, String.CASE_INSENSITIVE_ORDER));
            return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to calculate the skill gap", exception); }
    }

    @Override public boolean checkEligibility(int studentId, int companyId) { return findSkillGap(studentId, companyId).isEmpty(); }

    @Override
    public boolean applyForPlacement(int studentId, int companyId, String status, double score) {
        requirePositive(studentId);
        requirePositive(companyId);
        try {
            skillsync.repository.PlacementApplicationRepository appRepo = new skillsync.repository.PlacementApplicationRepository();
            // Delete existing application if there is one to allow re-applying/updating status
            try {
                java.util.List<skillsync.model.PlacementApplication> existing = appRepo.findByStudentId(studentId);
                for (var app : existing) {
                    if (app.getCompanyId() == companyId) {
                        appRepo.deleteById(app.getId());
                    }
                }
            } catch (Exception ignored) {}

            skillsync.model.PlacementApplication app = new skillsync.model.PlacementApplication(
                0, studentId, companyId, status, java.math.BigDecimal.valueOf(score), null, null, null
            );
            appRepo.create(app);
            return true;
        } catch (SQLException exception) {
            throw new ServiceException("Unable to apply for placement", exception);
        }
    }

    private void requirePositive(int value) { if (value <= 0) throw new IllegalArgumentException("Identifier must be positive"); }
}
