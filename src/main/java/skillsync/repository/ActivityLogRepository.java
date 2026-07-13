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

    public java.util.Map<String, Integer> getWeeklyActivityTimeline() throws SQLException {
        String sql = "SELECT TO_CHAR(DATE_TRUNC('week', created_at), 'Mon DD') as wk, COUNT(*) as cnt " +
                     "FROM activity_logs WHERE created_at >= CURRENT_DATE - INTERVAL '8 weeks' " +
                     "GROUP BY DATE_TRUNC('week', created_at) ORDER BY DATE_TRUNC('week', created_at)";
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString("wk"), rs.getInt("cnt"));
        }
        return result;
    }

    public java.util.Map<String, Integer> getDailyActivityThisWeek() throws SQLException {
        String sql = "SELECT TO_CHAR(created_at, 'Dy') as day, COUNT(*) as cnt " +
                     "FROM activity_logs WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE) " +
                     "GROUP BY TO_CHAR(created_at, 'Dy'), EXTRACT(DOW FROM created_at) " +
                     "ORDER BY EXTRACT(DOW FROM created_at)";
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.put(rs.getString("day"), rs.getInt("cnt"));
        }
        return result;
    }

    public long countThisWeek() throws SQLException {
        String sql = "SELECT COUNT(*) FROM activity_logs WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        }
    }
}

