package skillsync.repository;

import skillsync.model.Company;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompanyRepository extends BaseRepository {
    public Company create(Company company) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO companies (name, industry, website, minimum_gpa) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            bind(statement, company); statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) company.setId(keys.getInt(1)); }
            return company;
        }
    }

    public Optional<Company> findById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, industry, website, minimum_gpa FROM companies WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) { return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty(); }
        }
    }

    public List<Company> findAll() throws SQLException {
        List<Company> companies = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id, name, industry, website, minimum_gpa FROM companies ORDER BY id"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) companies.add(map(resultSet));
        }
        return companies;
    }

    public boolean update(Company company) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("UPDATE companies SET name = ?, industry = ?, website = ?, minimum_gpa = ? WHERE id = ?")) {
            bind(statement, company); statement.setInt(5, company.getId()); return statement.executeUpdate() == 1;
        }
    }

    public boolean deleteById(int id) throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("DELETE FROM companies WHERE id = ?")) {
            statement.setInt(1, id); return statement.executeUpdate() == 1;
        }
    }

    private void bind(PreparedStatement statement, Company value) throws SQLException {
        statement.setString(1, value.getName()); statement.setString(2, value.getIndustry()); statement.setString(3, value.getWebsite()); statement.setBigDecimal(4, value.getMinimumGpa());
    }

    private Company map(ResultSet resultSet) throws SQLException {
        return new Company(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("industry"), resultSet.getString("website"), resultSet.getBigDecimal("minimum_gpa"));
    }

    public long count() throws SQLException {
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM companies"); ResultSet resultSet = statement.executeQuery()) {
            resultSet.next(); return resultSet.getLong(1);
        }
    }
}
