package skillsync.profile;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import skillsync.model.Certification;
import skillsync.model.Project;
import skillsync.model.Student;
import skillsync.utils.ViewFactory;

public final class ProfileView extends VBox {
    private final ProfileController controller = new ProfileController();

    public ProfileView() {
        TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        try {
            tabs.getTabs().addAll(new Tab("Personal Info", personal()), new Tab("Skills", skills()), new Tab("Certifications", certifications()), new Tab("Projects", projects()));
        } catch (RuntimeException exception) { ViewFactory.error(exception.getMessage()); }
        getChildren().add(ViewFactory.shell("Profile", tabs));
    }

    private GridPane personal() {
        Student student = controller.loadProfile(); GridPane form = new GridPane(); form.setHgap(12); form.setVgap(12);
        TextField university = new TextField(student.getUniversity()); TextField degree = new TextField(student.getDegree()); TextField year = new TextField(student.getGraduationYear() == 0 ? "" : String.valueOf(student.getGraduationYear())); TextArea bio = new TextArea(student.getBio()); bio.setPrefRowCount(4);
        form.addRow(0, new Label("University"), university); form.addRow(1, new Label("Degree"), degree); form.addRow(2, new Label("Graduation Year"), year); form.addRow(3, new Label("Bio"), bio);
        Button save = ViewFactory.primaryButton("Save Profile"); save.setOnAction(event -> { try { student.setUniversity(university.getText()); student.setDegree(degree.getText()); student.setGraduationYear(year.getText().isBlank() ? 0 : Integer.parseInt(year.getText())); student.setBio(bio.getText()); controller.saveProfile(student); ViewFactory.info("Profile saved"); } catch (NumberFormatException e) { ViewFactory.error("Graduation year must be a number"); } }); form.add(save, 1, 4); return form;
    }

    private VBox skills() {
        ListView<skillsync.model.Skill> list = new ListView<>(); list.setCellFactory(ignored -> new ListCell<>() { @Override protected void updateItem(skillsync.model.Skill value, boolean empty) { super.updateItem(value, empty); setText(empty || value == null ? null : value.getName() + " — " + value.getCategory()); } });
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(controller.loadSkills())); refresh.run();
        TextField name = new TextField(); name.setPromptText("Skill name"); TextField category = new TextField(); category.setPromptText("Category");
        Button add = ViewFactory.primaryButton("Add Skill"); add.setOnAction(event -> { controller.addSkill(name.getText(), category.getText()); name.clear(); category.clear(); refresh.run(); });
        Button delete = new Button("Remove"); delete.setOnAction(event -> { var selected = list.getSelectionModel().getSelectedItem(); if (selected != null) { controller.deleteSkill(selected.getId()); refresh.run(); } });
        return new VBox(10, list, new HBox(10, name, category, add, delete));
    }

    private VBox certifications() {
        ListView<Certification> list = new ListView<>(); list.setCellFactory(ignored -> new ListCell<>() { @Override protected void updateItem(Certification value, boolean empty) { super.updateItem(value, empty); setText(empty || value == null ? null : value.getName() + " — " + value.getIssuingOrganization()); } });
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(controller.loadCertifications())); refresh.run();
        TextField name = new TextField(); name.setPromptText("Certification"); TextField issuer = new TextField(); issuer.setPromptText("Issuing organization");
        list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> { if (value != null) { name.setText(value.getName()); issuer.setText(value.getIssuingOrganization()); } });
        Button add = ViewFactory.primaryButton("Save"); add.setOnAction(event -> { Certification selected = list.getSelectionModel().getSelectedItem(); Certification value = selected == null ? new Certification(0, 0, name.getText(), issuer.getText(), null, null, null) : selected; value.setName(name.getText()); value.setIssuingOrganization(issuer.getText()); controller.saveCertification(value); list.getSelectionModel().clearSelection(); name.clear(); issuer.clear(); refresh.run(); });
        Button delete = new Button("Delete"); delete.setOnAction(event -> { Certification selected = list.getSelectionModel().getSelectedItem(); if (selected != null) { controller.deleteCertification(selected.getId()); refresh.run(); } });
        return new VBox(10, list, new HBox(10, name, issuer, add, delete));
    }

    private VBox projects() {
        ListView<Project> list = new ListView<>(); list.setCellFactory(ignored -> new ListCell<>() { @Override protected void updateItem(Project value, boolean empty) { super.updateItem(value, empty); setText(empty || value == null ? null : value.getName() + " — " + value.getRepositoryUrl()); } });
        Runnable refresh = () -> list.setItems(FXCollections.observableArrayList(controller.loadProjects())); refresh.run();
        TextField name = new TextField(); name.setPromptText("Project name"); TextField url = new TextField(); url.setPromptText("Repository URL");
        list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> { if (value != null) { name.setText(value.getName()); url.setText(value.getRepositoryUrl()); } });
        Button save = ViewFactory.primaryButton("Save Project"); save.setOnAction(event -> { Project selected = list.getSelectionModel().getSelectedItem(); Project value = selected == null ? new Project(0, 0, name.getText(), null, url.getText(), null, null) : selected; value.setName(name.getText()); value.setRepositoryUrl(url.getText()); controller.saveProject(value); list.getSelectionModel().clearSelection(); name.clear(); url.clear(); refresh.run(); });
        Button delete = new Button("Delete"); delete.setOnAction(event -> { Project selected = list.getSelectionModel().getSelectedItem(); if (selected != null) { controller.deleteProject(selected.getId()); refresh.run(); } });
        return new VBox(10, list, new HBox(10, name, url, save, delete));
    }
}
