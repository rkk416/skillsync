package skillsync.repository;

import skillsync.model.Project;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectRepository extends BaseRepository {
    public Project create(Project project) throws SQLException {
        String sql = "INSERT INTO projects (owner_student_id, name, description, repository_url, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, project); statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) project.setId(keys.getInt(1)); }
            return project;
        }
    }

    public Optional<Project> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, owner_student_id, name, description, repository_url, start_date, end_date FROM projects WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, owner_student_id, name, description, repository_url, start_date, end_date FROM projects ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) projects.add(map(resultSet));
        }
        return projects;
    }

    public boolean update(Project project) throws SQLException {
        String sql = "UPDATE projects SET owner_student_id = ?, name = ?, description = ?, repository_url = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, project); statement.setInt(7, project.getId()); return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM projects WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, Project value) throws SQLException {
        statement.setInt(1, value.getOwnerStudentId()); statement.setString(2, value.getName()); statement.setString(3, value.getDescription());
        statement.setString(4, value.getRepositoryUrl()); statement.setObject(5, value.getStartDate()); statement.setObject(6, value.getEndDate());
    }

    private Project map(ResultSet resultSet) throws SQLException {
        Date startDate = resultSet.getDate("start_date"); Date endDate = resultSet.getDate("end_date");
        return new Project(resultSet.getInt("id"), resultSet.getInt("owner_student_id"), resultSet.getString("name"),
                resultSet.getString("description"), resultSet.getString("repository_url"),
                startDate == null ? null : startDate.toLocalDate(), endDate == null ? null : endDate.toLocalDate());
    }
}
