package skillsync.repository;

import skillsync.model.Certification;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CertificationRepository extends BaseRepository {
    public Certification create(Certification certification) throws SQLException {
        String sql = "INSERT INTO certifications (student_id, name, issuing_organization, issue_date, expiry_date, credential_url) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, certification); statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) certification.setId(keys.getInt(1)); }
            return certification;
        }
    }

    public Optional<Certification> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, name, issuing_organization, issue_date, expiry_date, credential_url FROM certifications WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Certification> findAll() throws SQLException {
        List<Certification> certifications = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, student_id, name, issuing_organization, issue_date, expiry_date, credential_url FROM certifications ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) certifications.add(map(resultSet));
        }
        return certifications;
    }

    public boolean update(Certification certification) throws SQLException {
        String sql = "UPDATE certifications SET student_id = ?, name = ?, issuing_organization = ?, issue_date = ?, expiry_date = ?, credential_url = ? WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, certification); statement.setInt(7, certification.getId()); return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM certifications WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, Certification value) throws SQLException {
        statement.setInt(1, value.getStudentId()); statement.setString(2, value.getName()); statement.setString(3, value.getIssuingOrganization());
        statement.setObject(4, value.getIssueDate()); statement.setObject(5, value.getExpiryDate()); statement.setString(6, value.getCredentialUrl());
    }

    private Certification map(ResultSet resultSet) throws SQLException {
        Date issueDate = resultSet.getDate("issue_date"); Date expiryDate = resultSet.getDate("expiry_date");
        return new Certification(resultSet.getInt("id"), resultSet.getInt("student_id"), resultSet.getString("name"),
                resultSet.getString("issuing_organization"), issueDate == null ? null : issueDate.toLocalDate(),
                expiryDate == null ? null : expiryDate.toLocalDate(), resultSet.getString("credential_url"));
    }
}
