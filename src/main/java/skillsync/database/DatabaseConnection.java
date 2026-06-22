package skillsync.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/** Thread-safe holder for the application's PostgreSQL connection pool. */
public final class DatabaseConnection implements AutoCloseable {
    private static final String CONFIG_FILE = "application.properties";

    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        Properties properties = loadProperties();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(requiredProperty(properties, "DB_URL"));
        config.setUsername(requiredProperty(properties, "DB_USER"));
        config.setPassword(requiredProperty(properties, "DB_PASSWORD"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("SkillSyncPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        this.dataSource = new HikariDataSource(config);
    }

    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                throw new IllegalStateException(CONFIG_FILE + " was not found on the classpath. Copy application.properties.example and configure it.");
            }
            properties.load(stream);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + CONFIG_FILE, exception);
        }
    }

    private static String requiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required database property is missing: " + key);
        }
        return value.trim();
    }
}
