package skillsync.repository;

import skillsync.model.RecommendationHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecommendationHistoryRepository extends BaseRepository {
    public RecommendationHistory create(RecommendationHistory history) throws SQLException {
        String sql = """
                INSERT INTO recommendation_history (student_id, recommendation_type, target_id, score, algorithm_version, generated_at)
                VALUES (?, ?, ?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))
                RETURNING id, student_id, recommendation_type, target_id, score, algorithm_version, generated_at, created_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, history.getStudentId());
            statement.setString(2, history.getRecommendationType());
            statement.setInt(3, history.getTargetId());
            statement.setBigDecimal(4, history.getScore());
            statement.setString(5, history.getAlgorithmVersion());
            statement.setTimestamp(6, toTimestamp(history.getGeneratedAt()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return history;
        }
    }

    public Optional<RecommendationHistory> findById(int id) throws SQLException {
        String sql = "SELECT id, student_id, recommendation_type, target_id, score, algorithm_version, generated_at, created_at FROM recommendation_history WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<RecommendationHistory> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT id, student_id, recommendation_type, target_id, score, algorithm_version, generated_at, created_at FROM recommendation_history WHERE student_id = ? ORDER BY generated_at DESC";
        List<RecommendationHistory> history = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) history.add(map(resultSet)); }
        }
        return history;
    }

    private RecommendationHistory map(ResultSet resultSet) throws SQLException {
        return new RecommendationHistory(resultSet.getInt("id"), resultSet.getInt("student_id"),
                resultSet.getString("recommendation_type"), resultSet.getInt("target_id"), resultSet.getBigDecimal("score"),
                resultSet.getString("algorithm_version"), resultSet.getTimestamp("generated_at").toLocalDateTime(),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private Timestamp toTimestamp(java.time.LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
