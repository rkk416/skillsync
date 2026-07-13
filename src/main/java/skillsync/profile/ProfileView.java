package skillsync.profile;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import skillsync.model.Certification;
import skillsync.model.Project;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.utils.ViewFactory;

import java.time.LocalDate;

public final class ProfileView extends VBox {
    private final ProfileController controller = new ProfileController();

    // ---- Light blue / white palette (visual only — no logic below depends on these) ----
    private static final String ACCENT = "#1D6FE0";
    private static final String ACCENT_TINT = "#E6F1FB";
    private static final String ACCENT_TEXT = "#0C447C";
    private static final String TEXT_PRIMARY = "#0C2A47";
    private static final String TEXT_SECONDARY = "#5B6B7D";
    private static final String CARD_BORDER = "#DCE7F5";
    private static final String FIELD_BG = "#F8FBFF";
    private static final String PAGE_BG = "#F0F7FF";

    private static final String CARD_STYLE =
            "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24;";
    private static final String TAB_ROOT_STYLE = "-fx-background-color: " + PAGE_BG + ";";

    private static final String FIELD_STYLE_NORMAL =
            "-fx-background-color: " + FIELD_BG + "; -fx-background-radius: 8; -fx-border-radius: 8; "
                    + "-fx-border-color: " + CARD_BORDER + "; -fx-border-width: 1; -fx-text-fill: " + TEXT_PRIMARY + ";";
    private static final String FIELD_STYLE_ERROR =
            "-fx-background-color: " + FIELD_BG + "; -fx-background-radius: 8; -fx-border-radius: 8; "
                    + "-fx-border-color: #DC2626; -fx-border-width: 1.5; -fx-text-fill: " + TEXT_PRIMARY + ";";
    private static final String ERROR_LABEL_STYLE = "-fx-text-fill: #DC2626; -fx-font-size: 11px;";
    private static final String FIELD_LABEL_STYLE = "-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";";
    private static final String SECTION_LABEL_STYLE =
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";";

    private static final String PRIMARY_BUTTON_STYLE =
            "-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 10 20; -fx-font-weight: bold;";
    private static final String SECONDARY_BUTTON_STYLE =
            "-fx-background-color: " + FIELD_BG + "; -fx-text-fill: " + TEXT_SECONDARY + "; -fx-background-radius: 8; "
                    + "-fx-border-color: " + CARD_BORDER + "; -fx-border-radius: 8; -fx-padding: 10 20;";
    private static final String SECONDARY_BUTTON_HOVER_STYLE =
            SECONDARY_BUTTON_STYLE + " -fx-background-color: #EEF4FC;";
    private static final String DANGER_BUTTON_STYLE =
            "-fx-background-color: " + FIELD_BG + "; -fx-text-fill: #B91C1C; -fx-background-radius: 8; "
                    + "-fx-border-color: #FBD5D5; -fx-border-radius: 8; -fx-padding: 10 20;";
    private static final String DANGER_BUTTON_HOVER_STYLE =
            "-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; -fx-background-radius: 8; "
                    + "-fx-border-color: #FBD5D5; -fx-border-radius: 8; -fx-padding: 10 20;";

