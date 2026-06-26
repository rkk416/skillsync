package skillsync.repository;

import skillsync.model.PlacementApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlacementApplicationRepository extends BaseRepository {
    public PlacementApplication create(PlacementApplication application) throws SQLException {
        String sql = """
                INSERT INTO placement_applications (student_id, company_id, status, placement_score, applied_at)
                VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))
                RETURNING id, student_id, company_id, status, placement_score, applied_at, created_at, updated_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, application.getStudentId());
            statement.setInt(2, application.getCompanyId());
            statement.setString(3, application.getStatus() == null ? "APPLIED" : application.getStatus());
            statement.setBigDecimal(4, application.getPlacementScore());
            statement.setTimestamp(5, toTimestamp(application.getAppliedAt()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return application;
        }
    }

    public Optional<PlacementApplication> findById(int id) throws SQLException {
        String sql = "SELECT id, student_id, company_id, status, placement_score, applied_at, created_at, updated_at FROM placement_applications WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<PlacementApplication> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT id, student_id, company_id, status, placement_score, applied_at, created_at, updated_at FROM placement_applications WHERE student_id = ? ORDER BY applied_at DESC";
        List<PlacementApplication> applications = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) applications.add(map(resultSet)); }
        }
        return applications;
    }

    public boolean update(PlacementApplication application) throws SQLException {
        String sql = "UPDATE placement_applications SET student_id = ?, company_id = ?, status = ?, placement_score = ?, applied_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, application.getStudentId());
            statement.setInt(2, application.getCompanyId());
            statement.setString(3, application.getStatus());
            statement.setBigDecimal(4, application.getPlacementScore());
            statement.setTimestamp(5, toTimestamp(application.getAppliedAt()));
            statement.setInt(6, application.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM placement_applications WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private PlacementApplication map(ResultSet resultSet) throws SQLException {
        return new PlacementApplication(resultSet.getInt("id"), resultSet.getInt("student_id"), resultSet.getInt("company_id"),
                resultSet.getString("status"), resultSet.getBigDecimal("placement_score"),
                resultSet.getTimestamp("applied_at").toLocalDateTime(), resultSet.getTimestamp("created_at").toLocalDateTime(),
                resultSet.getTimestamp("updated_at").toLocalDateTime());
    }

    private Timestamp toTimestamp(java.time.LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
