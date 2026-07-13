package skillsync.repository;

import skillsync.model.Skill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class SkillRepository extends BaseRepository {

    public Skill create(Skill skill) throws SQLException {
        String sql = "INSERT INTO skills (name, category, description) VALUES (?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, skill.getName());
            statement.setString(2, skill.getCategory());
            statement.setString(3, skill.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) skill.setId(keys.getInt(1));
            }
            return skill;
        }
    }

    public Optional<Skill> findById(int id) throws SQLException {
        String sql = "SELECT id, name, category, description FROM skills WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Skill> findAll() throws SQLException {
        String sql = "SELECT id, name, category, description FROM skills ORDER BY id";
        List<Skill> skills = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) skills.add(map(resultSet));
        }
        return skills;
    }

    public Optional<Skill> findByName(String name) throws SQLException {
        String sql = "SELECT id, name, category, description FROM skills WHERE LOWER(name) = LOWER(?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
            }
        }
    }

    // Proficiencies the student owns: skill_id -> proficiency_level
    // NOTE: the actual column in student_skills is "proficiency_level" (see schema.sql).
    // This previously queried a non-existent "proficiency" column and threw at runtime.
    public Map<Integer, Integer> findProficienciesByStudent(int studentId) throws SQLException {
        String sql = "SELECT skill_id, proficiency_level FROM student_skills WHERE student_id = ?";
        Map<Integer, Integer> result = new HashMap<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("skill_id"), rs.getInt("proficiency_level"));
                }
            }
        }
        return result;
    }

    // Requirements for a company: skill_id -> minimum proficiency
    public Map<Integer, Integer> findRequirementsByCompany(int companyId) throws SQLException {
        String sql = "SELECT skill_id, minimum_proficiency FROM company_requirements WHERE company_id = ?";
        Map<Integer, Integer> result = new HashMap<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, companyId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("skill_id"), rs.getInt("minimum_proficiency"));
                }
            }
        }
        return result;
    }

    public List<Skill> findMissingForCompany(int studentId, int companyId) throws SQLException {
        Map<Integer, Integer> owned = findProficienciesByStudent(studentId);
        Map<Integer, Integer> required = findRequirementsByCompany(companyId);
        if (required.isEmpty()) return Collections.emptyList();

        // missing = either not owned or owned proficiency < required proficiency
        List<Integer> missingSkillIds = required.entrySet().stream()
                .filter(e -> owned.getOrDefault(e.getKey(), 0) < e.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (missingSkillIds.isEmpty()) return Collections.emptyList();

        String placeholders = missingSkillIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT id, name, category, description FROM skills WHERE id IN (" + placeholders + ")";

        List<Skill> result = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < missingSkillIds.size(); i++) {
                statement.setInt(i + 1, missingSkillIds.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    // Fixed: was inserting/updating a non-existent "proficiency" column.
    public boolean addToStudent(int studentId, int skillId, int proficiency) throws SQLException {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level) VALUES (?, ?, ?) " +
                "ON CONFLICT (student_id, skill_id) DO UPDATE SET proficiency_level = EXCLUDED.proficiency_level";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, skillId);
            statement.setInt(3, proficiency);
            return statement.executeUpdate() >= 1;
        }
    }

    public boolean update(Skill skill) throws SQLException {
        String sql = "UPDATE skills SET name = ?, category = ?, description = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, skill.getName());
            statement.setString(2, skill.getCategory());
            statement.setString(3, skill.getDescription());
            statement.setInt(4, skill.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public List<Skill> findByStudentIdList(int studentId) throws SQLException {
        String sql = "SELECT s.id, s.name, s.category, s.description " +
                "FROM skills s JOIN student_skills ss ON ss.skill_id = s.id WHERE ss.student_id = ?";
        List<Skill> result = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<Skill> findByStudentIdSkills(int studentId) throws SQLException {
        return findByStudentIdList(studentId);
    }

    public boolean removeFromStudent(int studentId, int skillId) throws SQLException {
        String sql = "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, skillId);
            return statement.executeUpdate() == 1;
        }
    }

    private Skill map(ResultSet resultSet) throws SQLException {
        return new Skill(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getString("description")
        );
    }

    // Compatibility method expected by services
    public List<Skill> findByStudentId(int studentId) throws SQLException {
        return findByStudentIdSkills(studentId);
    }

    public Map<String, Integer> getSkillDistribution() throws SQLException {
        String sql = "SELECT COALESCE(NULLIF(category, ''), 'Uncategorized') as cat, COUNT(*) as cnt FROM skills GROUP BY COALESCE(NULLIF(category, ''), 'Uncategorized') ORDER BY cnt DESC";
        Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString("cat"), rs.getInt("cnt"));
        }
        return result;
    }
}
