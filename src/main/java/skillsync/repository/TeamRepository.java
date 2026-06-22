package skillsync.repository;

import skillsync.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamRepository extends BaseRepository {
    public Team create(Team team) throws SQLException {
        String sql = "INSERT INTO teams (name, description, created_by) VALUES (?, ?, ?) RETURNING id, created_at";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, team.getName()); statement.setString(2, team.getDescription()); statement.setInt(3, team.getCreatedBy());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) { team.setId(resultSet.getInt("id")); team.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime()); }
            }
            return team;
        }
    }

    public Optional<Team> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, description, created_by, created_at FROM teams WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Team> findAll() throws SQLException {
        List<Team> teams = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, description, created_by, created_at FROM teams ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) teams.add(map(resultSet));
        }
        return teams;
    }

    public boolean update(Team team) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE teams SET name = ?, description = ?, created_by = ?, created_at = ? WHERE id = ?")) {
            statement.setString(1, team.getName()); statement.setString(2, team.getDescription()); statement.setInt(3, team.getCreatedBy());
            statement.setTimestamp(4, team.getCreatedAt() == null ? null : Timestamp.valueOf(team.getCreatedAt())); statement.setInt(5, team.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM teams WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private Team map(ResultSet resultSet) throws SQLException {
        return new Team(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("description"),
                resultSet.getInt("created_by"), resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
