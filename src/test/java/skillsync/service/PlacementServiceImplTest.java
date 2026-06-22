package skillsync.service;

import org.junit.jupiter.api.Test;
import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PlacementServiceImplTest {
    @Test void calculatesScoreAndSortedSkillGap() {
        PlacementService service = new PlacementServiceImpl(new FakeSkills(), new FakeCompanies());
        assertEquals(50.0, service.calculatePlacementScore(1));
        assertFalse(service.checkEligibility(1, 1));
        assertEquals(List.of("Java", "SQL"), service.findSkillGap(1, 1).stream().map(Skill::getName).toList());
    }
    private static final class FakeSkills extends SkillRepository {
        @Override public Map<Integer, Integer> findProficienciesByStudent(int id) { return Map.of(1, 2); }
        @Override public Map<Integer, Integer> findRequirementsByCompany(int id) { return Map.of(1, 4); }
        @Override public List<Skill> findMissingForCompany(int studentId, int companyId) { return List.of(new Skill(2, "SQL", null, null), new Skill(1, "Java", null, null)); }
    }
    private static final class FakeCompanies extends CompanyRepository {
        @Override public List<Company> findAll() { return List.of(new Company(1, "Company", null, null, null)); }
    }
}
