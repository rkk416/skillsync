package skillsync.repository;

import skillsync.model.LoginHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoginHistoryRepository extends BaseRepository {
    public LoginHistory create(LoginHistory history) throws SQLException {
        String sql = """
                INSERT INTO login_history (user_id, login_time, logout_time)
                VALUES (?, COALESCE(?, CURRENT_TIMESTAMP), ?)
                RETURNING id, user_id, login_time, logout_time, created_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, history.getUserId());
            statement.setTimestamp(2, toTimestamp(history.getLoginTime()));
            statement.setTimestamp(3, toTimestamp(history.getLogoutTime()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return history;
        }
    }

    public Optional<LoginHistory> findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, login_time, logout_time, created_at FROM login_history WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<LoginHistory> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, login_time, logout_time, created_at FROM login_history WHERE user_id = ? ORDER BY login_time DESC";
        List<LoginHistory> entries = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) entries.add(map(resultSet)); }
        }
        return entries;
    }

    public boolean markLogout(int id, java.time.LocalDateTime logoutTime) throws SQLException {
        String sql = "UPDATE login_history SET logout_time = COALESCE(?, CURRENT_TIMESTAMP) WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, toTimestamp(logoutTime));
            statement.setInt(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    private LoginHistory map(ResultSet resultSet) throws SQLException {
        Timestamp logoutTime = resultSet.getTimestamp("logout_time");
        return new LoginHistory(resultSet.getInt("id"), resultSet.getInt("user_id"),
                resultSet.getTimestamp("login_time").toLocalDateTime(), logoutTime == null ? null : logoutTime.toLocalDateTime(),
                resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private Timestamp toTimestamp(java.time.LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }
}
