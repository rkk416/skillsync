package skillsync.service;

import org.junit.jupiter.api.Test;
import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;
import skillsync.service.recommendation.RecommendationEngine;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationEngineTest {
    @Test void ranksCompaniesAndExplainsScoreDeterministically() throws Exception {
        RecommendationEngine engine = new RecommendationEngine(new FakeSkills(), new FakeCompanies(), new FakeStudents(), new FakeTeams());

        var companies = engine.recommendCompanies(1);
        assertEquals("Backend Labs", companies.getFirst().item().getName());
        assertTrue(companies.getFirst().score() > companies.get(1).score());
        assertEquals("Low", companies.getFirst().confidence());
        assertFalse(companies.getFirst().reasons().isEmpty());

        var skills = engine.recommendSkills(1);
        assertEquals(List.of("Docker", "React"), skills.stream().map(recommendation -> recommendation.item().getName()).toList());

        var teammates = engine.recommendTeammates(1);
        assertEquals("Maya Rao", teammates.getFirst().item().getFullName());
    }

    private static final class FakeSkills extends SkillRepository {
        private final List<Skill> catalog = List.of(
                new Skill(1, "Java", "Backend", null),
                new Skill(2, "SQL", "Database", null),
                new Skill(3, "Docker", "DevOps", null),
                new Skill(4, "React", "Frontend", null)
        );

        @Override public Map<Integer, Integer> findProficienciesByStudent(int studentId) {
            if (studentId == 1) return Map.of(1, 5, 2, 4);
            if (studentId == 2) return Map.of(1, 4, 3, 3);
            return Map.of(4, 4);
        }

        @Override public Map<Integer, Integer> findRequirementsByCompany(int companyId) {
            if (companyId == 1) return Map.of(1, 4, 2, 3, 3, 2);
            return Map.of(3, 3, 4, 3);
        }

        @Override public List<Skill> findAll() {
            return catalog;
        }
    }

    private static final class FakeCompanies extends CompanyRepository {
        @Override public List<Company> findAll() {
            return List.of(
                    new Company(1, "Backend Labs", "Software", null, BigDecimal.valueOf(7.5)),
                    new Company(2, "Frontend Works", "Design", null, BigDecimal.valueOf(7.0))
            );
        }
    }

    private static final class FakeStudents extends StudentRepository {
        private final List<Student> students = List.of(
                new Student(1, 1, "SkillSync University", "B.Tech", 2027, "Backend", "Ram", "Computer Science", 8.4),
                new Student(2, 2, "SkillSync University", "B.Tech", 2027, "DevOps", "Maya Rao", "Computer Science", 8.8),
                new Student(3, 3, "Other University", "B.Des", 2027, "Frontend", "Nina Das", "Design", 8.2)
        );

        @Override public Optional<Student> findById(int id) {
            return students.stream().filter(student -> student.getId() == id).findFirst();
        }

        @Override public List<Student> findAll() {
            return students;
        }
    }

    private static final class FakeTeams extends TeamRepository {
        @Override public Map<Integer, List<Integer>> loadMembershipGraph() {
            return Map.of();
        }
    }
}
