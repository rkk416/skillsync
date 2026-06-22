package skillsync.recommendation;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import skillsync.utils.ViewFactory;

public final class RecommendationView extends VBox {
    public RecommendationView() {
        RecommendationController controller = new RecommendationController();
        TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        try {
            ListView<String> skillList = new ListView<>(FXCollections.observableArrayList(controller.skills().stream().map(value -> value.getName()).toList()));
            ListView<String> companyList = new ListView<>(FXCollections.observableArrayList(controller.companies().stream().map(value -> value.getName()).toList()));
            ListView<String> teammateList = new ListView<>(FXCollections.observableArrayList(controller.teammates().stream().map(value -> "Student #" + value.getId() + " — " + value.getDegree()).toList()));
            tabs.getTabs().addAll(new Tab("Recommended Skills", skillList), new Tab("Recommended Companies", companyList), new Tab("Recommended Teammates", teammateList));
        } catch (RuntimeException exception) { ViewFactory.error(exception.getMessage()); }
        getChildren().add(ViewFactory.shell("Recommendations", tabs));
    }
}
