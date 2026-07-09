package skillsync.service;

import skillsync.model.User;
import skillsync.repository.UserRepository;
import skillsync.repository.StudentRepository;
import skillsync.model.Student;
import skillsync.utils.PasswordHasher;
import skillsync.utils.SessionManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class AuthServiceImpl implements AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AuthServiceImpl() {
        this(new UserRepository(), new StudentRepository());
    }

    public AuthServiceImpl(UserRepository userRepository) {
        this(userRepository, new StudentRepository());
    }

    public AuthServiceImpl(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.studentRepository = Objects.requireNonNull(studentRepository);
    }

    @Override
    public User registerUser(User user) {
        Objects.requireNonNull(user, "User is required");
        String email = normalizeEmail(user.getEmail());
        String password = user.getPasswordHash();
        if (user.getFullName() == null || user.getFullName().isBlank()) throw new IllegalArgumentException("Username is required");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Password must contain at least 8 characters");
        try {
            if (userRepository.findByEmail(email).isPresent()) throw new IllegalArgumentException("An account already exists for this email");
            char[] characters = password.toCharArray();
            try { user.setPasswordHash(PasswordHasher.hash(characters)); } finally { Arrays.fill(characters, '\0'); }
            user.setEmail(email);
            if (user.getRole() == null || user.getRole().isBlank()) user.setRole("STUDENT");
            User created = userRepository.create(user);
            studentRepository.create(new Student(0, created.getId(), null, null, 0, null));
            return created;
        } catch (SQLException exception) {
            throw new ServiceException("Unable to register the user", exception);
        }
    }

    @Override
    public User loginUser(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password is required");
        try {
            User user = userRepository.findByEmail(normalizedEmail).orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
            char[] characters = password.toCharArray();
            boolean valid;
            try { valid = PasswordHasher.matches(characters, user.getPasswordHash()); } finally { Arrays.fill(characters, '\0'); }
            if (!valid) throw new IllegalArgumentException("Invalid email or password");
            // Ensure student profile exists for this user (safety check for data consistency)
            if (studentRepository.findByUserId(user.getId()).isEmpty()) {
                studentRepository.create(new Student(0, user.getId(), null, null, 0, null));
            }
            SessionManager.start(user);
            return user;
        } catch (SQLException exception) {
            throw new ServiceException("Unable to sign in", exception);
        }
    }

    @Override
    public void logoutUser() {
        SessionManager.clear();
    }

    private String normalizeEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) throw new IllegalArgumentException("A valid email address is required");
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
