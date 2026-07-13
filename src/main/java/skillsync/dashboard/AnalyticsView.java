package skillsync.dashboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import skillsync.service.AnalyticsService;
import skillsync.service.AnalyticsServiceImpl;
import skillsync.utils.ViewFactory;

public final class AnalyticsView extends VBox {
    public AnalyticsView() {
        AnalyticsData data = AnalyticsData.preview();
        AnalyticsService service = new AnalyticsServiceImpl();
        try {
            data = data.withLiveData(service.generateSkillStatistics(), service.generatePlacementStatistics());
        } catch (RuntimeException exception) {
            ViewFactory.error(exception.getMessage());
        }

        VBox content = new VBox(18,
                header(data.live),
                kpis(data),
                charts(data),
                summaries(data)
        );
        getChildren().add(ViewFactory.shell("Analytics", ViewFactory.scroll(content)));
    }

    private HBox header(boolean live) {
        HBox header = new HBox(10,
                ViewFactory.sectionHeader("Analytics Command Center", "Operational intelligence across skills, placements, teams, and recommendations."),
                ViewFactory.spacer(),
                ViewFactory.badge(live ? "Live data" : "Preview data", live ? ViewFactory.SUCCESS : ViewFactory.WARNING));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private FlowPane kpis(AnalyticsData data) {
        FlowPane cards = new FlowPane(14, 14);
        data.kpis.forEach((name, metric) -> cards.getChildren().add(ViewFactory.metricCard(name, metric.value, metric.detail, metric.color)));
        return cards;
    }

    private GridPane charts(AnalyticsData data) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        VBox skillDistribution = chartCard("Skill Distribution", pieChart(data.skillDistribution));
        VBox industryDistribution = chartCard("Industry Distribution", pieChart(data.industryDistribution));
        VBox placementStats = chartCard("Placement Statistics", barChart("Placement Signals", data.placementStatistics));
        VBox recommendation = chartCard("Recommendation Performance", barChart("Recommendation Quality", data.recommendationPerformance));
        VBox collaboration = chartCard("Collaboration Activity", lineChart("Weekly Collaboration Activity", data.collaborationTrend));
        VBox learning = chartCard("Learning Progress", lineChart("Skill Growth Trend", data.learningTrend));
        grid.add(skillDistribution, 0, 0);
        grid.add(industryDistribution, 1, 0);
        grid.add(placementStats, 0, 1);
        grid.add(recommendation, 1, 1);
        grid.add(collaboration, 0, 2);
        grid.add(learning, 1, 2);
        for (Node node : List.of(skillDistribution, industryDistribution, placementStats, recommendation, collaboration, learning)) {
            GridPane.setHgrow(node, Priority.ALWAYS);
        }
        return grid;
    }

    private HBox summaries(AnalyticsData data) {
        VBox skills = listCard("Top Skills", data.topSkills);
        VBox companies = listCard("Top Companies", data.topCompanies);
        VBox teams = listCard("Most Active Teams", data.activeTeams);
        VBox insights = listCard("Insights Panel", data.insights);
        HBox row = new HBox(16, skills, companies, teams, insights);
        for (Node node : row.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);
        return row;
    }

    private VBox chartCard(String title, Node chart) {
        VBox container = ViewFactory.card(ViewFactory.sectionHeader(title, "Ready for live repository-backed expansion."), chart);
        container.setMinHeight(320);
        return container;
    }

    private VBox listCard(String title, List<String> rows) {
        VBox card = ViewFactory.card(ViewFactory.sectionHeader(title, "Ranked signals for fast scanning."));
        for (String row : rows) {
            card.getChildren().add(ViewFactory.caption(row));
        }
        return card;
    }

