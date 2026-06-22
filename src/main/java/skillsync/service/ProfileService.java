package skillsync.service;

import skillsync.model.Certification;
import skillsync.model.Skill;
import skillsync.model.Student;

public interface ProfileService {
    Student getStudentById(int id);
    Student updateProfile(Student student);
    void addSkill(int studentId, Skill skill);
    void addCertification(int studentId, Certification cert);
}
