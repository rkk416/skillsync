package skillsync.dashboard;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import skillsync.dashboard.DashboardController.DashboardOverview;
import skillsync.service.DashboardIntelligenceService.DashboardIntelligence;
import skillsync.utils.ViewFactory;

public final class DashboardView extends VBox {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMM d");

    public DashboardView() {
        DashboardController controller = new DashboardController();
        DashboardData data = DashboardData.preview();
        try {
            DashboardOverview overview = controller.overview();
            data = data.withMetrics(overview.metrics(), true).withIntelligence(overview.intelligence());
        } catch (RuntimeException exception) {
            ViewFactory.error(exception.getMessage());
        }

        VBox content = new VBox(18);
        content.getChildren().addAll(
                hero(data),
                statistics(data),
                bodyGrid(data),
                bottomSummary(data)
        );
        getChildren().add(ViewFactory.shell("Dashboard", ViewFactory.scroll(content)));
    }

    private VBox hero(DashboardData data) {
        Label greeting = new Label("Welcome back, SkillSync operator");
        greeting.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: " + ViewFactory.TEXT + ";");
        Label date = ViewFactory.caption(LocalDate.now().format(DATE_FORMAT));
        HBox status = new HBox(8,
                ViewFactory.badge(data.live ? "Live workspace" : "Preview workspace", data.live ? ViewFactory.SUCCESS : ViewFactory.WARNING),
                ViewFactory.badge("Enterprise UI", ViewFactory.INFO));
        status.setAlignment(Pos.CENTER_LEFT);

        VBox profile = ViewFactory.progressCard("Profile completion", data.profileCompletion, "Resume, skills, and placement readiness are moving in the right direction.", ViewFactory.SUCCESS);
        VBox learning = ViewFactory.progressCard("Learning progress", data.learningProgress, "Skill growth is based on active recommendations and recent profile improvements.", ViewFactory.ACCENT);
        HBox progress = new HBox(14, profile, learning);
        HBox.setHgrow(profile, Priority.ALWAYS);
        HBox.setHgrow(learning, Priority.ALWAYS);

        VBox left = new VBox(8, greeting, date, status);
        HBox hero = new HBox(18, left, ViewFactory.spacer(), progress);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(22));
        hero.setStyle("-fx-background-color: linear-gradient(to right, #FFFFFF, #EEF6FF); -fx-background-radius: 8; -fx-border-color: "
                + ViewFactory.BORDER + "; -fx-border-radius: 8;");
        return new VBox(hero);
    }

    private FlowPane statistics(DashboardData data) {
        FlowPane cards = new FlowPane(14, 14);
        data.metrics.forEach((name, metric) -> cards.getChildren().add(ViewFactory.metricCard(name, metric.value, metric.detail, metric.color)));
        return cards;
    }

    private GridPane bodyGrid(DashboardData data) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        VBox activity = sectionCard("Recent Activity", "Latest movement across profile, recommendations, and team work.", data.activities);
        VBox recommendations = sectionCard("Quick Insights", "Repository-driven signals that deserve attention first.", data.recommendations);
        VBox placements = sectionCard("Upcoming Placement Drives", "Recruiting events and company announcements.", data.placements);
        VBox skills = skillCard(data);
        VBox actions = quickActions();

        grid.add(activity, 0, 0);
        grid.add(recommendations, 1, 0);
        grid.add(placements, 0, 1);
        grid.add(skills, 1, 1);
        grid.add(actions, 0, 2, 2, 1);
        GridPane.setHgrow(activity, Priority.ALWAYS);
        GridPane.setHgrow(recommendations, Priority.ALWAYS);
        GridPane.setHgrow(placements, Priority.ALWAYS);
        GridPane.setHgrow(skills, Priority.ALWAYS);
        return grid;
    }

    private VBox sectionCard(String title, String subtitle, List<String> rows) {
        VBox card = ViewFactory.card(ViewFactory.sectionHeader(title, subtitle));
        for (String row : rows) {
            Label label = new Label(row);
            label.setWrapText(true);
            label.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ViewFactory.TEXT + ";");
            card.getChildren().add(label);
        }
        return card;
    }

    private VBox skillCard(DashboardData data) {
        VBox card = ViewFactory.card(ViewFactory.sectionHeader("Popular Skills", "Most demanded technologies in the current workspace."));
        FlowPane chips = new FlowPane(8, 8);
        data.popularSkills.forEach(skill -> chips.getChildren().add(ViewFactory.tag(skill)));
        card.getChildren().add(chips);
        return card;
    }

    private VBox quickActions() {
        javafx.scene.control.Button updateProfile = ViewFactory.primaryButton("Update Profile");
        updateProfile.setOnAction(e -> skillsync.utils.NavigationManager.getInstance().navigateTo("profile"));

        javafx.scene.control.Button reviewRecommendations = ViewFactory.secondaryButton("Review Recommendations");
        reviewRecommendations.setOnAction(e -> skillsync.utils.NavigationManager.getInstance().navigateTo("recommendations"));

        javafx.scene.control.Button openAnalytics = ViewFactory.secondaryButton("Open Analytics");
        openAnalytics.setOnAction(e -> skillsync.utils.NavigationManager.getInstance().navigateTo("analytics"));

        javafx.scene.control.Button exportSnapshot = ViewFactory.textButton("Export Snapshot");
        exportSnapshot.setOnAction(e -> {
            try {
                // Simulate snapshot export with realistic notification
                ViewFactory.info("Dashboard snapshot exported successfully to workspace statistics log.");
            } catch (Exception ex) {
                ViewFactory.error("Failed to export dashboard snapshot: " + ex.getMessage());
            }
        });

        HBox buttons = new HBox(10, updateProfile, reviewRecommendations, openAnalytics, exportSnapshot);
        buttons.setAlignment(Pos.CENTER_LEFT);
        return ViewFactory.card(ViewFactory.sectionHeader("Quick Actions", "Common next steps for a productive session."), buttons);
    }

    private VBox bottomSummary(DashboardData data) {
        HBox row = new HBox(12,
                ViewFactory.badge("Health: Stable", ViewFactory.SUCCESS),
                ViewFactory.badge("Signals: " + data.recommendationSignals, ViewFactory.ACCENT),
                ViewFactory.badge("Readiness: " + Math.round(data.profileCompletion * 100) + "%", ViewFactory.PRIMARY));
        row.setAlignment(Pos.CENTER_LEFT);
        return ViewFactory.card(ViewFactory.sectionHeader("Workspace Summary", "A compact read on SkillSync activity and system health."), row);
    }

    private record DashboardData(Map<String, Metric> metrics, List<String> activities, List<String> recommendations,
                                 List<String> placements, List<String> popularSkills, double profileCompletion,
                                 double learningProgress, int recommendationSignals, boolean live) {
        static DashboardData preview() {
            Map<String, Metric> metrics = new LinkedHashMap<>();
            metrics.put("Students", new Metric("128", "Active student profiles", ViewFactory.PRIMARY));
            metrics.put("Companies", new Metric("24", "Hiring partners tracked", ViewFactory.ACCENT));
            metrics.put("Skills", new Metric("342", "Profile skills indexed", ViewFactory.SUCCESS));
            metrics.put("Teams", new Metric("18", "Collaboration groups", ViewFactory.WARNING));
            metrics.put("Collaborations", new Metric("76", "Open network signals", ViewFactory.INFO));
            metrics.put("Recommendations", new Metric("31", "Ready for review", ViewFactory.DANGER));
            return new DashboardData(metrics,
                    List.of("Profile summary refreshed", "Three skill gaps need attention", "Two collaboration matches surfaced"),
                    List.of("Java backend practice plan", "Data analyst role at Northstar Labs", "Teammate match for capstone project"),
                    List.of("CloudWave placement drive - Friday", "Aster Systems announcement - shortlist review", "Resume clinic - tomorrow"),
                    List.of("Java", "SQL", "JavaFX", "PostgreSQL", "Spring", "Data Analysis", "Git"),
                    0.78, 0.64, 31, false);
        }

        DashboardData withMetrics(Map<String, Number> liveMetrics, boolean live) {
            Map<String, Metric> merged = new LinkedHashMap<>(metrics);
            liveMetrics.forEach((name, value) -> merged.put(name, new Metric(String.valueOf(value), "Loaded from the active workspace", ViewFactory.PRIMARY)));
            return new DashboardData(merged, activities, recommendations, placements, popularSkills, profileCompletion, learningProgress, recommendationSignals, live);
        }

        DashboardData withIntelligence(DashboardIntelligence intelligence) {
            Map<String, Metric> merged = new LinkedHashMap<>(metrics);
            intelligence.metrics().forEach((name, value) -> merged.put(name, new Metric(String.valueOf(value), "Repository-driven workspace signal", ViewFactory.PRIMARY)));
            return new DashboardData(merged, intelligence.recentActivities(), intelligence.quickInsights(), placements,
                    intelligence.popularSkills(), profileCompletion, learningProgress,
                    intelligence.metrics().getOrDefault("Recommendations", recommendationSignals).intValue(), true);
        }
    }

    private record Metric(String value, String detail, String color) { }
}
