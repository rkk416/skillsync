package skillsync.repository;

import skillsync.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentRepository extends BaseRepository {
    public Student create(Student student) throws SQLException {
        String sql = "INSERT INTO students (user_id, university, degree, graduation_year, bio) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getUniversity());
            statement.setString(3, student.getDegree());
            if (student.getGraduationYear() == 0) statement.setNull(4, Types.INTEGER); else statement.setInt(4, student.getGraduationYear());
            statement.setString(5, student.getBio());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) student.setId(keys.getInt(1)); }
            return student;
        }
    }

    public Optional<Student> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, university, degree, graduation_year, bio FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, university, degree, graduation_year, bio FROM students ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) students.add(map(resultSet));
        }
        return students;
    }

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET user_id = ?, university = ?, degree = ?, graduation_year = ?, bio = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getUniversity());
            statement.setString(3, student.getDegree());
            if (student.getGraduationYear() == 0) statement.setNull(4, Types.INTEGER); else statement.setInt(4, student.getGraduationYear());
            statement.setString(5, student.getBio());
            statement.setInt(6, student.getId());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private Student map(ResultSet resultSet) throws SQLException {
        return new Student(resultSet.getInt("id"), resultSet.getInt("user_id"), resultSet.getString("university"),
                resultSet.getString("degree"), resultSet.getInt("graduation_year"), resultSet.getString("bio"));
    }
}
