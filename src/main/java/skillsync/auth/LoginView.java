package skillsync.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import skillsync.utils.ViewFactory;

public final class LoginView extends VBox {
    public LoginView() {
        LoginController controller = new LoginController();
        setAlignment(Pos.CENTER); setSpacing(14); setPadding(new Insets(40)); setStyle("-fx-background-color: #F8FAFC; -fx-font-family: 'Segoe UI';");
        Label title = ViewFactory.title("Welcome to SkillSync");
        TextField email = new TextField(); email.setPromptText("Email"); email.setMaxWidth(360);
        PasswordField password = new PasswordField(); password.setPromptText("Password"); password.setMaxWidth(360);
        Button login = ViewFactory.primaryButton("Login"); login.setDefaultButton(true); login.setOnAction(event -> controller.login(email.getText(), password.getText()));
        Hyperlink register = new Hyperlink("Create an account"); register.setOnAction(event -> controller.showRegister());
        getChildren().addAll(title, email, password, login, register);
    }
}
