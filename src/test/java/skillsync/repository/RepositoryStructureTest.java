package skillsync.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryStructureTest {
    @Test void allRepositoriesExtendBaseRepository() {
        Class<?>[] repositories = {UserRepository.class, StudentRepository.class, SkillRepository.class,
                CertificationRepository.class, ProjectRepository.class, CompanyRepository.class,
                TeamRepository.class, RecommendationRepository.class};
        for (Class<?> repository : repositories) assertTrue(BaseRepository.class.isAssignableFrom(repository));
    }
}
