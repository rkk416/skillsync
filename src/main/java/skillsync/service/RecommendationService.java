package skillsync.service;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;

import java.util.List;

public interface RecommendationService {
    List<Skill> recommendSkills(int studentId);
    List<Company> recommendCompanies(int studentId);
    List<Student> recommendTeammates(int studentId);
}
