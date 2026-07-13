package skillsync.service;

import org.junit.jupiter.api.Test;
import skillsync.model.Student;
import skillsync.model.User;
import skillsync.repository.StudentRepository;
import skillsync.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceImplTest {
    @Test void registersAndAuthenticatesAUser() {
        FakeUsers users = new FakeUsers(); AuthService service = new AuthServiceImpl(users, new FakeStudents());
        User registered = service.registerUser(new User(0, "Student@Example.com", "secure-pass", "Student", "STUDENT", null));
        assertEquals("student@example.com", registered.getEmail()); assertNotEquals("secure-pass", registered.getPasswordHash());
        assertEquals(registered, service.loginUser("student@example.com", "secure-pass"));
        assertThrows(IllegalArgumentException.class, () -> service.loginUser("student@example.com", "wrong-pass"));
    }

    private static final class FakeUsers extends UserRepository {
        private User stored;
        @Override public Optional<User> findByEmail(String email) { return Optional.ofNullable(stored).filter(user -> user.getEmail().equalsIgnoreCase(email)); }
        @Override public User create(User user) { user.setId(1); stored = user; return user; }
    }
    private static final class FakeStudents extends StudentRepository {
        private Student stored;
        @Override public Student create(Student student) throws SQLException { student.setId(1); stored = student; return student; }
        @Override public Optional<Student> findByUserId(int userId) { return Optional.ofNullable(stored).filter(student -> student.getUserId() == userId); }
    }
}
