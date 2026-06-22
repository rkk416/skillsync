package skillsync.placement;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import skillsync.model.Company;
import skillsync.utils.ViewFactory;

public final class PlacementView extends VBox {
    public PlacementView() {
        PlacementController controller = new PlacementController();
        Label score = new Label(); score.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        ListView<Company> companies = new ListView<>(); companies.setCellFactory(ignored -> new ListCell<>() { @Override protected void updateItem(Company value, boolean empty) { super.updateItem(value, empty); setText(empty || value == null ? null : value.getName() + (value.getIndustry() == null ? "" : " — " + value.getIndustry())); } });
        Label result = new Label("Select a company to check readiness."); result.setWrapText(true);
        Button eligibility = ViewFactory.primaryButton("Check Eligibility"); eligibility.setOnAction(event -> {
            Company selected = companies.getSelectionModel().getSelectedItem(); if (selected == null) { ViewFactory.error("Select a company"); return; }
            try { result.setText(controller.eligible(selected.getId()) ? "Eligible" : "Not eligible. Missing: " + controller.skillGap(selected.getId()).stream().map(skill -> skill.getName()).toList()); } catch (RuntimeException e) { ViewFactory.error(e.getMessage()); }
        });
        try { score.setText("Placement Score: " + controller.score() + "%"); companies.setItems(FXCollections.observableArrayList(controller.companies())); } catch (RuntimeException e) { ViewFactory.error(e.getMessage()); }
        getChildren().add(ViewFactory.shell("Placement", new VBox(14, score, companies, new HBox(10, eligibility), result)));
    }
}
