package skillsync.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class ViewFactory {
    public static final String BACKGROUND = "#F8FAFC";
    public static final String SURFACE = UIConstants.SECONDARY_COLOR;
    public static final String PRIMARY = UIConstants.PRIMARY_COLOR;
    public static final String ACCENT = "#0EA5E9";
    public static final String SUCCESS = "#16A34A";
    public static final String WARNING = "#F59E0B";
    public static final String DANGER = "#DC2626";
    public static final String INFO = "#2563EB";
    public static final String BORDER = "#E5E7EB";
    public static final String TEXT = "#111827";
    public static final String MUTED_TEXT = "#6B7280";
    public static final String SOFT_TEXT = "#94A3B8";

    private static final String NAV_TEXT = "#374151";
    private static final String NAV_HOVER = "#EEF4FF";
    private static final String RADIUS = "8";
    private static final String SHADOW = "dropshadow(gaussian, rgba(15, 23, 42, 0.08), 14, 0, 0, 4)";

    private ViewFactory() { }

    public static Label title(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: " + TEXT + ";");
        return label;
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: " + TEXT + ";");
        return label;
    }

    public static Label caption(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + MUTED_TEXT + ";");
        return label;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(buttonStyle(PRIMARY, "white", PRIMARY));
        return button;
    }

    public static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(buttonStyle(SURFACE, NAV_TEXT, BORDER));
        return button;
    }

    public static Button textButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY + "; -fx-font-weight: 700; -fx-padding: 8 10; -fx-cursor: hand;");
        return button;
    }

    public static TextField searchField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(38);
        field.setStyle("-fx-background-color: " + SURFACE + "; -fx-background-radius: " + RADIUS
                + "; -fx-border-color: " + BORDER + "; -fx-border-radius: " + RADIUS
                + "; -fx-padding: 8 12; -fx-font-size: 13px;");
        return field;
    }

    public static VBox card(Node... children) {
        VBox card = new VBox(12, children);
        card.setPadding(new Insets(18));
        card.setStyle(cardStyle());
        return card;
    }

    public static VBox metricCard(String labelText, Number value) {
        return metricCard(labelText, String.valueOf(value), "Updated now", PRIMARY);
    }

    public static VBox metricCard(String labelText, String value, String detail, String accentColor) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: " + MUTED_TEXT + ";");
        Label metric = new Label(value);
        metric.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: " + accentColor + ";");
        Label detailLabel = caption(detail);
        VBox card = card(label, metric, detailLabel);
        card.setPrefSize(190, 132);
        card.setMinWidth(170);
        return card;
    }

    public static VBox progressCard(String title, double progress, String detail, String color) {
        ProgressBar bar = new ProgressBar(clamp(progress));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(9);
        bar.setStyle("-fx-accent: " + color + ";");
        Label percent = new Label(Math.round(clamp(progress) * 100) + "%");
        percent.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + color + ";");
        HBox header = new HBox(10, sectionTitle(title), spacer(), percent);
        header.setAlignment(Pos.CENTER_LEFT);
        return card(header, bar, caption(detail));
    }

    public static Label badge(String text, String color) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: derive(" + color + ", 92%); -fx-text-fill: " + color
                + "; -fx-background-radius: 999; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: 800;");
        return badge;
    }

    public static Label tag(String text) {
        Label tag = new Label(text);
        tag.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: " + PRIMARY
                + "; -fx-background-radius: 999; -fx-padding: 5 10; -fx-font-size: 11px; -fx-font-weight: 700;");
        return tag;
    }

    public static HBox sectionHeader(String title, String subtitle) {
        VBox text = new VBox(3, sectionTitle(title), caption(subtitle));
        HBox header = new HBox(text, spacer());
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    public static VBox emptyState(String title, String detail) {
        Label mark = new Label("i");
        mark.setAlignment(Pos.CENTER);
        mark.setMinSize(34, 34);
        mark.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 999; -fx-text-fill: " + PRIMARY
                + "; -fx-font-weight: 800;");
        Label titleLabel = sectionTitle(title);
        Label detailLabel = caption(detail);
        VBox emptyState = new VBox(10, mark, titleLabel, detailLabel);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(28));
        emptyState.setStyle(cardStyle());
        return emptyState;
    }

    public static ScrollPane scroll(Node content) {
        ScrollPane pane = new ScrollPane(content);
        pane.setFitToWidth(true);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return pane;
    }

    public static void prepareTable(TableView<?> table, String emptyMessage) {
        table.setPlaceholder(emptyState("No records found", emptyMessage));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(cardStyle());
    }

    public static BorderPane shell(String title, Node content) {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: " + BACKGROUND + "; -fx-font-family: '" + UIConstants.FONT_FAMILY + "';");
        pane.setLeft(sidebar(title));

        VBox center = new VBox(18, topBar(title), content);
        center.setPadding(new Insets(24));
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

    public static Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static VBox sidebar(String activeTitle) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(22));
        sidebar.setPrefWidth(236);
        sidebar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 1 0 0;");
        Label brand = new Label("SkillSync");
        brand.setStyle("-fx-font-size: 27px; -fx-font-weight: 800; -fx-text-fill: " + PRIMARY + "; -fx-padding: 0 0 6 0;");
        Label product = caption("Career intelligence workspace");
        sidebar.getChildren().addAll(brand, product, new Separator());
        addNav(sidebar, "Dashboard", "dashboard", activeTitle);
        addNav(sidebar, "Profile", "profile", activeTitle);
        addNav(sidebar, "Placement", "placement", activeTitle);
        addNav(sidebar, "Collaboration", "collaboration", activeTitle);
        addNav(sidebar, "Recommendations", "recommendations", activeTitle);
        addNav(sidebar, "Analytics", "analytics", activeTitle);
        sidebar.getChildren().addAll(spacerV(), secondaryButton("Logout"));
        return sidebar;
    }

    private static HBox topBar(String title) {
        TextField search = searchField("Search SkillSync");
        search.setMaxWidth(280);
        Button notification = secondaryButton("Alerts");
        Button profile = secondaryButton("Profile");
        HBox bar = new HBox(14, title(title), spacer(), search, notification, profile);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private static Region spacerV() {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private static void addNav(VBox sidebar, String label, String screen, String activeTitle) {
        boolean active = activeTitle.equalsIgnoreCase(label)
                || activeTitle.equalsIgnoreCase("Recommendation Center") && label.equals("Recommendations");
        Button button = new Button(label);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(42);
        button.setStyle(active ? navStyle(PRIMARY, "white", "800") : navStyle("transparent", NAV_TEXT, "600"));
        button.setOnMouseEntered(e -> button.setStyle(active ? navStyle(PRIMARY, "white", "800") : navStyle(NAV_HOVER, PRIMARY, "700")));
        button.setOnMouseExited(e -> button.setStyle(active ? navStyle(PRIMARY, "white", "800") : navStyle("transparent", NAV_TEXT, "600")));
        button.setOnAction(event -> NavigationManager.getInstance().navigateTo(screen));
        sidebar.getChildren().add(button);
    }

    private static String cardStyle() {
        return "-fx-background-color: " + SURFACE
                + "; -fx-background-radius: " + RADIUS
                + "; -fx-border-color: " + BORDER
                + "; -fx-border-radius: " + RADIUS
                + "; -fx-effect: " + SHADOW + ";";
    }

    private static String buttonStyle(String background, String textColor, String borderColor) {
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-radius: " + RADIUS + ";"
                + "-fx-background-radius: " + RADIUS + ";"
                + "-fx-font-weight: 700;"
                + "-fx-padding: 9 14;"
                + "-fx-cursor: hand;";
    }

    private static String navStyle(String background, String textColor, String weight) {
        return "-fx-background-color: " + background + ";"
                + "-fx-text-fill: " + textColor + ";"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: " + weight + ";"
                + "-fx-background-radius: " + RADIUS + ";"
                + "-fx-cursor: hand;"
                + "-fx-padding: 11 14;"
                + "-fx-alignment: CENTER_LEFT;";
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    private static void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
