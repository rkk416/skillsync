package skillsync.service;

import org.junit.jupiter.api.Test;
import skillsync.model.ActivityLog;
import skillsync.model.Company;
import skillsync.model.Recommendation;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.model.Team;
import skillsync.repository.ActivityLogRepository;
import skillsync.repository.CompanyRepository;
import skillsync.repository.RecommendationRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardIntelligenceServiceTest {
    @Test void generatesRepositoryDrivenDashboardSignals() {
        DashboardIntelligenceService service = new DashboardIntelligenceService(
                new FakeStudents(), new FakeCompanies(), new FakeSkills(), new FakeTeams(),
                new FakeRecommendations(), new FakeActivities());

        DashboardIntelligenceService.DashboardIntelligence result = service.generate(1);

        assertEquals(2, result.metrics().get("Students"));
        assertEquals(1, result.metrics().get("Companies"));
        assertEquals(2, result.metrics().get("Skills"));
        assertEquals(1, result.metrics().get("Teams"));
        assertEquals(1, result.metrics().get("Recommendations"));
        assertEquals(2L, result.metrics().get("Collaborations"));
        assertEquals(List.of("PROFILE: Updated profile summary."), result.recentActivities());
        assertEquals(List.of("Java", "SQL"), result.popularSkills());
        assertTrue(result.quickInsights().getFirst().contains("Java"));
    }

    @Test void rejectsInvalidStudentId() {
        DashboardIntelligenceService service = new DashboardIntelligenceService(
                new FakeStudents(), new FakeCompanies(), new FakeSkills(), new FakeTeams(),
                new FakeRecommendations(), new FakeActivities());

        assertThrows(IllegalArgumentException.class, () -> service.generate(0));
    }

    private static final class FakeStudents extends StudentRepository {
        @Override public List<Student> findAll() {
            return List.of(new Student(1, 1, "A", "B.Tech", 2027, null),
                    new Student(2, 2, "B", "B.Tech", 2027, null));
        }
    }

    private static final class FakeCompanies extends CompanyRepository {
        @Override public List<Company> findAll() {
            return List.of(new Company(1, "Backend Labs", "Software", null, BigDecimal.valueOf(7.5)));
        }
    }

    private static final class FakeSkills extends SkillRepository {
        @Override public List<Skill> findAll() {
            return List.of(new Skill(1, "Java", "Backend", null), new Skill(2, "SQL", "Database", null));
        }
    }

    private static final class FakeTeams extends TeamRepository {
        @Override public List<Team> findAll() {
            return List.of(new Team(1, "Platform", "Backend team", 1, LocalDateTime.now()));
        }

        @Override public long countByStudentId(int studentId) {
            return 2;
        }
    }

    private static final class FakeRecommendations extends RecommendationRepository {
        @Override public List<Recommendation> findAll() {
            return List.of(new Recommendation(1, 1, "SKILL", 2, BigDecimal.valueOf(0.75), "Learn SQL", LocalDateTime.now()));
        }
    }

    private static final class FakeActivities extends ActivityLogRepository {
        @Override public List<ActivityLog> findByStudentId(int studentId) {
            return List.of(new ActivityLog(1, studentId, "PROFILE", "Updated profile summary.", LocalDateTime.now()));
        }
    }
}
