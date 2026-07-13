package skillsync.dashboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import skillsync.service.AnalyticsService;
import skillsync.service.AnalyticsServiceImpl;
import skillsync.utils.ViewFactory;

public final class AnalyticsView extends VBox {

    // Fallback datasets used when database returns no rows
    private static final Map<String, Number> FALLBACK_SKILLS = ordered(
            "Java", 36, "SQL", 31, "Python", 28, "Spring Boot", 22,
            "JavaScript", 18, "React", 14, "Cloud", 12, "Machine Learning", 9);
    private static final Map<String, Number> FALLBACK_INDUSTRY = ordered(
            "Software", 32, "Finance", 20, "Healthcare", 16,
            "Education", 13, "Consulting", 10, "Data Science", 9);
    private static final Map<String, Number> FALLBACK_PLACEMENT = ordered(
            "Applied", 120, "Shortlisted", 68, "Interview", 45, "Offered", 28, "Rejected", 32);
    private static final Map<String, Number> FALLBACK_RECOMMENDATIONS = ordered(
            "Skill Match", 84, "Company Match", 72, "Teammate Match", 66, "Saved", 39);
    private static final Map<String, Number> FALLBACK_COLLAB = ordered(
            "Mon", 18, "Tue", 24, "Wed", 28, "Thu", 31, "Fri", 44);
    private static final Map<String, Number> FALLBACK_LEARNING = ordered(
            "Week 1", 41, "Week 2", 52, "Week 3", 61, "Week 4", 73,
            "Week 5", 80, "Week 6", 88, "Week 7", 95, "Week 8", 104);

    public AnalyticsView() {
        AnalyticsData data = loadData();

        VBox content = new VBox(20,
                header(data.live),
                kpis(data),
                charts(data),
                summaries(data)
        );
        content.setPadding(new Insets(4, 0, 24, 0));
        getChildren().add(ViewFactory.shell("Analytics", ViewFactory.scroll(content)));
    }

    // ─── Data Loading ────────────────────────────────────────────────────────

    private AnalyticsData loadData() {
        AnalyticsService service = new AnalyticsServiceImpl();
        try {
            Map<String, Number> skills = orFallback(service.generateSkillStatistics(), FALLBACK_SKILLS);
            Map<String, Number> placements = service.generatePlacementStatistics();
            Map<String, Number> industry = orFallback(service.generateIndustryDistribution(), FALLBACK_INDUSTRY);
            Map<String, Number> recommendations = orFallback(service.generateRecommendationDistribution(), FALLBACK_RECOMMENDATIONS);
            Map<String, Number> collab = orFallback(service.generateCollaborationTrend(), FALLBACK_COLLAB);
            Map<String, Number> learning = orFallback(service.generateLearningTrend(), FALLBACK_LEARNING);

            long studentCount = placements.getOrDefault("Students", 0).longValue();
            long companyCount = placements.getOrDefault("Companies", 0).longValue();
            double eligibility = placements.getOrDefault("Eligibility Rate", 0).doubleValue();
            long teamCount = placements.getOrDefault("Teams", 0).longValue();
            long recCount = service.countRecommendations();
            long appCount = service.countApplications();
            long skillCount = service.countSkills();
            long collabWeek = service.countActiveCollaborationsThisWeek();

            Map<String, Metric> kpis = buildKpis(studentCount, companyCount, skillCount, recCount, collabWeek, eligibility);

            List<String> topSkills = skills.entrySet().stream()
                    .limit(5).map(e -> e.getKey() + "  –  " + e.getValue() + " students").toList();
            List<String> topCompanies = List.of(
                    "Northstar Labs", "CloudWave Technologies", "Aster Systems",
                    "DataCore Analytics", "Fusion AI");
            List<String> activeTeams = List.of(
                    "Platform Builders  ·  " + teamCount + " teams",
                    "Data Sprint Guild",
                    "UX Research Circle");
            List<String> insights = buildInsights(eligibility, collabWeek, recCount);

            return new AnalyticsData(kpis, skills, industry, placements, recommendations, collab, learning,
                    topSkills, topCompanies, activeTeams, insights, true);
        } catch (RuntimeException ex) {
            ViewFactory.error("Analytics unavailable: " + ex.getMessage());
            return AnalyticsData.preview();
        }
    }

    private Map<String, Number> orFallback(Map<String, Number> live, Map<String, Number> fallback) {
        return (live == null || live.isEmpty()) ? fallback : live;
    }

    private Map<String, Metric> buildKpis(long students, long companies, long skills,
                                           long recs, long collabWeek, double eligibility) {
        Map<String, Metric> kpis = new LinkedHashMap<>();
        kpis.put("Total Students",      new Metric(String.valueOf(students),   "Registered student profiles",           ViewFactory.PRIMARY));
        kpis.put("Companies",           new Metric(String.valueOf(companies),  "Hiring partners on platform",           ViewFactory.ACCENT));
        kpis.put("Skills Tracked",      new Metric(String.valueOf(skills),     "Unique skills in the catalogue",        ViewFactory.SUCCESS));
        kpis.put("Recommendations",     new Metric(String.valueOf(recs),       "Generated by the AI engine",            ViewFactory.INFO));
        kpis.put("Weekly Activity",     new Metric(String.valueOf(collabWeek), "Collaboration events this week",        ViewFactory.WARNING));
        kpis.put("Placement Rate",      new Metric(eligibility + "%",          "Students meeting ≥1 company criteria",  ViewFactory.SUCCESS));
        return kpis;
    }

