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
    // Base columns that exist in both old and new schema
    private static final String COLUMNS_BASE = "id, user_id, university, degree, graduation_year, bio";
    // Full columns (production schema with profile fields)
    private static final String COLUMNS_FULL = "id, user_id, university, degree, graduation_year, bio, full_name, branch, cgpa";

    public Student create(Student student) throws SQLException {
        String sql = "INSERT INTO students (user_id, university, degree, graduation_year, bio, full_name, branch, cgpa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getUniversity());
            statement.setString(3, student.getDegree());
            if (student.getGraduationYear() == 0) statement.setNull(4, Types.INTEGER); else statement.setInt(4, student.getGraduationYear());
            statement.setString(5, student.getBio());
            statement.setString(6, student.getFullName());
            statement.setString(7, student.getBranch());
            if (student.getCgpa() == null) statement.setNull(8, Types.NUMERIC); else statement.setDouble(8, student.getCgpa());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) student.setId(keys.getInt(1)); }
            return student;
        } catch (SQLException e) {
            // Fallback for databases that don't have the new columns yet
            if (e.getMessage().contains("full_name")) {
                String fallbackSql = "INSERT INTO students (user_id, university, degree, graduation_year, bio) VALUES (?, ?, ?, ?, ?)";
                try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
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
            throw e;
        }
    }

    public Optional<Student> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_FULL + " FROM students WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        } catch (SQLException e) {
            if (e.getMessage().contains("full_name")) {
                try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_BASE + " FROM students WHERE id = ?")) {
                    statement.setInt(1, id);
                    try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(mapBase(resultSet)) : Optional.empty(); }
                }
            }
            throw e;
        }
    }

    public Optional<Student> findByUserId(int userId) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_FULL + " FROM students WHERE user_id = ?")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        } catch (SQLException e) {
            if (e.getMessage().contains("full_name")) {
                try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_BASE + " FROM students WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(mapBase(resultSet)) : Optional.empty(); }
                }
            }
            throw e;
        }
    }

    public List<Student> findAll() throws SQLException {
        List<Student> students = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_FULL + " FROM students ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) students.add(map(resultSet));
        } catch (SQLException e) {
            if (e.getMessage().contains("full_name")) {
                try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT " + COLUMNS_BASE + " FROM students ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) students.add(mapBase(resultSet));
                }
            } else {
                throw e;
            }
        }
        return students;
    }

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET user_id = ?, university = ?, degree = ?, graduation_year = ?, bio = ?, "
                + "full_name = ?, branch = ?, cgpa = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, student.getUserId());
            statement.setString(2, student.getUniversity());
            statement.setString(3, student.getDegree());
            if (student.getGraduationYear() == 0) statement.setNull(4, Types.INTEGER); else statement.setInt(4, student.getGraduationYear());
            statement.setString(5, student.getBio());
            statement.setString(6, student.getFullName());
            statement.setString(7, student.getBranch());
            if (student.getCgpa() == null) statement.setNull(8, Types.NUMERIC); else statement.setDouble(8, student.getCgpa());
            statement.setInt(9, student.getId());
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
        Double cgpa = resultSet.getObject("cgpa") == null ? null : resultSet.getDouble("cgpa");
        return new Student(resultSet.getInt("id"), resultSet.getInt("user_id"), resultSet.getString("university"),
                resultSet.getString("degree"), resultSet.getInt("graduation_year"), resultSet.getString("bio"),
                resultSet.getString("full_name"), resultSet.getString("branch"), cgpa);
    }

    private Student mapBase(ResultSet resultSet) throws SQLException {
        return new Student(resultSet.getInt("id"), resultSet.getInt("user_id"), resultSet.getString("university"),
                resultSet.getString("degree"), resultSet.getInt("graduation_year"), resultSet.getString("bio"),
                null, null, null);
    }

    public long count() throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM students"); ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        }
    }

    public long countEligiblePairs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students s CROSS JOIN companies c " +
                     "WHERE NOT EXISTS ( " +
                     "  SELECT 1 FROM company_requirements cr " +
                     "  LEFT JOIN student_skills ss ON ss.skill_id = cr.skill_id AND ss.student_id = s.id " +
                     "  WHERE cr.company_id = c.id " +
                     "    AND (ss.proficiency_level IS NULL OR ss.proficiency_level < cr.minimum_proficiency) " +
                     ")";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            rs.next(); return rs.getLong(1);
        }
    }
}