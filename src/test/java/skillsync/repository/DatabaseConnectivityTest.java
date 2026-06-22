package skillsync.repository;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import skillsync.database.DatabaseConnection;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectivityTest {
    @Test void connectsWhenIntegrationConfigurationIsPresent() throws Exception {
        Assumptions.assumeTrue(getClass().getClassLoader().getResource("application.properties") != null,
                "Copy application.properties.example to application.properties to run database integration tests");
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            assertTrue(connection.isValid(5));
        }
    }
}
