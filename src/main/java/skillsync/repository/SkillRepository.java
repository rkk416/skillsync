package skillsync.repository;

import skillsync.model.Skill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillRepository extends BaseRepository {
    public Skill create(Skill skill) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO skills (name, category, description) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, skill.getName()); statement.setString(2, skill.getCategory()); statement.setString(3, skill.getDescription());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) skill.setId(keys.getInt(1)); }
            return skill;
        }
    }

    public Optional<Skill> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, category, description FROM skills WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public Optional<Skill> findByName(String name) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, category, description FROM skills WHERE LOWER(name) = LOWER(?)")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Skill> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT s.id, s.name, s.category, s.description FROM skills s JOIN student_skills ss ON ss.skill_id = s.id WHERE ss.student_id = ? ORDER BY s.name";
        List<Skill> skills = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) skills.add(map(resultSet)); }
        }
        return skills;
    }

    public void addToStudent(int studentId, int skillId, int proficiencyLevel) throws SQLException {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level) VALUES (?, ?, ?) ON CONFLICT (student_id, skill_id) DO UPDATE SET proficiency_level = EXCLUDED.proficiency_level";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId); statement.setInt(2, skillId); statement.setInt(3, proficiencyLevel); statement.executeUpdate();
        }
    }

    public boolean removeFromStudent(int studentId, int skillId) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?")) {
            statement.setInt(1, studentId); statement.setInt(2, skillId); return statement.executeUpdate() == 1;
        }
    }

    public Map<Integer, Integer> findProficienciesByStudent(int studentId) throws SQLException {
        Map<Integer, Integer> values = new LinkedHashMap<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT skill_id, proficiency_level FROM student_skills WHERE student_id = ?")) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) values.put(resultSet.getInt(1), resultSet.getInt(2)); }
        }
        return values;
    }

    public Map<Integer, Integer> findRequirementsByCompany(int companyId) throws SQLException {
        Map<Integer, Integer> values = new LinkedHashMap<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT skill_id, minimum_proficiency FROM company_requirements WHERE company_id = ? AND required = TRUE")) {
            statement.setInt(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) values.put(resultSet.getInt(1), resultSet.getInt(2)); }
        }
        return values;
    }

    public List<Skill> findMissingForCompany(int studentId, int companyId) throws SQLException {
        String sql = "SELECT s.id, s.name, s.category, s.description FROM company_requirements cr JOIN skills s ON s.id = cr.skill_id LEFT JOIN student_skills ss ON ss.skill_id = cr.skill_id AND ss.student_id = ? WHERE cr.company_id = ? AND cr.required = TRUE AND (ss.proficiency_level IS NULL OR ss.proficiency_level < cr.minimum_proficiency) ORDER BY s.name";
        List<Skill> skills = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId); statement.setInt(2, companyId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) skills.add(map(resultSet)); }
        }
        return skills;
    }

    public List<Skill> findAll() throws SQLException {
        List<Skill> skills = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, category, description FROM skills ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) skills.add(map(resultSet));
        }
        return skills;
    }

    public boolean update(Skill skill) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE skills SET name = ?, category = ?, description = ? WHERE id = ?")) {
            statement.setString(1, skill.getName()); statement.setString(2, skill.getCategory()); statement.setString(3, skill.getDescription()); statement.setInt(4, skill.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM skills WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private Skill map(ResultSet resultSet) throws SQLException {
        return new Skill(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("category"), resultSet.getString("description"));
    }
}
