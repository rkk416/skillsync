package skillsync.dashboard;

import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import skillsync.utils.ViewFactory;

public final class DashboardView extends VBox {
    public DashboardView() {
        DashboardController controller = new DashboardController();
        FlowPane cards = new FlowPane(18, 18);
        try { 
            controller.metrics().forEach((name, value) -> cards.getChildren().add(ViewFactory.metricCard(name, value)));
            if (cards.getChildren().isEmpty()) cards.getChildren().add(ViewFactory.emptyState("Dashboard unavailable", "No dashboard metrics are available yet."));
        }
        catch (RuntimeException exception) { 
            exception.printStackTrace();
            ViewFactory.error(exception.getMessage()); 
            cards.getChildren().setAll(ViewFactory.emptyState("Dashboard unavailable", "Metrics could not be loaded right now."));
        }
        getChildren().add(ViewFactory.shell("Dashboard", cards));
    }
}
