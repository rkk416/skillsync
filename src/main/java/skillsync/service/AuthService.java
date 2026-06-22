package skillsync.service;

import skillsync.model.User;

public interface AuthService {
    User registerUser(User user);
    User loginUser(String email, String password);
    void logoutUser();
}
