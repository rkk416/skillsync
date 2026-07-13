package skillsync.utils;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class ViewFactory {
    private ViewFactory() { }

    public static Label title(String text) {
        Label label = new Label(text); label.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #111827;"); return label;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text); button.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 18;"); return button;
    }

    public static BorderPane shell(String title, Node content) {
        BorderPane pane = new BorderPane(); pane.setStyle("-fx-background-color: #F8FAFC; -fx-font-family: 'Segoe UI';");
        VBox sidebar = new VBox(12); sidebar.setPadding(new Insets(24)); sidebar.setPrefWidth(220); sidebar.setStyle(
        "-fx-background-color:white;" +
        "-fx-border-color:#E5E7EB;" +
        "-fx-border-width:0 1 0 0;"
);
       Label brand = new Label("🚀 SkillSync");

brand.setStyle(
        "-fx-font-size:28px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#2563EB;" +
        "-fx-padding:0 0 25 0;"
);
        sidebar.getChildren().add(brand);
        addNav(sidebar, "Dashboard", "dashboard"); addNav(sidebar, "Profile", "profile"); addNav(sidebar, "Placement", "placement");
        addNav(sidebar, "Collaboration", "collaboration"); addNav(sidebar, "Recommendations", "recommendations"); addNav(sidebar, "Analytics", "analytics");
        VBox center = new VBox(20, title(title), content); center.setPadding(new Insets(28));
        pane.setLeft(sidebar); pane.setCenter(center); return pane;
    }

    public static void error(String message) { alert(Alert.AlertType.ERROR, "Error", message); }
    public static void info(String message) { alert(Alert.AlertType.INFORMATION, "SkillSync", message); }
    private static void alert(Alert.AlertType type, String title, String message) { Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait(); }
private static void addNav(VBox sidebar, String label, String screen) {

    Button button = new Button(label);

    button.setMaxWidth(Double.MAX_VALUE);

    button.setPrefHeight(42);

    button.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill:#374151;" +
            "-fx-font-size:15px;" +
            "-fx-font-weight:600;" +
            "-fx-background-radius:12;" +
            "-fx-cursor:hand;" +
            "-fx-padding:12 16;" +
            "-fx-alignment:CENTER-LEFT;"
    );

    button.setOnMouseEntered(e ->
            button.setStyle(
                    "-fx-background-color:#EEF4FF;" +
                    "-fx-text-fill:#2563EB;" +
                    "-fx-font-size:15px;" +
                    "-fx-font-weight:700;" +
                    "-fx-background-radius:12;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:12 16;" +
                    "-fx-alignment:CENTER-LEFT;"
            )
    );

    button.setOnMouseExited(e ->
            button.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-text-fill:#374151;" +
                    "-fx-font-size:15px;" +
                    "-fx-font-weight:600;" +
                    "-fx-background-radius:12;" +
                    "-fx-cursor:hand;" +
                    "-fx-padding:12 16;" +
                    "-fx-alignment:CENTER-LEFT;"
            )
    );

    button.setOnAction(event ->
            NavigationManager.getInstance().navigateTo(screen)
    );

    sidebar.getChildren().add(button);
}
}