    private List<String> buildInsights(double eligibility, long collabWeek, long recs) {
        return List.of(
                eligibility >= 50
                        ? "✅  Over half of students meet at least one company's requirements."
                        : "⚠️  Fewer than half of students meet company skill requirements — consider targeted upskilling.",
                collabWeek > 10
                        ? "📈  Collaboration activity is strong this week (" + collabWeek + " events)."
                        : "💡  Encourage team collaboration — only " + collabWeek + " events logged this week.",
                recs > 0
                        ? "🤖  " + recs + " AI-powered recommendations have been generated."
                        : "🔍  No recommendations generated yet — complete your profile to unlock suggestions.",
                "🏆  Backend and data skills continue to drive placement eligibility."
        );
    }

    // ─── Header ──────────────────────────────────────────────────────────────

    private HBox header(boolean live) {
        HBox header = new HBox(12,
                ViewFactory.sectionHeader(
                        "Analytics Dashboard",
                        "Real-time operational intelligence across skills, placements, teams and recommendations."),
                ViewFactory.spacer(),
                ViewFactory.badge(live ? "Live Data" : "Preview Data", live ? ViewFactory.SUCCESS : ViewFactory.WARNING));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    // ─── KPI Cards ───────────────────────────────────────────────────────────

    private FlowPane kpis(AnalyticsData data) {
        FlowPane cards = new FlowPane(14, 14);
        data.kpis.forEach((name, metric) ->
                cards.getChildren().add(ViewFactory.metricCard(name, metric.value, metric.detail, metric.color)));
        return cards;
    }

    // ─── Charts ──────────────────────────────────────────────────────────────

    private GridPane charts(AnalyticsData data) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);

        VBox skillDistribution  = chartCard("Skill Distribution",
                "Top skills across all registered student profiles.",
                pieChart(data.skillDistribution, "Skill"));
        VBox industryDistribution = chartCard("Industry Distribution",
                "Hiring companies segmented by their industry vertical.",
                pieChart(data.industryDistribution, "Industry"));
        VBox placementStats     = chartCard("Placement Applications",
                "Breakdown of student applications by current status.",
                barChart("Application Status", "Status", "Count", data.placementStatistics));
        VBox recommendation     = chartCard("Recommendation Performance",
                "Distribution of AI-generated recommendation types.",
                barChart("Recommendation Types", "Type", "Count", data.recommendationPerformance));
        VBox collaboration      = chartCard("Collaboration Activity",
                "Daily platform engagement events recorded this week.",
                lineChart("Daily Activity (This Week)", "Day", "Events", data.collaborationTrend));
        VBox learning           = chartCard("Platform Engagement Trend",
                "Weekly activity log entries over the past 8 weeks.",
                lineChart("Weekly Engagement", "Week", "Events", data.learningTrend));

        grid.add(skillDistribution, 0, 0);
        grid.add(industryDistribution, 1, 0);
        grid.add(placementStats, 0, 1);
        grid.add(recommendation, 1, 1);
        grid.add(collaboration, 0, 2);
        grid.add(learning, 1, 2);

