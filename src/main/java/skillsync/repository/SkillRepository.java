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
