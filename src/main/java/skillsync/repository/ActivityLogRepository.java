package skillsync.repository;

import skillsync.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActivityLogRepository extends BaseRepository {
    public ActivityLog create(ActivityLog log) throws SQLException {
        String sql = """
                INSERT INTO activity_logs (student_id, activity_type, description)
                VALUES (?, ?, ?)
                RETURNING id, student_id, activity_type, description, created_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, log.getStudentId());
            statement.setString(2, log.getActivityType());
            statement.setString(3, log.getDescription());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return log;
        }
    }

    public Optional<ActivityLog> findById(int id) throws SQLException {
        String sql = "SELECT id, student_id, activity_type, description, created_at FROM activity_logs WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<ActivityLog> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT id, student_id, activity_type, description, created_at FROM activity_logs WHERE student_id = ? ORDER BY created_at DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) logs.add(map(resultSet)); }
        }
        return logs;
    }

    private ActivityLog map(ResultSet resultSet) throws SQLException {
        return new ActivityLog(resultSet.getInt("id"), resultSet.getInt("student_id"), resultSet.getString("activity_type"),
                resultSet.getString("description"), resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
