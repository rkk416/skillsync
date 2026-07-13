package skillsync.service;

import skillsync.model.Certification;
import skillsync.model.Project;
import skillsync.model.Skill;
import skillsync.model.Student;

public interface ProfileService {
    Student getStudentById(int id);
    Student updateProfile(Student student);
    void addSkill(int studentId, Skill skill);
    void updateSkill(int studentId, Skill skill);
    void removeSkill(int studentId, int skillId);
    void addCertification(int studentId, Certification cert);
    void updateCertification(int studentId, Certification cert);
    void removeCertification(int studentId, int certificationId);
    void addProject(int studentId, Project project);
    void updateProject(int studentId, Project project);
    void removeProject(int studentId, int projectId);
}