package skillsync.dashboard;

import java.util.Map;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import skillsync.service.AnalyticsService;
import skillsync.service.AnalyticsServiceImpl;
import skillsync.utils.ViewFactory;

public final class AnalyticsView extends VBox {
    public AnalyticsView() {
        AnalyticsService service = new AnalyticsServiceImpl(); TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        try {
            Map<String, Number> skillStatistics = service.generateSkillStatistics();
            Map<String, Number> placementStatistics = service.generatePlacementStatistics();
            PieChart skills = new PieChart();
            skillStatistics.forEach((name, value) -> skills.getData().add(new PieChart.Data(name, value.doubleValue())));
            skills.setTitle("Skills Distribution");
            Node skillsContent = skills.getData().isEmpty()
                    ? ViewFactory.emptyState("No skill data", "Add skills to student profiles to populate this chart.")
                    : skills;
            BarChart<String, Number> readiness = chart("Placement Readiness");
            XYChart.Series<String, Number> placement = new XYChart.Series<>();
            placementStatistics.forEach((name, value) -> placement.getData().add(new XYChart.Data<>(name, value)));
            readiness.getData().add(placement);
            BarChart<String, Number> teams = chart("Team Statistics");
            XYChart.Series<String, Number> teamSeries = new XYChart.Series<>();
            Number count = placementStatistics.getOrDefault("Teams", 0);
            teamSeries.getData().add(new XYChart.Data<>("Teams", count));
            teams.getData().add(teamSeries);
            tabs.getTabs().addAll(new Tab("Skills Distribution", skillsContent), new Tab("Placement Readiness", readiness), new Tab("Team Statistics", teams));
        } catch (RuntimeException exception) { ViewFactory.error(exception.getMessage()); tabs.getTabs().setAll(new Tab("Analytics", ViewFactory.emptyState("Analytics unavailable", "Analytics could not be loaded right now."))); }
        getChildren().add(ViewFactory.shell("Analytics", tabs));
    }
    private BarChart<String, Number> chart(String title) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        return chart;
    }
}
