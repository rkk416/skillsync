package skillsync.repository;

import skillsync.model.TeamJoinRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamJoinRequestRepository extends BaseRepository {
    public TeamJoinRequest create(TeamJoinRequest request) throws SQLException {
        String sql = """
                INSERT INTO team_join_requests (team_id, student_id, status, requested_at)
                VALUES (?, ?, ?, COALESCE(?, CURRENT_TIMESTAMP))
                RETURNING id, team_id, student_id, status, requested_at, created_at, updated_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, request.getTeamId());
            statement.setInt(2, request.getStudentId());
            statement.setString(3, request.getStatus() == null ? "PENDING" : request.getStatus());
            statement.setTimestamp(4, toTimestamp(request.getRequestedAt()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return request;
        }
    }

    public Optional<TeamJoinRequest> findById(int id) throws SQLException {
        String sql = "SELECT id, team_id, student_id, status, requested_at, created_at, updated_at FROM team_join_requests WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<TeamJoinRequest> findByTeamId(int teamId) throws SQLException {
        String sql = "SELECT id, team_id, student_id, status, requested_at, created_at, updated_at FROM team_join_requests WHERE team_id = ? ORDER BY requested_at DESC";
        List<TeamJoinRequest> requests = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, teamId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) requests.add(map(resultSet)); }
        }
        return requests;
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE team_join_requests SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM team_join_requests WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private TeamJoinRequest map(ResultSet resultSet) throws SQLException {
        return new TeamJoinRequest(resultSet.getInt("id"), resultSet.getInt("team_id"), resultSet.getInt("student_id"),
                resultSet.getString("status"), resultSet.getTimestamp("requested_at").toLocalDateTime(),
                resultSet.getTimestamp("created_at").toLocalDateTime(), resultSet.getTimestamp("updated_at").toLocalDateTime());
    }

    private Timestamp toTimestamp(java.time.LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
