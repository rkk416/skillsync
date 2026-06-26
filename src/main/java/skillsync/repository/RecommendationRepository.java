package skillsync.repository;

import skillsync.model.Recommendation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecommendationRepository extends BaseRepository {
    public Recommendation create(Recommendation recommendation) throws SQLException {
        String sql = "INSERT INTO recommendations (student_id, recommendation_type, target_id, score, reason) VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recommendation.getStudentId()); statement.setString(2, recommendation.getRecommendationType());
            statement.setInt(3, recommendation.getTargetId()); statement.setBigDecimal(4, recommendation.getScore()); statement.setString(5, recommendation.getReason());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) { recommendation.setId(resultSet.getInt("id")); recommendation.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime()); }
            }
            return recommendation;
        }
    }

    public Optional<Recommendation> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, recommendation_type, target_id, score, reason, created_at FROM recommendations WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Recommendation> findAll() throws SQLException {
        List<Recommendation> recommendations = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, recommendation_type, target_id, score, reason, created_at FROM recommendations ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) recommendations.add(map(resultSet));
        }
        return recommendations;
    }

    public boolean update(Recommendation recommendation) throws SQLException {
        String sql = "UPDATE recommendations SET student_id = ?, recommendation_type = ?, target_id = ?, score = ?, reason = ?, created_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, recommendation.getStudentId()); statement.setString(2, recommendation.getRecommendationType());
            statement.setInt(3, recommendation.getTargetId()); statement.setBigDecimal(4, recommendation.getScore()); statement.setString(5, recommendation.getReason());
            statement.setTimestamp(6, recommendation.getCreatedAt() == null ? null : Timestamp.valueOf(recommendation.getCreatedAt())); statement.setInt(7, recommendation.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM recommendations WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private Recommendation map(ResultSet resultSet) throws SQLException {
        return new Recommendation(resultSet.getInt("id"), resultSet.getInt("student_id"), resultSet.getString("recommendation_type"),
                resultSet.getInt("target_id"), resultSet.getBigDecimal("score"), resultSet.getString("reason"),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }
}
