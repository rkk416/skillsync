package skillsync.auth;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class LoginView extends VBox {
    private static final String ACCENT = "#1D6FE0";
    private static final String ACCENT_TINT = "#E6F1FB";
    private static final String TEXT_PRIMARY = "#0C2A47";
    private static final String TEXT_SECONDARY = "#5B6B7D";
    private static final String FIELD_STYLE =
            "-fx-background-color: #F8FBFF; -fx-background-radius: 8; -fx-border-radius: 8; "
                    + "-fx-border-color: #DCE7F5; -fx-border-width: 1; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-padding: 10 12;";
    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-background-radius: 9; -fx-padding: 12 0; -fx-font-weight: bold; -fx-font-size: 14px;";

    public LoginView() {
        LoginController controller = new LoginController();
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #F0F7FF;");

        StackPane badge = new StackPane(new Label("\u26A1"));
        badge.setPrefSize(52, 52);
        badge.setMaxSize(52, 52);
        badge.setStyle("-fx-background-color: " + ACCENT_TINT + "; -fx-background-radius: 14;");

        Label brand = new Label("SkillSync");
        brand.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");

        Label subtitle = new Label("Welcome back — sign in to continue");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_SECONDARY + ";");

        VBox header = new VBox(8, badge, brand, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 24, 0));

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        TextField email = new TextField();
        email.setPromptText("name@college.edu");
        email.setPrefWidth(320);
        email.setPrefHeight(38);
        email.setStyle(FIELD_STYLE);

        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        PasswordField password = new PasswordField();
        password.setPromptText("Enter your password");
        password.setPrefWidth(320);
        password.setPrefHeight(38);
        password.setStyle(FIELD_STYLE);

        Button login = new Button("Sign in");
        login.setDefaultButton(true);
        login.setPrefWidth(320);
        login.setStyle(PRIMARY_BUTTON_STYLE);
        login.setOnAction(event -> controller.login(email.getText(), password.getText()));

        Hyperlink register = new Hyperlink("New to SkillSync? Create an account");
        register.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-size: 12px;");
        register.setOnAction(event -> controller.showRegister());

        VBox form = new VBox(6, emailLabel, email, passwordLabel, password);
        form.setPadding(new Insets(0, 0, 20, 0));

        VBox card = new VBox(0, header, form, login, register);
        card.setSpacing(10);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(36, 32, 32, 32));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;");
        VBox.setMargin(login, new Insets(4, 0, 4, 0));

        getChildren().add(card);
    }
}