    public ProfileView() {
    setStyle(TAB_ROOT_STYLE);
    TabPane tabs = new TabPane();
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabs.setStyle("-fx-background-color: transparent;");
    tabs.getStylesheets().add(
        "data:text/css," +
        ".tab-pane .tab-header-area{-fx-padding:16 0 0 0;}" +
        ".tab-pane .headers-region{-fx-background-color:white;-fx-background-radius:12;-fx-padding:6;}" +
        ".tab-pane .tab{-fx-background-color:white;-fx-background-radius:9;-fx-padding:10 18;-fx-background-insets:0 6 0 0;}" +
        ".tab-pane .tab:selected{-fx-background-color:#1D6FE0;}" +
        ".tab-pane .tab .tab-label{-fx-text-fill:#5B6B7D;-fx-font-size:13px;}" +
        ".tab-pane .tab:selected .tab-label{-fx-text-fill:white;-fx-font-weight:bold;}" +
        ".tab-pane .tab-header-background{-fx-background-color:transparent;}"
    );
    try {
        tabs.getTabs().addAll(
                new Tab("Personal Info", personal()),
                new Tab("Skills", skills()),
                new Tab("Certifications", certifications()),
                new Tab("Projects", projects()));
    } catch (RuntimeException exception) {
        exception.printStackTrace();
        ViewFactory.error(exception.getMessage());
    }
    applyFadeOnTabSwitch(tabs);
    getChildren().add(ViewFactory.shell("Profile", tabs));
}
    private void applyFadeOnTabSwitch(TabPane tabs) {
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == null || newTab.getContent() == null) return;
            FadeTransition fade = new FadeTransition(Duration.millis(180), newTab.getContent());
            fade.setFromValue(0.3);
            fade.setToValue(1.0);
            fade.play();
        });
    }

    private void styleHover(Button button, String baseStyle, String hoverStyle) {
        button.setStyle(baseStyle);
        button.setOnMouseEntered(event -> button.setStyle(hoverStyle));
        button.setOnMouseExited(event -> button.setStyle(baseStyle));
    }

    private Label errorLabel() {
        Label label = new Label("");
        label.setStyle(ERROR_LABEL_STYLE);
        return label;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(FIELD_LABEL_STYLE);
        return label;
    }

    private boolean validateCgpaField(TextField field, Label errorLabel, String value) {
        if (value == null || value.isBlank()) {
            errorLabel.setText("");
            field.setStyle(FIELD_STYLE_NORMAL);
            return true;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            if (parsed < 0.0 || parsed > 10.0) {
                errorLabel.setText("CGPA must be between 0 and 10");
                field.setStyle(FIELD_STYLE_ERROR);
                return false;
            }
            errorLabel.setText("");
            field.setStyle(FIELD_STYLE_NORMAL);
            return true;
        } catch (NumberFormatException e) {
            errorLabel.setText("CGPA must be a number");
            field.setStyle(FIELD_STYLE_ERROR);
            return false;
        }
    }

    private boolean validateYearField(TextField field, Label errorLabel, String value) {
        if (value == null || value.isBlank()) {
            errorLabel.setText("");
            field.setStyle(FIELD_STYLE_NORMAL);
            return true;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            int currentYear = LocalDate.now().getYear();
            if (parsed < currentYear || parsed > 2200) {
                errorLabel.setText("Graduation year must be " + currentYear + " or later");
                field.setStyle(FIELD_STYLE_ERROR);
                return false;
            }
            errorLabel.setText("");
            field.setStyle(FIELD_STYLE_NORMAL);
            return true;
        } catch (NumberFormatException e) {
            errorLabel.setText("Graduation year must be a number");
            field.setStyle(FIELD_STYLE_ERROR);
            return false;
        }
    }

    private StackPane avatar(String fullName) {
        String initial = (fullName == null || fullName.isBlank()) ? "?" : fullName.trim().substring(0, 1).toUpperCase();
        Label letter = new Label(initial);
        letter.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + ACCENT_TEXT + ";");
        StackPane circle = new StackPane(letter);
        circle.setPrefSize(56, 56);
        circle.setMaxSize(56, 56);
        circle.setMinSize(56, 56);
        circle.setStyle("-fx-background-color: " + ACCENT_TINT + "; -fx-background-radius: 28;");
        return circle;
    }

    private VBox personal() {
        Student student = controller.loadProfile();

        StackPane avatar = avatar(student.getFullName());
        Label nameLabel = new Label(student.getFullName() == null || student.getFullName().isBlank() ? "Your name" : student.getFullName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TEXT_PRIMARY + ";");
        Label subLabel = new Label((student.getBranch() == null ? "" : student.getBranch())
                + (student.getGraduationYear() == 0 ? "" : " · Class of " + student.getGraduationYear()));
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + TEXT_SECONDARY + ";");
        VBox nameBlock = new VBox(2, nameLabel, subLabel);
        HBox header = new HBox(16, avatar, nameBlock);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setStyle("-fx-border-color: " + ACCENT_TINT + "; -fx-border-width: 0 0 1 0;");

        Label sectionLabel = new Label("Personal information");
        sectionLabel.setStyle(SECTION_LABEL_STYLE);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(6);

        TextField fullName = new TextField(student.getFullName());
        TextField branch = new TextField(student.getBranch());
        TextField cgpa = new TextField(student.getCgpa() == null ? "" : String.valueOf(student.getCgpa()));
        TextField year = new TextField(student.getGraduationYear() == 0 ? "" : String.valueOf(student.getGraduationYear()));

        for (TextField field : new TextField[]{fullName, branch, cgpa, year}) {
            field.setPrefWidth(260);
            field.setPrefHeight(36);
            field.setDisable(true);
            field.setStyle(FIELD_STYLE_NORMAL);
        }

        Label nameError = errorLabel();
        Label cgpaError = errorLabel();
        Label yearError = errorLabel();

        VBox nameCol = new VBox(4, fieldLabel("Full name"), fullName, nameError);
        VBox branchCol = new VBox(4, fieldLabel("Branch"), branch);
        VBox cgpaCol = new VBox(4, fieldLabel("CGPA"), cgpa, cgpaError);
        VBox yearCol = new VBox(4, fieldLabel("Graduation year"), year, yearError);

        form.add(nameCol, 0, 0);
        form.add(branchCol, 1, 0);
        form.add(cgpaCol, 0, 1);
        form.add(yearCol, 1, 1);
        form.setPadding(new Insets(4, 0, 24, 0));

        cgpa.textProperty().addListener((obs, oldValue, newValue) -> validateCgpaField(cgpa, cgpaError, newValue));
        year.textProperty().addListener((obs, oldValue, newValue) -> validateYearField(year, yearError, newValue));

        Button edit = new Button("Edit");
        styleHover(edit, SECONDARY_BUTTON_STYLE, SECONDARY_BUTTON_HOVER_STYLE);
        Button save = new Button("Save");
        save.setStyle(PRIMARY_BUTTON_STYLE);
        Button cancel = new Button("Cancel");
        styleHover(cancel, SECONDARY_BUTTON_STYLE, SECONDARY_BUTTON_HOVER_STYLE);
        save.setDisable(true);
        cancel.setDisable(true);

        Runnable enterEditMode = () -> {
            for (TextField field : new TextField[]{fullName, branch, cgpa, year}) field.setDisable(false);
            edit.setDisable(true);
            save.setDisable(false);
            cancel.setDisable(false);
        };

        Runnable exitEditMode = () -> {
            for (TextField field : new TextField[]{fullName, branch, cgpa, year}) {
                field.setDisable(true);
                field.setStyle(FIELD_STYLE_NORMAL);
            }
            nameError.setText(""); cgpaError.setText(""); yearError.setText("");
            edit.setDisable(false);
            save.setDisable(true);
            cancel.setDisable(true);
        };

        edit.setOnAction(event -> enterEditMode.run());

        cancel.setOnAction(event -> {
            fullName.setText(student.getFullName());
            branch.setText(student.getBranch());
            cgpa.setText(student.getCgpa() == null ? "" : String.valueOf(student.getCgpa()));
            year.setText(student.getGraduationYear() == 0 ? "" : String.valueOf(student.getGraduationYear()));
            exitEditMode.run();
        });

        save.setOnAction(event -> {
            boolean valid = true;
            if (fullName.getText() == null || fullName.getText().isBlank()) {
                nameError.setText("Name cannot be empty");
                fullName.setStyle(FIELD_STYLE_ERROR);
                valid = false;
            } else {
                nameError.setText("");
                fullName.setStyle(FIELD_STYLE_NORMAL);
            }
            if (!validateCgpaField(cgpa, cgpaError, cgpa.getText())) valid = false;
            if (!validateYearField(year, yearError, year.getText())) valid = false;
            if (!valid) return;

            String previousFullName = student.getFullName();
            String previousBranch = student.getBranch();
            Double previousCgpa = student.getCgpa();
            int previousYear = student.getGraduationYear();
            try {
                student.setFullName(fullName.getText().trim());
                student.setBranch(branch.getText() == null || branch.getText().isBlank() ? null : branch.getText().trim());
                student.setCgpa(cgpa.getText().isBlank() ? null : Double.parseDouble(cgpa.getText().trim()));
                student.setGraduationYear(year.getText().isBlank() ? 0 : Integer.parseInt(year.getText().trim()));

                boolean saved = controller.saveProfile(student);
                if (saved) {
                    ViewFactory.info("Profile saved");
                    nameLabel.setText(student.getFullName());
                    subLabel.setText((student.getBranch() == null ? "" : student.getBranch())
                            + (student.getGraduationYear() == 0 ? "" : " · Class of " + student.getGraduationYear()));
                    exitEditMode.run();
                } else {
                    student.setFullName(previousFullName);
                    student.setBranch(previousBranch);
                    student.setCgpa(previousCgpa);
                    student.setGraduationYear(previousYear);
                }
            } catch (NumberFormatException e) {
                ViewFactory.error("CGPA and Graduation Year must be numbers");
                student.setFullName(previousFullName);
                student.setBranch(previousBranch);
                student.setCgpa(previousCgpa);
                student.setGraduationYear(previousYear);
            }
        });

        HBox buttons = new HBox(10, edit, save, cancel);

        VBox card = new VBox(0, header, sectionLabel, form, buttons);
        card.setStyle(CARD_STYLE);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle(TAB_ROOT_STYLE);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private VBox skills() {
        Label sectionLabel = new Label("Skills");
        sectionLabel.setStyle(SECTION_LABEL_STYLE);

        TableView<Skill> table = new TableView<>();
        TableColumn<Skill, String> nameColumn = new TableColumn<>("Skill Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(220);
        TableColumn<Skill, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryColumn.setPrefWidth(220);
        table.getColumns().addAll(nameColumn, categoryColumn);

        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(controller.loadSkills()));
        refresh.run();

        TextField name = new TextField(); name.setPromptText("Skill name"); name.setPrefWidth(200); name.setPrefHeight(34); name.setStyle(FIELD_STYLE_NORMAL);
        TextField category = new TextField(); category.setPromptText("Category"); category.setPrefWidth(200); category.setPrefHeight(34); category.setStyle(FIELD_STYLE_NORMAL);

        Button add = new Button("Add Skill"); add.setStyle(PRIMARY_BUTTON_STYLE);
        Button edit = new Button("Edit Skill"); edit.setDisable(true);
        Button delete = new Button("Delete Skill"); delete.setDisable(true);
        styleHover(edit, SECONDARY_BUTTON_STYLE, SECONDARY_BUTTON_HOVER_STYLE);
        styleHover(delete, DANGER_BUTTON_STYLE, DANGER_BUTTON_HOVER_STYLE);

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            boolean hasSelection = value != null;
            edit.setDisable(!hasSelection);
            delete.setDisable(!hasSelection);
            if (hasSelection) { name.setText(value.getName()); category.setText(value.getCategory()); }
        });

        add.setOnAction(event -> {
            if (controller.addSkill(name.getText(), category.getText())) {
                name.clear(); category.clear(); table.getSelectionModel().clearSelection(); refresh.run();
            }
        });
        edit.setOnAction(event -> {
            Skill selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            String previousName = selected.getName(); String previousCategory = selected.getCategory();
            selected.setName(name.getText()); selected.setCategory(category.getText());
            if (controller.updateSkill(selected)) {
                name.clear(); category.clear(); table.getSelectionModel().clearSelection(); refresh.run();
            } else {
                selected.setName(previousName); selected.setCategory(previousCategory);
            }
        });
        delete.setOnAction(event -> {
            Skill selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && ViewFactory.confirm("Delete skill \"" + selected.getName() + "\"?")) {
                controller.deleteSkill(selected.getId());
                refresh.run();
            }
        });

        VBox card = new VBox(14, sectionLabel, table, new HBox(10, name, category, add, edit, delete));
        card.setStyle(CARD_STYLE);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle(TAB_ROOT_STYLE);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private VBox certifications() {
        Label sectionLabel = new Label("Certifications");
        sectionLabel.setStyle(SECTION_LABEL_STYLE);

        TableView<Certification> table = new TableView<>();
        TableColumn<Certification, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        titleColumn.setPrefWidth(200);
        TableColumn<Certification, String> issuerColumn = new TableColumn<>("Issuer");
        issuerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssuingOrganization()));
        issuerColumn.setPrefWidth(200);
        TableColumn<Certification, String> dateColumn = new TableColumn<>("Issue Date");
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getIssueDate() == null ? "" : data.getValue().getIssueDate().toString()));
        dateColumn.setPrefWidth(140);
        table.getColumns().addAll(titleColumn, issuerColumn, dateColumn);

        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(controller.loadCertifications()));
        refresh.run();

        TextField name = new TextField(); name.setPromptText("Title"); name.setPrefWidth(180); name.setPrefHeight(34); name.setStyle(FIELD_STYLE_NORMAL);
        TextField issuer = new TextField(); issuer.setPromptText("Issuer"); issuer.setPrefWidth(180); issuer.setPrefHeight(34); issuer.setStyle(FIELD_STYLE_NORMAL);
        DatePicker issueDate = new DatePicker(); issueDate.setPromptText("Issue date"); issueDate.setPrefWidth(160); issueDate.setPrefHeight(34);

        Button add = new Button("Add"); add.setStyle(PRIMARY_BUTTON_STYLE);
        Button edit = new Button("Edit"); edit.setDisable(true);
        Button delete = new Button("Delete"); delete.setDisable(true);
        styleHover(edit, SECONDARY_BUTTON_STYLE, SECONDARY_BUTTON_HOVER_STYLE);
        styleHover(delete, DANGER_BUTTON_STYLE, DANGER_BUTTON_HOVER_STYLE);

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            boolean hasSelection = value != null;
            edit.setDisable(!hasSelection);
            delete.setDisable(!hasSelection);
            if (hasSelection) { name.setText(value.getName()); issuer.setText(value.getIssuingOrganization()); issueDate.setValue(value.getIssueDate()); }
        });

        add.setOnAction(event -> {
            Certification value = new Certification(0, 0, name.getText(), issuer.getText(), issueDate.getValue(), null, null);
            if (controller.saveCertification(value)) {
                name.clear(); issuer.clear(); issueDate.setValue(null); table.getSelectionModel().clearSelection(); refresh.run();
            }
        });
        edit.setOnAction(event -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            String previousName = selected.getName(); String previousIssuer = selected.getIssuingOrganization();
            selected.setName(name.getText()); selected.setIssuingOrganization(issuer.getText()); selected.setIssueDate(issueDate.getValue());
            if (controller.saveCertification(selected)) {
                name.clear(); issuer.clear(); issueDate.setValue(null); table.getSelectionModel().clearSelection(); refresh.run();
            } else {
                selected.setName(previousName); selected.setIssuingOrganization(previousIssuer);
            }
        });
        delete.setOnAction(event -> {
            Certification selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && ViewFactory.confirm("Delete certification \"" + selected.getName() + "\"?")) {
                controller.deleteCertification(selected.getId());
                refresh.run();
            }
        });

        VBox card = new VBox(14, sectionLabel, table, new HBox(10, name, issuer, issueDate, add, edit, delete));
        card.setStyle(CARD_STYLE);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle(TAB_ROOT_STYLE);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private VBox projects() {
        Label sectionLabel = new Label("Projects");
        sectionLabel.setStyle(SECTION_LABEL_STYLE);

        TableView<Project> table = new TableView<>();
        TableColumn<Project, String> nameColumn = new TableColumn<>("Project Name");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(180);
        TableColumn<Project, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        descriptionColumn.setPrefWidth(240);
        TableColumn<Project, String> stackColumn = new TableColumn<>("Technology Stack");
        stackColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTechnologyStack()));
        stackColumn.setPrefWidth(180);
        table.getColumns().addAll(nameColumn, descriptionColumn, stackColumn);

        Runnable refresh = () -> table.setItems(FXCollections.observableArrayList(controller.loadProjects()));
        refresh.run();

        TextField name = new TextField(); name.setPromptText("Project name"); name.setPrefWidth(160); name.setPrefHeight(34); name.setStyle(FIELD_STYLE_NORMAL);
        TextField stack = new TextField(); stack.setPromptText("Technology stack (e.g. Java, React)"); stack.setPrefWidth(200); stack.setPrefHeight(34); stack.setStyle(FIELD_STYLE_NORMAL);
        TextField url = new TextField(); url.setPromptText("Repository URL"); url.setPrefWidth(200); url.setPrefHeight(34); url.setStyle(FIELD_STYLE_NORMAL);
        TextArea description = new TextArea(); description.setPromptText("Description"); description.setPrefRowCount(3); description.setStyle(FIELD_STYLE_NORMAL);

        Button add = new Button("Add"); add.setStyle(PRIMARY_BUTTON_STYLE);
        Button edit = new Button("Edit"); edit.setDisable(true);
        Button delete = new Button("Delete"); delete.setDisable(true);
        styleHover(edit, SECONDARY_BUTTON_STYLE, SECONDARY_BUTTON_HOVER_STYLE);
        styleHover(delete, DANGER_BUTTON_STYLE, DANGER_BUTTON_HOVER_STYLE);

        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            boolean hasSelection = value != null;
            edit.setDisable(!hasSelection);
            delete.setDisable(!hasSelection);
            if (hasSelection) {
                name.setText(value.getName());
                description.setText(value.getDescription());
                stack.setText(value.getTechnologyStack());
                url.setText(value.getRepositoryUrl());
            }
        });

        add.setOnAction(event -> {
            Project value = new Project(0, 0, name.getText(), description.getText(), url.getText(), null, null, stack.getText());
            if (controller.saveProject(value)) {
                name.clear(); description.clear(); stack.clear(); url.clear(); table.getSelectionModel().clearSelection(); refresh.run();
            }
        });
        edit.setOnAction(event -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            String previousName = selected.getName(); String previousDescription = selected.getDescription();
            String previousStack = selected.getTechnologyStack(); String previousUrl = selected.getRepositoryUrl();
            selected.setName(name.getText()); selected.setDescription(description.getText());
            selected.setTechnologyStack(stack.getText()); selected.setRepositoryUrl(url.getText());
            if (controller.saveProject(selected)) {
                name.clear(); description.clear(); stack.clear(); url.clear(); table.getSelectionModel().clearSelection(); refresh.run();
            } else {
                selected.setName(previousName); selected.setDescription(previousDescription);
                selected.setTechnologyStack(previousStack); selected.setRepositoryUrl(previousUrl);
            }
        });
        delete.setOnAction(event -> {
            Project selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && ViewFactory.confirm("Delete project \"" + selected.getName() + "\"?")) {
                controller.deleteProject(selected.getId());
                refresh.run();
            }
        });

        VBox card = new VBox(14, sectionLabel, table, new HBox(10, name, stack, url, add, edit, delete), description);
        card.setStyle(CARD_STYLE);
        VBox wrapper = new VBox(card);
        wrapper.setPadding(new Insets(16));
        wrapper.setStyle(TAB_ROOT_STYLE);
        return wrapper;
    }
}