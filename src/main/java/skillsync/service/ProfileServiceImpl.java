package skillsync.service;

import skillsync.model.Certification;
import skillsync.model.Project;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CertificationRepository;
import skillsync.repository.ProjectRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class ProfileServiceImpl implements ProfileService {
    private final StudentRepository students;
    private final SkillRepository skills;
    private final CertificationRepository certifications;
    private final ProjectRepository projects;

    public ProfileServiceImpl() { this(new StudentRepository(), new SkillRepository(), new CertificationRepository(), new ProjectRepository()); }
    public ProfileServiceImpl(StudentRepository students, SkillRepository skills, CertificationRepository certifications, ProjectRepository projects) {
        this.students = Objects.requireNonNull(students); this.skills = Objects.requireNonNull(skills);
        this.certifications = Objects.requireNonNull(certifications); this.projects = Objects.requireNonNull(projects);
    }

    @Override public Student getStudentById(int id) {
        requirePositive(id, "Student id");
        try { return students.findById(id).orElseThrow(() -> new IllegalArgumentException("Student was not found")); }
        catch (SQLException exception) {
    exception.printStackTrace();
    throw new ServiceException("Unable to save data", exception);
}
    }

    @Override public Student updateProfile(Student student) {
        Objects.requireNonNull(student, "Student is required"); requirePositive(student.getId(), "Student id");
        if (student.getFullName() == null || student.getFullName().isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (student.getCgpa() != null && (student.getCgpa() < 0.0 || student.getCgpa() > 10.0)) throw new IllegalArgumentException("CGPA must be between 0 and 10");
        if (student.getGraduationYear() != 0 && student.getGraduationYear() < LocalDate.now().getYear()) throw new IllegalArgumentException("Graduation year must be the current year or later");
        if (student.getGraduationYear() != 0 && student.getGraduationYear() > 2200) throw new IllegalArgumentException("Graduation year is invalid");
        try { if (!students.update(student)) throw new IllegalArgumentException("Student was not found"); return student; }
        catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void addSkill(int studentId, Skill skill) {
        requirePositive(studentId, "Student id"); Objects.requireNonNull(skill, "Skill is required");
        if (skill.getName() == null || skill.getName().isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        try {
            Skill stored = skills.findByName(skill.getName()).orElseGet(() -> {
                try { return skills.create(skill); } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
            });
            skills.addToStudent(studentId, stored.getId(), 1);
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void updateSkill(int studentId, Skill skill) {
        requirePositive(studentId, "Student id"); Objects.requireNonNull(skill, "Skill is required");
        if (skill.getName() == null || skill.getName().isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        try {
            boolean owned = skills.findByStudentId(studentId).stream().anyMatch(existing -> existing.getId() == skill.getId());
            if (!owned) throw new IllegalArgumentException("Skill was not found");
            if (!skills.update(skill)) throw new IllegalArgumentException("Skill was not found");
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void removeSkill(int studentId, int skillId) {
        requirePositive(studentId, "Student id"); requirePositive(skillId, "Skill id");
        try { if (!skills.removeFromStudent(studentId, skillId)) throw new IllegalArgumentException("Skill was not found"); }
        catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void addCertification(int studentId, Certification certification) {
        requirePositive(studentId, "Student id");
        validateCertification(certification);
        certification.setStudentId(studentId);
        try { certifications.create(certification); } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void updateCertification(int studentId, Certification certification) {
        requirePositive(studentId, "Student id");
        validateCertification(certification);
        try {
            Certification existing = certifications.findById(certification.getId()).orElseThrow(() -> new IllegalArgumentException("Certification was not found"));
            if (existing.getStudentId() != studentId) throw new IllegalArgumentException("Certification was not found");
            certification.setStudentId(studentId);
            if (!certifications.update(certification)) throw new IllegalArgumentException("Certification was not found");
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void removeCertification(int studentId, int certificationId) {
        requirePositive(studentId, "Student id"); requirePositive(certificationId, "Certification id");
        try {
            Certification existing = certifications.findById(certificationId).orElseThrow(() -> new IllegalArgumentException("Certification was not found"));
            if (existing.getStudentId() != studentId) throw new IllegalArgumentException("Certification was not found");
            if (!certifications.deleteById(certificationId)) throw new IllegalArgumentException("Certification was not found");
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void addProject(int studentId, Project project) {
        requirePositive(studentId, "Student id");
        validateProject(project);
        project.setOwnerStudentId(studentId);
        try { projects.create(project); } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void updateProject(int studentId, Project project) {
        requirePositive(studentId, "Student id");
        validateProject(project);
        try {
            Project existing = projects.findById(project.getId()).orElseThrow(() -> new IllegalArgumentException("Project was not found"));
            if (existing.getOwnerStudentId() != studentId) throw new IllegalArgumentException("Project was not found");
            project.setOwnerStudentId(studentId);
            if (!projects.update(project)) throw new IllegalArgumentException("Project was not found");
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    @Override public void removeProject(int studentId, int projectId) {
        requirePositive(studentId, "Student id"); requirePositive(projectId, "Project id");
        try {
            Project existing = projects.findById(projectId).orElseThrow(() -> new IllegalArgumentException("Project was not found"));
            if (existing.getOwnerStudentId() != studentId) throw new IllegalArgumentException("Project was not found");
            if (!projects.deleteById(projectId)) throw new IllegalArgumentException("Project was not found");
        } catch (SQLException exception) { throw new ServiceException("Unable to save data", exception); }
    }

    private void validateCertification(Certification certification) {
        Objects.requireNonNull(certification, "Certification is required");
        if (certification.getName() == null || certification.getName().isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (certification.getIssuingOrganization() == null || certification.getIssuingOrganization().isBlank()) throw new IllegalArgumentException("Issuing organization is required");
        if (certification.getIssueDate() != null && certification.getExpiryDate() != null && certification.getExpiryDate().isBefore(certification.getIssueDate())) throw new IllegalArgumentException("Expiry date cannot be before the issue date");
    }

    private void validateProject(Project project) {
        Objects.requireNonNull(project, "Project is required");
        if (project.getName() == null || project.getName().isBlank()) throw new IllegalArgumentException("Name cannot be empty");
        if (project.getStartDate() != null && project.getEndDate() != null && project.getEndDate().isBefore(project.getStartDate())) throw new IllegalArgumentException("End date cannot be before the start date");
    }

    private void requirePositive(int value, String label) { if (value <= 0) throw new IllegalArgumentException(label + " must be positive"); }
}