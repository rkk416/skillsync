package skillsync.auth;

import skillsync.model.User;
import skillsync.service.AuthService;
import skillsync.service.AuthServiceImpl;
import skillsync.utils.ControllerSupport;
import skillsync.utils.NavigationManager;
import skillsync.utils.ViewFactory;

public final class RegisterController extends ControllerSupport {
    private final AuthService authService;
    public RegisterController() { this(new AuthServiceImpl()); }
    public RegisterController(AuthService authService) { this.authService = authService; }
    public void register(String username, String email, String password) {
        execute(() -> { authService.registerUser(new User(0, email, password, username, "STUDENT", null)); ViewFactory.info("Registration successful. Please sign in."); NavigationManager.getInstance().navigateTo("login"); });
    }
    public void showLogin() { NavigationManager.getInstance().navigateTo("login"); }
}
