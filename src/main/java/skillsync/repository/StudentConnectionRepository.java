package skillsync.repository;

import skillsync.model.StudentConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentConnectionRepository extends BaseRepository {
    public StudentConnection create(StudentConnection connectionValue) throws SQLException {
        int studentId = orderedFirst(connectionValue.getStudentId(), connectionValue.getConnectedStudentId());
        int connectedStudentId = orderedSecond(connectionValue.getStudentId(), connectionValue.getConnectedStudentId());
        String sql = """
                INSERT INTO student_connections (student_id, connected_student_id, status)
                VALUES (?, ?, ?)
                ON CONFLICT (student_id, connected_student_id) DO UPDATE SET status = EXCLUDED.status
                RETURNING student_id, connected_student_id, status, created_at
                """;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, connectedStudentId);
            statement.setString(3, connectionValue.getStatus() == null ? "PENDING" : connectionValue.getStatus());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return map(resultSet);
            }
            return connectionValue;
        }
    }

    public Optional<StudentConnection> find(int studentId, int connectedStudentId) throws SQLException {
        String sql = "SELECT student_id, connected_student_id, status, created_at FROM student_connections WHERE student_id = ? AND connected_student_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderedFirst(studentId, connectedStudentId));
            statement.setInt(2, orderedSecond(studentId, connectedStudentId));
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<StudentConnection> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT student_id, connected_student_id, status, created_at FROM student_connections WHERE student_id = ? OR connected_student_id = ? ORDER BY created_at DESC";
        List<StudentConnection> connections = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            statement.setInt(2, studentId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) connections.add(map(resultSet)); }
        }
        return connections;
    }

    public boolean updateStatus(int studentId, int connectedStudentId, String status) throws SQLException {
        String sql = "UPDATE student_connections SET status = ? WHERE student_id = ? AND connected_student_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, orderedFirst(studentId, connectedStudentId));
            statement.setInt(3, orderedSecond(studentId, connectedStudentId));
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(int studentId, int connectedStudentId) throws SQLException {
        String sql = "DELETE FROM student_connections WHERE student_id = ? AND connected_student_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderedFirst(studentId, connectedStudentId));
            statement.setInt(2, orderedSecond(studentId, connectedStudentId));
            return statement.executeUpdate() == 1;
        }
    }

    private StudentConnection map(ResultSet resultSet) throws SQLException {
        return new StudentConnection(resultSet.getInt("student_id"), resultSet.getInt("connected_student_id"),
                resultSet.getString("status"), resultSet.getTimestamp("created_at").toLocalDateTime());
    }

    private int orderedFirst(int first, int second) {
        return Math.min(first, second);
    }

    private int orderedSecond(int first, int second) {
        return Math.max(first, second);
    }
}
