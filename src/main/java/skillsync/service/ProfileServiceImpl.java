package skillsync.service;

import skillsync.model.Certification;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CertificationRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;

import java.sql.SQLException;
import java.util.Objects;

public class ProfileServiceImpl implements ProfileService {
    private final StudentRepository students;
    private final SkillRepository skills;
    private final CertificationRepository certifications;

    public ProfileServiceImpl() { this(new StudentRepository(), new SkillRepository(), new CertificationRepository()); }
    public ProfileServiceImpl(StudentRepository students, SkillRepository skills, CertificationRepository certifications) {
        this.students = Objects.requireNonNull(students); this.skills = Objects.requireNonNull(skills); this.certifications = Objects.requireNonNull(certifications);
    }

    @Override public Student getStudentById(int id) {
        requirePositive(id, "Student id");
        try { return students.findById(id).orElseThrow(() -> new IllegalArgumentException("Student was not found")); }
        catch (SQLException exception) { throw new ServiceException("Unable to load the profile", exception); }
    }

    @Override public Student updateProfile(Student student) {
        Objects.requireNonNull(student, "Student is required"); requirePositive(student.getId(), "Student id");
        if (student.getGraduationYear() != 0 && (student.getGraduationYear() < 1900 || student.getGraduationYear() > 2200)) throw new IllegalArgumentException("Graduation year is invalid");
        try { if (!students.update(student)) throw new IllegalArgumentException("Student was not found"); return student; }
        catch (SQLException exception) { throw new ServiceException("Unable to update the profile", exception); }
    }

    @Override public void addSkill(int studentId, Skill skill) {
        requirePositive(studentId, "Student id"); Objects.requireNonNull(skill, "Skill is required");
        if (skill.getName() == null || skill.getName().isBlank()) throw new IllegalArgumentException("Skill name is required");
        try {
            Skill stored = skills.findByName(skill.getName()).orElseGet(() -> {
                try { return skills.create(skill); } catch (SQLException exception) { throw new ServiceException("Unable to create the skill", exception); }
            });
            skills.addToStudent(studentId, stored.getId(), 1);
        } catch (SQLException exception) { throw new ServiceException("Unable to add the skill", exception); }
    }

    @Override public void addCertification(int studentId, Certification certification) {
        requirePositive(studentId, "Student id"); Objects.requireNonNull(certification, "Certification is required");
        if (certification.getName() == null || certification.getName().isBlank()) throw new IllegalArgumentException("Certification name is required");
        if (certification.getIssuingOrganization() == null || certification.getIssuingOrganization().isBlank()) throw new IllegalArgumentException("Issuing organization is required");
        certification.setStudentId(studentId);
        try { certifications.create(certification); } catch (SQLException exception) { throw new ServiceException("Unable to add the certification", exception); }
    }

    private void requirePositive(int value, String label) { if (value <= 0) throw new IllegalArgumentException(label + " must be positive"); }
}
