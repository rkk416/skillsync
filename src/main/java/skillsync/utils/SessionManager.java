package skillsync.utils;

import skillsync.model.User;

import java.util.Optional;

public final class SessionManager {
    private static volatile User currentUser;

    private SessionManager() {
    }

    public static Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static void start(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
