package skillsync.utils;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
        VBox sidebar = new VBox(12); sidebar.setPadding(new Insets(24)); sidebar.setPrefWidth(220); sidebar.setStyle("-fx-background-color: white;");
        Label brand = new Label("SkillSync"); brand.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2563EB;"); sidebar.getChildren().add(brand);
        addNav(sidebar, "Dashboard", "dashboard"); addNav(sidebar, "Profile", "profile"); addNav(sidebar, "Placement", "placement");
        addNav(sidebar, "Collaboration", "collaboration"); addNav(sidebar, "Recommendations", "recommendations"); addNav(sidebar, "Analytics", "analytics");
        VBox center = new VBox(20, title(title), content); center.setPadding(new Insets(28));
        pane.setLeft(sidebar); pane.setCenter(center); return pane;
    }

    public static void error(String message) { alert(Alert.AlertType.ERROR, "Error", message); }
    public static void info(String message) { alert(Alert.AlertType.INFORMATION, "SkillSync", message); }
    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm"); alert.setHeaderText(null); alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }
    private static void alert(Alert.AlertType type, String title, String message) { Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait(); }
    private static void addNav(VBox sidebar, String label, String screen) { Button button = new Button(label); button.setMaxWidth(Double.MAX_VALUE); button.setStyle("-fx-background-color: transparent; -fx-alignment: CENTER-LEFT; -fx-padding: 10;"); button.setOnAction(event -> NavigationManager.getInstance().navigateTo(screen)); sidebar.getChildren().add(button); }
}