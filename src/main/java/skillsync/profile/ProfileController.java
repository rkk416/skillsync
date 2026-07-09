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
    public boolean saveProfile(Student student) { return executeBoolean(() -> service.updateProfile(student)); }
    public boolean addSkill(String name, String category) { return executeBoolean(() -> service.addSkill(currentStudentId(), new Skill(0, name, category, null))); }
    public boolean updateSkill(Skill skill) { return executeBoolean(() -> service.updateSkill(currentStudentId(), skill)); }
    public void deleteSkill(int skillId) { execute(() -> service.removeSkill(currentStudentId(), skillId)); }
    public void addCertification(Certification value) { execute(() -> service.addCertification(currentStudentId(), value)); }
    public boolean saveCertification(Certification value) { return executeBoolean(() -> { if (value.getId() == 0) service.addCertification(currentStudentId(), value); else service.updateCertification(currentStudentId(), value); }); }
    public boolean saveProject(Project value) { return executeBoolean(() -> { if (value.getId() == 0) service.addProject(currentStudentId(), value); else service.updateProject(currentStudentId(), value); }); }
    public void deleteCertification(int id) { execute(() -> service.removeCertification(currentStudentId(), id)); }
    public void deleteProject(int id) { execute(() -> service.removeProject(currentStudentId(), id)); }
}