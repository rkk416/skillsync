package skillsync.profile;

import skillsync.model.Certification;
import skillsync.model.Project;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CertificationRepository;
import skillsync.repository.ProjectRepository;
import skillsync.repository.SkillRepository;
import skillsync.service.ProfileService;
import skillsync.service.ProfileServiceImpl;
import skillsync.service.ServiceException;
import skillsync.utils.ControllerSupport;

import java.sql.SQLException;
import java.util.List;

public final class ProfileController extends ControllerSupport {
    private final ProfileService service = new ProfileServiceImpl();
    private final SkillRepository skills = new SkillRepository();
    private final CertificationRepository certifications = new CertificationRepository();
    private final ProjectRepository projects = new ProjectRepository();

    public Student loadProfile() { return service.getStudentById(currentStudentId()); }
    public List<Skill> loadSkills() { try { return skills.findByStudentId(currentStudentId()); } catch (SQLException e) { throw new ServiceException("Unable to load skills", e); } }
    public List<Certification> loadCertifications() { try { return certifications.findAll().stream().filter(c -> c.getStudentId() == currentStudentId()).toList(); } catch (SQLException e) { throw new ServiceException("Unable to load certifications", e); } }
    public List<Project> loadProjects() { try { return projects.findAll().stream().filter(p -> p.getOwnerStudentId() == currentStudentId()).toList(); } catch (SQLException e) { throw new ServiceException("Unable to load projects", e); } }
    public void saveProfile(Student student) { execute(() -> service.updateProfile(student)); }
    public void addSkill(String name, String category) { execute(() -> service.addSkill(currentStudentId(), new Skill(0, name, category, null))); }
    public void deleteSkill(int skillId) { execute(() -> { try { skills.removeFromStudent(currentStudentId(), skillId); } catch (SQLException e) { throw new ServiceException("Unable to remove skill", e); } }); }
    public void addCertification(Certification value) { execute(() -> service.addCertification(currentStudentId(), value)); }
    public void saveCertification(Certification value) { execute(() -> { try { if (value.getId() == 0) service.addCertification(currentStudentId(), value); else certifications.update(value); } catch (SQLException e) { throw new ServiceException("Unable to save certification", e); } }); }
    public void saveProject(Project value) { execute(() -> { value.setOwnerStudentId(currentStudentId()); try { if (value.getId() == 0) projects.create(value); else projects.update(value); } catch (SQLException e) { throw new ServiceException("Unable to save project", e); } }); }
    public void deleteCertification(int id) { execute(() -> { try { certifications.deleteById(id); } catch (SQLException e) { throw new ServiceException("Unable to delete certification", e); } }); }
    public void deleteProject(int id) { execute(() -> { try { projects.deleteById(id); } catch (SQLException e) { throw new ServiceException("Unable to delete project", e); } }); }
}
