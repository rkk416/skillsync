package skillsync.utils;

import skillsync.model.Student;
import skillsync.repository.StudentRepository;
import skillsync.service.ServiceException;

import java.sql.SQLException;

public abstract class ControllerSupport {
    protected int currentStudentId() {
        int userId = SessionManager.getCurrentUser().orElseThrow(() -> new IllegalStateException("No active session")).getId();
        try { return new StudentRepository().findByUserId(userId).map(Student::getId).orElseThrow(() -> new IllegalStateException("Student profile was not found")); }
        catch (SQLException exception) { throw new ServiceException("Unable to load the active student", exception); }
    }

    protected void execute(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException | IllegalStateException | ServiceException exception) {
            exception.printStackTrace();
            ViewFactory.error(exception.getMessage());
        }
    }

    protected boolean executeBoolean(Runnable action) {
        try { action.run(); return true; }
        catch (IllegalArgumentException | IllegalStateException | ServiceException exception) {
            exception.printStackTrace();
            ViewFactory.error(exception.getMessage());
            return false;
        }
    }
}