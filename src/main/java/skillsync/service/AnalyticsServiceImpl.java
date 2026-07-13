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
            return new LinkedHashMap<>(skills.getSkillDistribution());
        } catch (SQLException exception) { throw new ServiceException("Unable to generate skill statistics", exception); }
    }

    @Override public Map<String, Number> generatePlacementStatistics() {
        try {
            long studentCount = students.count();
            long companyCount = companies.count();
            long totalPairs = studentCount * companyCount;
            long eligiblePairs = students.countEligiblePairs();
            Map<String, Number> result = new LinkedHashMap<>();
            result.put("Students", studentCount); result.put("Companies", companyCount); result.put("Eligibility Rate", totalPairs == 0 ? 0 : Math.round(eligiblePairs * 10_000.0 / totalPairs) / 100.0);
            result.put("Teams", teams.count()); return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to generate placement statistics", exception); }
    }
}
