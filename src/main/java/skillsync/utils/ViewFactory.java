package skillsync.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public final class ViewFactory {
    private static final String BACKGROUND = "#F8FAFC";
    private static final String BORDER = "#E5E7EB";
    private static final String TEXT = "#111827";
    private static final String MUTED_TEXT = "#6B7280";
    private static final String NAV_TEXT = "#374151";
    private static final String NAV_HOVER = "#EEF4FF";
    private static final String SURFACE = UIConstants.SECONDARY_COLOR;
    private static final String RADIUS = "8";

    private ViewFactory() { }

    public static Label title(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");
        return label;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + UIConstants.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-background-radius: " + RADIUS + "; -fx-padding: 10 18;");
        return button;
    }

    public static VBox metricCard(String labelText, Number value) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: " + MUTED_TEXT + ";");
        Label metric = new Label(String.valueOf(value));
        metric.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + UIConstants.PRIMARY_COLOR + ";");
        VBox card = new VBox(10, label, metric);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefSize(210, 130);
        card.setStyle(surfaceStyle());
        return card;
    }

    public static VBox emptyState(String title, String detail) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + TEXT + ";");
        Label detailLabel = new Label(detail);
        detailLabel.setWrapText(true);
        detailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED_TEXT + ";");
        VBox emptyState = new VBox(8, titleLabel, detailLabel);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(28));
        emptyState.setStyle(surfaceStyle());
        return emptyState;
    }

    public static void prepareTable(TableView<?> table, String emptyMessage) {
        table.setPlaceholder(emptyState("No records found", emptyMessage));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(surfaceStyle());
    }

    public static BorderPane shell(String title, Node content) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: " + BACKGROUND + "; -fx-font-family: '" + UIConstants.FONT_FAMILY + "';");
        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 1 0 0;");
        Label brand = new Label("SkillSync");
        brand.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + UIConstants.PRIMARY_COLOR + "; -fx-padding: 0 0 25 0;");
        sidebar.getChildren().add(brand);
        addNav(sidebar, "Dashboard", "dashboard");
        addNav(sidebar, "Profile", "profile");
        addNav(sidebar, "Placement", "placement");
        addNav(sidebar, "Collaboration", "collaboration");
        addNav(sidebar, "Recommendations", "recommendations");
        addNav(sidebar, "Analytics", "analytics");
        VBox center = new VBox(20, title(title), content);
        center.setPadding(new Insets(28));
        pane.setLeft(sidebar);
        pane.setCenter(center);
        return pane;
    }

    public static void error(String message) { alert(Alert.AlertType.ERROR, "Error", message); }
    public static void info(String message) { alert(Alert.AlertType.INFORMATION, "SkillSync", message); }
    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm"); alert.setHeaderText(null); alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    private static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static String surfaceStyle() {
        return "-fx-background-color: " + SURFACE
                + "; -fx-background-radius: " + RADIUS
                + "; -fx-border-color: " + BORDER
                + "; -fx-border-radius: " + RADIUS + ";";
    }

    private static void addNav(VBox sidebar, String label, String screen) {
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(42);
        button.setStyle(navStyle("transparent", NAV_TEXT, "600"));
        button.setOnMouseEntered(e -> button.setStyle(navStyle(NAV_HOVER, UIConstants.PRIMARY_COLOR, "700")));
        button.setOnMouseExited(e -> button.setStyle(navStyle("transparent", NAV_TEXT, "600")));
        button.setOnAction(event -> NavigationManager.getInstance().navigateTo(screen));
        sidebar.getChildren().add(button);
    }

    private static String navStyle(String background, String textColor, String weight) {
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-font-size: 15px;"
                + "-fx-font-weight: " + weight + ";"
                + "-fx-background-radius: " + RADIUS + ";"
                + "-fx-cursor: hand;"
                + "-fx-padding: 12 16;"
                + "-fx-alignment: CENTER_LEFT;";
    }
}