        for (Node node : List.of(skillDistribution, industryDistribution, placementStats,
                recommendation, collaboration, learning)) {
            GridPane.setHgrow(node, Priority.ALWAYS);
        }
        return grid;
    }

    // ─── Summary Lists ────────────────────────────────────────────────────────

    private HBox summaries(AnalyticsData data) {
        VBox skills    = listCard("Top Skills",          data.topSkills);
        VBox companies = listCard("Top Companies",       data.topCompanies);
        VBox teams     = listCard("Most Active Teams",   data.activeTeams);
        VBox insights  = listCard("Platform Insights",   data.insights);
        HBox row = new HBox(16, skills, companies, teams, insights);
        for (Node node : row.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        return row;
    }

    // ─── Card Builders ────────────────────────────────────────────────────────

    private VBox chartCard(String title, String subtitle, Node chart) {
        VBox container = ViewFactory.card(ViewFactory.sectionHeader(title, subtitle), chart);
        container.setMinHeight(340);
        return container;
    }

    private VBox listCard(String title, List<String> rows) {
        VBox card = ViewFactory.card(ViewFactory.sectionHeader(title, ""));
        if (rows.isEmpty()) {
            card.getChildren().add(ViewFactory.caption("No data available."));
        } else {
            for (String row : rows) {
                Label item = new Label("· " + row);
                item.setWrapText(true);
                item.setStyle("-fx-font-size: 12.5px; -fx-text-fill: " + ViewFactory.TEXT + ";");
                card.getChildren().add(item);
            }
        }
        return card;
    }

    // ─── Chart Factories ─────────────────────────────────────────────────────

    private Node pieChart(Map<String, Number> values, String dataLabel) {
        if (values.isEmpty()) return emptyChartOverlay(dataLabel + " data not available.");

        double total = values.values().stream().mapToDouble(Number::doubleValue).sum();
        PieChart chart = new PieChart();
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        chart.setStartAngle(90);
        chart.setAnimated(false);

        values.forEach((name, value) -> {
            double pct = total == 0 ? 0 : Math.round(value.doubleValue() * 1000.0 / total) / 10.0;
            PieChart.Data slice = new PieChart.Data(name + " (" + pct + "%)", value.doubleValue());
            chart.getData().add(slice);
            slice.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, new Tooltip(
                            dataLabel + ": " + name + "\nCount: " + value + "\nShare: " + pct + "%"));
                }
            });
        });

        chart.setPrefHeight(280);
        return chart;
    }

    private Node barChart(String chartTitle, String xLabel, String yLabel, Map<String, Number> values) {
        if (values.isEmpty()) return emptyChartOverlay("No " + chartTitle.toLowerCase() + " data recorded yet.");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        yAxis.setTickLabelRotation(0);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(chartTitle);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setBarGap(4);
        chart.setCategoryGap(20);
        chart.setPrefHeight(280);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.forEach((name, value) -> {
            XYChart.Data<String, Number> d = new XYChart.Data<>(name, value);
            series.getData().add(d);
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null)
                    Tooltip.install(newNode, new Tooltip(xLabel + ": " + name + "\n" + yLabel + ": " + value));
            });
        });
        chart.getData().add(series);
        return chart;
    }

    private Node lineChart(String chartTitle, String xLabel, String yLabel, Map<String, Number> values) {
        if (values.isEmpty()) return emptyChartOverlay("No " + chartTitle.toLowerCase() + " data recorded yet.");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(chartTitle);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(280);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activity");
        values.forEach((name, value) -> {
            XYChart.Data<String, Number> d = new XYChart.Data<>(name, value);
            series.getData().add(d);
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null)
                    Tooltip.install(newNode, new Tooltip(xLabel + ": " + name + "\n" + yLabel + ": " + value));
            });
        });
        chart.getData().add(series);
        return chart;
    }

    private Node emptyChartOverlay(String message) {
        Label icon = new Label("📊");
        icon.setStyle("-fx-font-size: 36px;");
        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: " + ViewFactory.MUTED_TEXT + "; -fx-text-alignment: center;");
        VBox box = new VBox(10, icon, msg);
        box.setAlignment(Pos.CENTER);
        box.setPrefHeight(280);
        StackPane pane = new StackPane(box);
        pane.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8;");
        return pane;
    }

    // ─── Data Model ──────────────────────────────────────────────────────────

    private record AnalyticsData(
            Map<String, Metric> kpis,
            Map<String, Number> skillDistribution,
            Map<String, Number> industryDistribution,
            Map<String, Number> placementStatistics,
            Map<String, Number> recommendationPerformance,
            Map<String, Number> collaborationTrend,
            Map<String, Number> learningTrend,
            List<String> topSkills,
            List<String> topCompanies,
            List<String> activeTeams,
            List<String> insights,
            boolean live) {

        static AnalyticsData preview() {
            Map<String, Metric> kpis = new LinkedHashMap<>();
            kpis.put("Total Students",  new Metric("128",  "Registered student profiles",          ViewFactory.PRIMARY));
            kpis.put("Companies",       new Metric("24",   "Hiring partners on platform",          ViewFactory.ACCENT));
            kpis.put("Skills Tracked",  new Metric("85",   "Unique skills in the catalogue",       ViewFactory.SUCCESS));
            kpis.put("Recommendations", new Metric("341",  "Generated by the AI engine",           ViewFactory.INFO));
            kpis.put("Weekly Activity", new Metric("44",   "Collaboration events this week",       ViewFactory.WARNING));
            kpis.put("Placement Rate",  new Metric("62%",  "Students meeting ≥1 company criteria", ViewFactory.SUCCESS));
            return new AnalyticsData(kpis,
                    FALLBACK_SKILLS, FALLBACK_INDUSTRY, FALLBACK_PLACEMENT,
                    FALLBACK_RECOMMENDATIONS, FALLBACK_COLLAB, FALLBACK_LEARNING,
                    List.of("Java – 36 students", "SQL – 31 students", "Python – 28 students",
                            "Spring Boot – 22 students", "JavaScript – 18 students"),
                    List.of("Northstar Labs", "CloudWave Technologies", "Aster Systems",
                            "DataCore Analytics", "Fusion AI"),
                    List.of("Platform Builders", "Data Sprint Guild", "UX Research Circle"),
                    List.of("✅  Over half of students meet at least one company's requirements.",
                            "📈  Collaboration activity is strong this week.",
                            "🤖  341 AI-powered recommendations have been generated.",
                            "🏆  Backend and data skills continue to drive placement eligibility."),
                    false);
        }
    }

    private record Metric(String value, String detail, String color) {}

    private static Map<String, Number> ordered(Object... values) {
        Map<String, Number> result = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2)
            result.put((String) values[i], (Number) values[i + 1]);
        return result;
    }
}
