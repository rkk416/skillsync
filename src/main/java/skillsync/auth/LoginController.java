package skillsync.auth;

import skillsync.service.AuthService;
import skillsync.service.AuthServiceImpl;
import skillsync.utils.ControllerSupport;
import skillsync.utils.NavigationManager;

public final class LoginController extends ControllerSupport {
    private final AuthService authService;
    public LoginController() { this(new AuthServiceImpl()); }
    public LoginController(AuthService authService) { this.authService = authService; }
    public void login(String email, String password) { execute(() -> { authService.loginUser(email, password); NavigationManager.getInstance().navigateTo("dashboard"); }); }
    public void showRegister() { NavigationManager.getInstance().navigateTo("register"); }
}
