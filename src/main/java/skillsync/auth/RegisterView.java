package skillsync.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import skillsync.utils.ViewFactory;

public final class RegisterView extends VBox {
    public RegisterView() {
        RegisterController controller = new RegisterController();
        setAlignment(Pos.CENTER); setSpacing(14); setPadding(new Insets(40)); setStyle("-fx-background-color: #F8FAFC; -fx-font-family: 'Segoe UI';");
        TextField username = new TextField(); username.setPromptText("Username"); username.setMaxWidth(360);
        TextField email = new TextField(); email.setPromptText("Email"); email.setMaxWidth(360);
        PasswordField password = new PasswordField(); password.setPromptText("Password (8+ characters)"); password.setMaxWidth(360);
        Button register = ViewFactory.primaryButton("Register"); register.setDefaultButton(true); register.setOnAction(event -> controller.register(username.getText(), email.getText(), password.getText()));
        Hyperlink login = new Hyperlink("Back to login"); login.setOnAction(event -> controller.showLogin());
        getChildren().addAll(ViewFactory.title("Create your account"), username, email, password, register, login);
    }
}
