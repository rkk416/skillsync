package skillsync.dashboard;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
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
            PieChart skills = new PieChart(); service.generateSkillStatistics().forEach((name, value) -> skills.getData().add(new PieChart.Data(name, value.doubleValue())));
            BarChart<String, Number> readiness = chart("Placement Readiness"); XYChart.Series<String, Number> placement = new XYChart.Series<>(); service.generatePlacementStatistics().forEach((name, value) -> placement.getData().add(new XYChart.Data<>(name, value))); readiness.getData().add(placement);
            BarChart<String, Number> teams = chart("Team Statistics"); XYChart.Series<String, Number> teamSeries = new XYChart.Series<>(); Number count = service.generatePlacementStatistics().getOrDefault("Teams", 0); teamSeries.getData().add(new XYChart.Data<>("Teams", count)); teams.getData().add(teamSeries);
            tabs.getTabs().addAll(new Tab("Skills Distribution", skills), new Tab("Placement Readiness", readiness), new Tab("Team Statistics", teams));
        } catch (RuntimeException exception) { ViewFactory.error(exception.getMessage()); }
        getChildren().add(ViewFactory.shell("Analytics", tabs));
    }
    private BarChart<String, Number> chart(String title) { BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis()); chart.setTitle(title); chart.setLegendVisible(false); return chart; }
}
