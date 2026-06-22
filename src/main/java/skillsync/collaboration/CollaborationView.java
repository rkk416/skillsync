package skillsync.collaboration;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import skillsync.model.Team;
import skillsync.utils.ViewFactory;

public final class CollaborationView extends VBox {
    private final CollaborationController controller = new CollaborationController();
    private final ListView<Team> teams = new ListView<>();
    public CollaborationView() {
        teams.setCellFactory(ignored -> new ListCell<>() { @Override protected void updateItem(Team team, boolean empty) { super.updateItem(team, empty); setText(empty || team == null ? null : team.getName() + " — " + (team.getDescription() == null ? "" : team.getDescription())); } });
        TextField name = new TextField(); name.setPromptText("Team name"); TextArea description = new TextArea(); description.setPromptText("Description"); description.setPrefRowCount(2);
        Button create = ViewFactory.primaryButton("Create Team"); create.setOnAction(event -> { try { controller.createTeam(name.getText(), description.getText()); name.clear(); description.clear(); refresh(); } catch (RuntimeException e) { ViewFactory.error(e.getMessage()); } });
        Button join = ViewFactory.primaryButton("Join Team"); join.setOnAction(event -> { Team selected = teams.getSelectionModel().getSelectedItem(); if (selected == null) ViewFactory.error("Select a team"); else { controller.joinTeam(selected.getId()); ViewFactory.info("Joined " + selected.getName()); } });
        Label suggestions = new Label(); suggestions.setWrapText(true);
        try { suggestions.setText("Suggested teammates: " + controller.teammates().stream().map(student -> "Student #" + student.getId()).toList()); } catch (RuntimeException e) { suggestions.setText(e.getMessage()); }
        refresh(); getChildren().add(ViewFactory.shell("Collaboration", new VBox(12, new Label("Team Builder"), name, description, new HBox(10, create, join), teams, suggestions)));
    }
    private void refresh() { try { teams.setItems(FXCollections.observableArrayList(controller.teams())); } catch (RuntimeException e) { ViewFactory.error(e.getMessage()); } }
}