    private PieChart pieChart(Map<String, Number> values) {
        PieChart chart = new PieChart();
        values.forEach((name, value) -> {
            PieChart.Data data = new PieChart.Data(name, value.doubleValue());
            chart.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    javafx.scene.control.Tooltip.install(newNode, new javafx.scene.control.Tooltip(data.getName() + ": " + data.getPieValue()));
                }
            });
        });
        chart.setLegendVisible(true);
        chart.setLabelsVisible(true);
        return chart;
    }


    private BarChart<String, Number> barChart(String title, Map<String, Number> values) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Category");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.forEach((name, value) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(name, value);
            series.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    javafx.scene.control.Tooltip.install(newNode, new javafx.scene.control.Tooltip(data.getXValue() + ": " + data.getYValue()));
                }
            });
        });
        chart.getData().add(series);
        return chart;
    }


    private LineChart<String, Number> lineChart(String title, Map<String, Number> values) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Timeline");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        values.forEach((name, value) -> {
            XYChart.Data<String, Number> data = new XYChart.Data<>(name, value);
            series.getData().add(data);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    javafx.scene.control.Tooltip.install(newNode, new javafx.scene.control.Tooltip(data.getXValue() + ": " + data.getYValue()));
                }
            });
        });
        chart.getData().add(series);
        return chart;
    }


    private record AnalyticsData(Map<String, Metric> kpis, Map<String, Number> skillDistribution,
                                 Map<String, Number> industryDistribution, Map<String, Number> placementStatistics,
                                 Map<String, Number> recommendationPerformance, Map<String, Number> collaborationTrend,
                                 Map<String, Number> learningTrend, List<String> topSkills, List<String> topCompanies,
                                 List<String> activeTeams, List<String> insights, boolean live) {
        static AnalyticsData preview() {
            Map<String, Metric> kpis = new LinkedHashMap<>();
            kpis.put("Placement Readiness", new Metric("82%", "Average readiness across tracked students", ViewFactory.SUCCESS));
            kpis.put("Skill Coverage", new Metric("68%", "Required technologies represented", ViewFactory.ACCENT));
            kpis.put("Recommendation Accuracy", new Metric("91%", "High-confidence suggestions", ViewFactory.PRIMARY));
            kpis.put("Team Activity", new Metric("44", "Collaboration events this week", ViewFactory.WARNING));
            return new AnalyticsData(kpis,
                    ordered("Java", 36, "SQL", 31, "Python", 28, "JavaFX", 16, "PostgreSQL", 14),
                    ordered("Software", 30, "Data", 22, "Cloud", 18, "Finance", 14, "Consulting", 9),
                    ordered("Students", 128, "Companies", 24, "Eligibility Rate", 62, "Teams", 18),
                    ordered("Skill", 84, "Company", 72, "Teammate", 66, "Saved", 39),
                    ordered("Mon", 18, "Tue", 24, "Wed", 28, "Thu", 31, "Fri", 44),
                    ordered("Week 1", 41, "Week 2", 52, "Week 3", 61, "Week 4", 73),
                    List.of("Java - 36 profiles", "SQL - 31 profiles", "Python - 28 profiles"),
                    List.of("Northstar Labs", "CloudWave", "Aster Systems"),
                    List.of("Platform Builders", "Data Sprint", "UX Research Guild"),
                    List.of("Backend skills continue to drive placement readiness.", "Team activity is strongest mid-week.", "Recommendation follow-through is above target."),
                    false);
        }

        AnalyticsData withLiveData(Map<String, Number> skills, Map<String, Number> placements) {
            Map<String, Metric> liveKpis = new LinkedHashMap<>(kpis);
            liveKpis.put("Students", new Metric(String.valueOf(placements.getOrDefault("Students", 0)), "Loaded from repository statistics", ViewFactory.PRIMARY));
            liveKpis.put("Companies", new Metric(String.valueOf(placements.getOrDefault("Companies", 0)), "Loaded from repository statistics", ViewFactory.ACCENT));
            liveKpis.put("Eligibility Rate", new Metric(placements.getOrDefault("Eligibility Rate", 0) + "%", "Computed from skill gaps", ViewFactory.SUCCESS));
            liveKpis.put("Teams", new Metric(String.valueOf(placements.getOrDefault("Teams", 0)), "Loaded from team records", ViewFactory.WARNING));
            return new AnalyticsData(liveKpis, skills.isEmpty() ? skillDistribution : skills, industryDistribution,
                    placements.isEmpty() ? placementStatistics : placements, recommendationPerformance, collaborationTrend,
                    learningTrend, topSkills, topCompanies, activeTeams, insights, true);
        }

        private static Map<String, Number> ordered(Object... values) {
            Map<String, Number> result = new LinkedHashMap<>();
            for (int index = 0; index < values.length; index += 2) {
                result.put((String) values[index], (Number) values[index + 1]);
            }
            return result;
        }
    }

    private record Metric(String value, String detail, String color) { }
}
