package skillsync.dashboard;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import skillsync.utils.ViewFactory;

public final class DashboardView extends VBox {
    public DashboardView() {
        DashboardController controller = new DashboardController();
        FlowPane cards = new FlowPane(18, 18);
        try { 
            controller.metrics().forEach((name, value) -> cards.getChildren().add(card(name, value))); 
        }
        catch (RuntimeException exception) { 
            exception.printStackTrace();
            ViewFactory.error(exception.getMessage()); 
        }
        getChildren().add(ViewFactory.shell("Dashboard", cards));
    }

    private VBox card(String name, Number value) {
        Label label = new Label(name); 
        Label metric = new Label(String.valueOf(value)); 
        metric.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
        VBox card = new VBox(10, label, metric); 
        card.setPadding(new Insets(20)); 
        card.setPrefSize(210, 130); 
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;"); 
        return card;
    }
}
