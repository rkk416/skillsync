package skillsync.repository;

import skillsync.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

/** Base class for repositories that use the application's shared connection pool. */
public abstract class BaseRepository {
    private final DatabaseConnection databaseConnection;

    protected BaseRepository() {
        this.databaseConnection = null;
    }

    protected BaseRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    protected final Connection getConnection() throws SQLException {
        return (databaseConnection == null ? DatabaseConnection.getInstance() : databaseConnection).getConnection();
    }
}
