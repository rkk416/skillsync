package skillsync.service;

import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.repository.CompanyRepository;
import skillsync.repository.SkillRepository;
import skillsync.repository.StudentRepository;
import skillsync.repository.TeamRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class RecommendationServiceImpl implements RecommendationService {
    private final SkillRepository skills;
    private final CompanyRepository companies;
    private final StudentRepository students;
    private final TeamRepository teams;

    public RecommendationServiceImpl() { this(new SkillRepository(), new CompanyRepository(), new StudentRepository(), new TeamRepository()); }
    public RecommendationServiceImpl(SkillRepository skills, CompanyRepository companies, StudentRepository students, TeamRepository teams) {
        this.skills = skills; this.companies = companies; this.students = students; this.teams = teams;
    }

    @Override public List<Skill> recommendSkills(int studentId) {
        validate(studentId);
        try {
            Set<Integer> owned = skills.findProficienciesByStudent(studentId).keySet();
            Map<Integer, Integer> demand = new HashMap<>();
            for (Company company : companies.findAll()) for (int skillId : skills.findRequirementsByCompany(company.getId()).keySet()) if (!owned.contains(skillId)) demand.merge(skillId, 1, Integer::sum);
            PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(Map.Entry.<Integer, Integer>comparingByValue().reversed());
            queue.addAll(demand.entrySet());
            List<Skill> result = new ArrayList<>();
            while (!queue.isEmpty() && result.size() < 10) skills.findById(queue.remove().getKey()).ifPresent(result::add);
            return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend skills", exception); }
    }

    @Override public List<Company> recommendCompanies(int studentId) {
        validate(studentId);
        try {
            Map<Integer, Integer> owned = skills.findProficienciesByStudent(studentId);
            PriorityQueue<Scored<Company>> queue = new PriorityQueue<>(Comparator.comparingDouble(Scored<Company>::score).reversed());
            for (Company company : companies.findAll()) {
                Map<Integer, Integer> requirements = skills.findRequirementsByCompany(company.getId());
                long matches = requirements.entrySet().stream().filter(entry -> owned.getOrDefault(entry.getKey(), 0) >= entry.getValue()).count();
                queue.add(new Scored<>(company, requirements.isEmpty() ? 1 : matches / (double) requirements.size()));
            }
            List<Company> result = new ArrayList<>(); while (!queue.isEmpty() && result.size() < 10) result.add(queue.remove().value()); return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend companies", exception); }
    }

    @Override public List<Student> recommendTeammates(int studentId) {
        validate(studentId);
        try {
            Map<Integer, List<Integer>> graph = teams.loadMembershipGraph();
            Set<Integer> direct = new HashSet<>(graph.getOrDefault(studentId, List.of()));
            Map<Integer, Integer> mutualCounts = new HashMap<>();
            for (int teammate : direct) for (int candidate : graph.getOrDefault(teammate, List.of())) if (candidate != studentId && !direct.contains(candidate)) mutualCounts.merge(candidate, 1, Integer::sum);
            PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(Map.Entry.<Integer, Integer>comparingByValue().reversed()); queue.addAll(mutualCounts.entrySet());
            List<Student> result = new ArrayList<>(); while (!queue.isEmpty() && result.size() < 10) students.findById(queue.remove().getKey()).ifPresent(result::add); return result;
        } catch (SQLException exception) { throw new ServiceException("Unable to recommend teammates", exception); }
    }

    private void validate(int studentId) { if (studentId <= 0) throw new IllegalArgumentException("Student id must be positive"); }
    private record Scored<T>(T value, double score) { }
}
