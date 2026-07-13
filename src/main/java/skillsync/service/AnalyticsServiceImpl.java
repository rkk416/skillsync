package skillsync.service;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AnalyticsServiceImpl implements AnalyticsService {
    private final SkillRepository skills;
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final TeamRepository teams;

    public AnalyticsServiceImpl() { this(new SkillRepository(), new StudentRepository(), new CompanyRepository(), new TeamRepository()); }
    public AnalyticsServiceImpl(SkillRepository skills, StudentRepository students, CompanyRepository companies, TeamRepository teams) {
        this.skills = skills; this.students = students; this.companies = companies; this.teams = teams;
    }

    @Override public Map<String, Number> generateSkillStatistics() {
        try {
            Map<String, Integer> aggregated = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            for (Skill skill : skills.findAll()) aggregated.merge(skill.getCategory() == null || skill.getCategory().isBlank() ? "Uncategorized" : skill.getCategory(), 1, Integer::sum);
            return new LinkedHashMap<>(aggregated);
        } catch (SQLException exception) { throw new ServiceException("Unable to generate skill statistics", exception); }
    }

    @Override public Map<String, Number> generatePlacementStatistics() {
        try {
            List<Student> allStudents = students.findAll();
            List<Company> allCompanies = companies.findAll();
            long studentCount = allStudents.size();
            long companyCount = allCompanies.size();
            long eligiblePairs = 0;
            long totalPairs = studentCount * companyCount;
            for (Student student : allStudents) {
                for (Company company : allCompanies) {
                    if (skills.findMissingForCompany(student.getId(), company.getId()).isEmpty()) eligiblePairs++;
                }
            }
            Map<String, Number> result = new LinkedHashMap<>();
            result.put("Students", studentCount); result.put("Companies", companyCount); result.put("Eligibility Rate", totalPairs == 0 ? 0 : Math.round(eligiblePairs * 10_000.0 / totalPairs) / 100.0);
            result.put("Teams", teams.findAll().size()); return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to generate placement statistics", exception); }
    }
}
