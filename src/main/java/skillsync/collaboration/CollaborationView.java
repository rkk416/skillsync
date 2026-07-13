package skillsync.collaboration;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import skillsync.model.Team;
import skillsync.model.User;
import skillsync.model.Student;
import skillsync.repository.TeamRepository;
import skillsync.utils.ViewFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public final class CollaborationView extends VBox {
    private final CollaborationController controller = new CollaborationController();
    private final ListView<Team> teams = new ListView<>();
    private final ListView<CollaborationController.StudentSuggestion> suggestionsList = new ListView<>();
    private final ListView<CollaborationController.StudentSuggestion> discoveryList = new ListView<>();
    
    // Beautiful, responsive empty state container for Recommended Teammates
    private final VBox emptyRecsContainer = new VBox(14);
    private final Label noStudentsLabel = new Label("No students available.");
    
    private final VBox recCard = new VBox(12);
    private final VBox discoveryCard = new VBox(12);

    private final TextField searchTeamsField = new TextField();
    private final TextField searchDiscoveryField = new TextField();
    private final ChoiceBox<String> teamSortChoice = new ChoiceBox<>();
    private final ChoiceBox<String> studentSortChoice = new ChoiceBox<>();
    private final ChoiceBox<String> teamFilterChoice = new ChoiceBox<>();
    private final ChoiceBox<String> studentFilterChoice = new ChoiceBox<>();

    // Real-time search result counters
    private final Label teamResultCounter = new Label("Showing 0 teams");
    private final Label discoveryResultCounter = new Label("Showing 0 students");

    private final List<Team> allTeamsList = new ArrayList<>();
    private final List<CollaborationController.StudentSuggestion> allDiscoveryList = new ArrayList<>();

    private final Label activeTeamsStat = new Label("0");
    private final Label myTeamsStat = new Label("0");
    private final Label studentsAvailableStat = new Label("0");
    private final Label recTeammatesStat = new Label("0");
    private final Label pendingRequestsStat = new Label("0");

    // Inline Toast/Notification Banner at the top of the workspace
    private final HBox notificationBanner = new HBox(12);
    private final Label notificationMsg = new Label();
    private final Label lastRefreshedLabel = new Label("Last updated: Just now");

    // Live validation labels
    private final Label nameValidationLabel = new Label();
    private final Label domainValidationLabel = new Label();

    // Collaboration Insights Container
    private final VBox insightsCard = new VBox(14);

    public CollaborationView() {
        setSpacing(20);
        setPadding(new Insets(10));


        // Inline Toast Notification Setup (hidden by default)
        notificationBanner.setVisible(false);
        notificationBanner.setManaged(false);
        notificationBanner.setStyle("-fx-background-color: rgba(34,197,94,0.15); -fx-border-color: #22C55E; -fx-border-width: 1.5; -fx-background-radius: 8; -fx-padding: 12 18; -fx-alignment: CENTER_LEFT;");
        notificationMsg.setStyle("-fx-text-fill: #111827; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        notificationBanner.getChildren().add(notificationMsg);

        // Subtitle + Refresh Button Row
        HBox headerRow = new HBox();
        headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
        
        VBox titleArea = new VBox(4);
        Label subtitle = new Label("Collaborate with other students, build project teams, and find recommended teammates.");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-font-style: italic;");
        
        lastRefreshedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-opacity: 0.7;");
        titleArea.getChildren().addAll(subtitle, lastRefreshedLabel);
        
        Button refreshBtn = new Button("ðŸ”„ Refresh Data");
        refreshBtn.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #2563EB); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
        Tooltip.install(refreshBtn, new Tooltip("Refresh all statistics, project teams, and student recommendations"));
        
        // Micro-interaction on Refresh Button
        refreshBtn.setOnMouseEntered(e -> {
            refreshBtn.setStyle("-fx-background-color: linear-gradient(to right, #3B82F6, #3F83F8); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px; -fx-effect: dropshadow(three-pass-box, rgba(124,58,237,0.35), 10, 0, 0, 1);");
            ScaleTransition st = new ScaleTransition(Duration.millis(80), refreshBtn);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        refreshBtn.setOnMouseExited(e -> {
            refreshBtn.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #2563EB); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
            ScaleTransition st = new ScaleTransition(Duration.millis(80), refreshBtn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        
        refreshBtn.setOnAction(e -> {
            javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(Duration.millis(600), refreshBtn);
            rt.setByAngle(360);
            rt.play();
            refresh();
            showNotification("Collaboration data refreshed successfully!", false);
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(titleArea, spacer, refreshBtn);

        // Premium Collaboration Statistics section (with colored left accent borders and hover rise)
        HBox statsBar = new HBox(12);
        statsBar.setStyle("-fx-alignment: CENTER;");
        statsBar.getChildren().addAll(
            createStatCard("Active Teams", activeTeamsStat, "#2563EB", "Total in platform", "ðŸ‘¥"),
            createStatCard("My Teams", myTeamsStat, "#2563EB", "Created or joined", "ðŸ‘‘"),
            createStatCard("Available Peers", studentsAvailableStat, "#22C55E", "Network candidates", "ðŸŽ“"),
            createStatCard("Recommended", recTeammatesStat, "#F59E0B", "Best matches (BFS)", "âœ¨"),
            createStatCard("Pending Requests", pendingRequestsStat, "#EF4444", "Connections pending", "ðŸ“©")
        );

        statsBar.setMaxWidth(Double.MAX_VALUE);

        // 1. LEFT SIDE: Team Creation Form & Teams List
        VBox leftColumn = new VBox(20);
        leftColumn.setMinWidth(280);
        leftColumn.setPrefWidth(0);
        leftColumn.setFillWidth(true);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Create Team Card
        VBox createCard = new VBox(12);
        createCard.setMaxWidth(Double.MAX_VALUE);
        createCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        VBox.setVgrow(createCard, Priority.NEVER);

        Label createTitle = new Label("+ Create New Team");
        createTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        VBox nameGroup = new VBox(6);
        HBox nameLabelRow = new HBox(6);
        Label nameLabel = new Label("Team Name");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        Region nameSpacer = new Region();
        HBox.setHgrow(nameSpacer, Priority.ALWAYS);
        Label charCounter = new Label("0 / 30 characters");
        charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
        nameLabelRow.getChildren().addAll(nameLabel, nameSpacer, charCounter);

        TextField nameField = new TextField(); 
        nameField.setPromptText("Enter a unique team name");
        styleTextField(nameField);
        Tooltip.install(nameField, new Tooltip("Choose a distinct, professional name for your capstone project team"));
        
        nameValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        nameValidationLabel.setVisible(false);
        nameValidationLabel.setManaged(false);

        // Character counter and input validation styling
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int len = newVal.trim().length();
                charCounter.setText(len + " / 30 characters");
                if (len == 0) {
                    charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF;");
                    nameValidationLabel.setText("âš ï¸ Team name is required.");
                    nameValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    nameValidationLabel.setVisible(true);
                    nameValidationLabel.setManaged(true);
                } else if (len > 30) {
                    charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    nameValidationLabel.setText("âŒ Exceeds maximum 30 character limit.");
                    nameValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    nameValidationLabel.setVisible(true);
                    nameValidationLabel.setManaged(true);
                } else {
                    charCounter.setStyle("-fx-font-size: 10px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
                    nameValidationLabel.setText("âœ“ Team name is valid.");
                    nameValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981; -fx-font-weight: bold;");
                    nameValidationLabel.setVisible(true);
                    nameValidationLabel.setManaged(true);
                }
            }
        });

        nameGroup.getChildren().addAll(nameLabelRow, nameField, nameValidationLabel);

        VBox domainGroup = new VBox(6);
        Label domainLabel = new Label("Project Domain / Description");
        domainLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
        TextField domainField = new TextField(); 
        domainField.setPromptText("e.g. Web Development, Machine Learning, Mobile App");
        styleTextField(domainField);
        Tooltip.install(domainField, new Tooltip("Specify the primary engineering domain or technology stack"));

        domainValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-font-weight: bold;");
        domainValidationLabel.setVisible(false);
        domainValidationLabel.setManaged(false);

        domainField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int len = newVal.trim().length();
                if (len == 0) {
                    domainValidationLabel.setText("âš ï¸ Project domain description is required.");
                    domainValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    domainValidationLabel.setVisible(true);
                    domainValidationLabel.setManaged(true);
                } else {
                    domainValidationLabel.setText("âœ“ Description is valid.");
                    domainValidationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #22C55E; -fx-font-weight: bold;");
                    domainValidationLabel.setVisible(true);
                    domainValidationLabel.setManaged(true);
                }
            }
        });

        Label domainHelper = new Label("â€¢ Or select one of these popular suggested domains:");
        domainHelper.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
        
        // Interactive suggestion chips
        FlowPane domainChips = new FlowPane();
        domainChips.setHgap(6);
        domainChips.setVgap(6);
        String[] popularDomains = {"Web Dev", "Machine Learning", "Mobile App", "Data Science", "Cybersecurity"};
        for (String dom : popularDomains) {
            Label chip = new Label(dom);
            chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
            chip.setOnMouseEntered(e -> {
                chip.setStyle("-fx-background-color: #3F4756; -fx-text-fill: #111827; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #D1D5DB; -fx-border-width: 1; -fx-border-radius: 12;");
                ScaleTransition st = new ScaleTransition(Duration.millis(50), chip);
                st.setToX(1.04);
                st.setToY(1.04);
                st.play();
            });
            chip.setOnMouseExited(e -> {
                chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
                ScaleTransition st = new ScaleTransition(Duration.millis(50), chip);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
            chip.setOnMouseClicked(e -> {
                domainField.setText(dom);
                showNotification("Domain set to '" + dom + "'", false);
            });
            domainChips.getChildren().add(chip);
        }

        domainGroup.getChildren().addAll(domainLabel, domainField, domainHelper, domainChips, domainValidationLabel);

        Button createButton = ViewFactory.primaryButton("Create Team");
        createButton.setMinWidth(140);
        createButton.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #2563EB); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
        applyBtnHover(createButton, "linear-gradient(to right, #2563EB, #2563EB)", "linear-gradient(to right, #3B82F6, #3F83F8)");
        
        createButton.setOnAction(event -> {
            String name = nameField.getText();
            String domain = domainField.getText();
            if (name == null || name.trim().isBlank()) {
                ViewFactory.error("Team name is required");
                return;
            }
            if (name.trim().length() > 30) {
                ViewFactory.error("Team name cannot exceed 30 characters");
                return;
            }
            if (domain == null || domain.trim().isBlank()) {
                ViewFactory.error("Project domain is required");
                return;
            }
            createButton.setDisable(true);
            try {
                controller.createTeam(name.trim(), domain.trim());
                nameField.clear();
                domainField.clear();
                nameValidationLabel.setVisible(false);
                nameValidationLabel.setManaged(false);
                domainValidationLabel.setVisible(false);
                domainValidationLabel.setManaged(false);
                showNotification("ðŸŽ‰ Project Team '" + name.trim() + "' created successfully!", false);
                refresh();
            } catch (RuntimeException e) {
                ViewFactory.error(e.getMessage());
            } finally {
                createButton.setDisable(false);
            }
        });

        HBox btnRow = new HBox(createButton);
        btnRow.setStyle("-fx-alignment: CENTER_RIGHT;");

        createCard.getChildren().addAll(createTitle, nameGroup, domainGroup, btnRow);

        // Available Teams Card
        VBox teamsCard = new VBox(12);
        teamsCard.setMaxWidth(Double.MAX_VALUE);
        teamsCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        VBox.setVgrow(teamsCard, Priority.ALWAYS);
        
        HBox teamsHeaderBox = new HBox(8);
        teamsHeaderBox.setStyle("-fx-alignment: CENTER_LEFT;");
        Label teamsTitle = new Label("ðŸ‘¥ Active Teams");
        teamsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region teamsHeaderSpacer = new Region();
        HBox.setHgrow(teamsHeaderSpacer, Priority.ALWAYS);
        
        teamResultCounter.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        teamsHeaderBox.getChildren().addAll(teamsTitle, teamsHeaderSpacer, teamResultCounter);

        // Search & Sorting & Filtering controls Row
        HBox teamControlRow = new HBox(8);
        searchTeamsField.setPromptText("ðŸ” Search teams by name or domain...");
        styleTextField(searchTeamsField);
        searchTeamsField.textProperty().addListener((observable, oldValue, newValue) -> applyTeamFilteringAndSorting());
        
        teamSortChoice.setItems(FXCollections.observableArrayList("Sort: Default", "Name (A-Z)", "Members (High-Low)", "Members (Low-High)"));
        teamSortChoice.setValue("Sort: Default");
        styleChoiceBox(teamSortChoice);
        teamSortChoice.setOnAction(e -> applyTeamFilteringAndSorting());

        teamFilterChoice.setItems(FXCollections.observableArrayList("Filter: All Teams", "Filter: Open (Has slots)", "Filter: Full", "Filter: My Teams"));
        teamFilterChoice.setValue("Filter: All Teams");
        styleChoiceBox(teamFilterChoice);
        teamFilterChoice.setOnAction(e -> applyTeamFilteringAndSorting());
        
        teamControlRow.getChildren().addAll(searchTeamsField, teamFilterChoice, teamSortChoice);
        HBox.setHgrow(searchTeamsField, Priority.ALWAYS);

        teams.setPrefHeight(240);
        teams.setMinHeight(160);
        VBox.setVgrow(teams, Priority.ALWAYS);
        teams.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-control-inner-background: transparent;");
        teams.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                if (empty || team == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 12 0; -fx-background-insets: 0;");
                    
                    VBox card = new VBox(8);
                    
                    // Determine status first
                    boolean isMember = controller.isStudentInTeam(team.getId());
                    boolean isOwner = team.getCreatedBy() == controller.getCurrentStudentId();
                    List<String> memberNames = controller.getTeamMemberNames(team.getId());
                    int memberCount = memberNames.size();
                    
                    String borderCol = isOwner ? "#2563EB" : (isMember ? "#22C55E" : "#E5E7EB");
                    card.setStyle("-fx-background-color: white; -fx-padding: 14; -fx-background-radius: 12; -fx-border-color: " + borderCol + "; -fx-border-width: 1.5;");
                    
                    // Premium Smooth Hover Lift and DropShadow Transition
                    applyCardHoverAnimation(card, borderCol);

                    Label nameLabel = new Label("ðŸ‘¥ " + team.getName());
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                    
                    Label domainLabel = new Label("Domain: " + (team.getDescription() == null || team.getDescription().isBlank() ? "General" : team.getDescription()));
                    domainLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-style: italic;");
                    
                    // Owner Initials Avatar & Creator Label
                    String creatorName = controller.getStudentName(team.getCreatedBy());
                    HBox creatorRow = new HBox(6);
                    creatorRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    Label creatorAvatar = new Label(getInitials(creatorName));
                    creatorAvatar.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-font-weight: bold; -fx-font-size: 9px; -fx-alignment: CENTER; -fx-pref-width: 18; -fx-pref-height: 18; -fx-background-radius: 9; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 9;");
                    
                    Label creatorLabel = new Label("Created By: " + creatorName);
                    creatorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                    creatorRow.getChildren().addAll(creatorAvatar, creatorLabel);
                    
                    Label membersCountLabel = new Label("Capacity: " + memberCount + " / 5 Members");
                    membersCountLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                    
                    // Responsive Progress Track for Capacity using native ProgressBar styled beautifully
                    ProgressBar progressBar = new ProgressBar((double) memberCount / 5.0);
                    progressBar.setMaxHeight(6);
                    progressBar.setMaxWidth(Double.MAX_VALUE);
                    String barColor = memberCount >= 5 ? "-fx-accent: #EF4444;" : (memberCount >= 4 ? "-fx-accent: #F59E0B;" : "-fx-accent: #22C55E;");
                    progressBar.setStyle("-fx-background-radius: 3; -fx-border-radius: 3; -fx-padding: 0; -fx-control-inner-background: #F8FAFC; " + barColor);
                    
                    Label membersLabel = new Label("Members: " + (memberNames.isEmpty() ? "None" : String.join(", ", memberNames)));
                    membersLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
                    membersLabel.setWrapText(true);
                    
                    // Team Status Badge
                    String statusText;
                    String statusColor;
                    String statusBg;
                    if (isOwner) {
                        statusText = "Owner";
                        statusColor = "#FFFFFF";
                        statusBg = "#2563EB";
                    } else if (isMember) {
                        statusText = "Joined";
                        statusColor = "#FFFFFF";
                        statusBg = "#22C55E";
                    } else if (memberCount >= 5) {
                        statusText = "Full";
                        statusColor = "#FFFFFF";
                        statusBg = "#EF4444";
                    } else {
                        statusText = "Open";
                        statusColor = "#FFFFFF";
                        statusBg = "#2563EB";
                    }
                    
                    Label statusBadge = new Label(statusText);
                    statusBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + "; -fx-background-color: " + statusBg + "; -fx-padding: 3 8; -fx-background-radius: 10;");
                    
                    HBox headerRow = new HBox(10, nameLabel, statusBadge);
                    headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    // Action Buttons with Confirmations
                    Button joinBtn = new Button("Join Team");
                    if (isOwner) {
                        joinBtn.setText("Lead / Owner");
                        joinBtn.setDisable(true);
                        joinBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #9CA3AF; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-min-height: 32;");
                    } else if (isMember) {
                        joinBtn.setText("Already Joined");
                        joinBtn.setDisable(true);
                        joinBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #9CA3AF; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-min-height: 32;");
                    } else if (memberCount >= 5) {
                        joinBtn.setText("Team Full");
                        joinBtn.setDisable(true);
                        joinBtn.setStyle("-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #EF4444; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-min-height: 32;");
                    } else {
                        joinBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-height: 32;");
                        applyBtnHover(joinBtn, "#2563EB", "#1D4ED8");
                        joinBtn.setOnAction(event -> {
                            boolean confirmed = showConfirmation(
                                "Join Project Team",
                                "Request to Join Team",
                                "Are you sure you want to join '" + team.getName() + "'?\n\nYou will be added to the official member roster for this project."
                            );
                            if (confirmed) {
                                try {
                                    controller.joinTeam(team.getId());
                                    showNotification("Successfully joined project team '" + team.getName() + "'!", false);
                                    refresh();
                                } catch (RuntimeException e) {
                                    ViewFactory.error(e.getMessage());
                                }
                            }
                        });
                    }
                    
                    Button viewBtn = new Button("View Details");
                    viewBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-cursor: hand; -fx-min-height: 32; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 6;");
                    applyBtnHover(viewBtn, "#F8FAFC", "#3F4756");
                    viewBtn.setOnAction(event -> showTeamDetailsDialog(team));
                    
                    // Quick Clipboard action
                    Button copyBtn = new Button("ðŸ“‹ Copy ID");
                    copyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-padding: 6 10; -fx-cursor: hand; -fx-min-height: 32;");
                    copyBtn.setOnAction(e -> {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(String.valueOf(team.getId()));
                        clipboard.setContent(content);
                        showNotification("Team ID " + team.getId() + " copied to clipboard!", false);
                    });

                    HBox actionRow = new HBox(8, joinBtn, viewBtn, copyBtn);
                    actionRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");
                    
                    card.getChildren().addAll(headerRow, domainLabel, creatorRow, membersCountLabel, progressBar, membersLabel, actionRow);
                    setGraphic(card);
                    setText(null);
                }
            }
        });

        // Double Click Micro-interaction on Teams List
        teams.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Team selected = teams.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showTeamDetailsDialog(selected);
                }
            }
        });

        teamsCard.getChildren().addAll(teamsHeaderBox, teamControlRow, teams);
        leftColumn.getChildren().addAll(createCard, teamsCard);

        // 2. RIGHT SIDE: Recommended Teammates & Student Discovery & Insights
        VBox rightColumn = new VBox(20);
        rightColumn.setMinWidth(280);
        rightColumn.setPrefWidth(0);
        rightColumn.setFillWidth(true);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        // A. Recommended Teammates Card (BFS)
        recCard.setMaxWidth(Double.MAX_VALUE);
        recCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        VBox.setVgrow(recCard, Priority.ALWAYS);
        
        Label recTitle = new Label("â­ Recommended Teammates");
        recTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        suggestionsList.setPrefHeight(180);
        suggestionsList.setMinHeight(120);
        VBox.setVgrow(suggestionsList, Priority.ALWAYS);
        suggestionsList.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-control-inner-background: transparent;");
        suggestionsList.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CollaborationController.StudentSuggestion suggestion, boolean empty) {
                super.updateItem(suggestion, empty);
                if (empty || suggestion == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 12 0; -fx-background-insets: 0;");
                    
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1.5;");
                    
                    // Smooth hover lift animation and shadows
                    applyCardHoverAnimation(card, "#E5E7EB");

                    HBox titleRow = new HBox(8);
                    titleRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    Label avatar = new Label(getInitials(suggestion.name()));
                    avatar.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-font-weight: bold; -fx-font-size: 11px; -fx-alignment: CENTER; -fx-pref-width: 24; -fx-pref-height: 24; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
                    
                    Label nameLabel = new Label(suggestion.name());
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                    
                    titleRow.getChildren().addAll(avatar, nameLabel);

                    // Match Strength Indicator
                    double matchPercentage = suggestion.matchScore() * 100;
                    Label matchLabel = new Label(String.format("%.0f%% Match Score", matchPercentage));
                    matchLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
                    
                    ProgressBar matchBar = new ProgressBar(suggestion.matchScore());
                    matchBar.setMaxHeight(5);
                    matchBar.setPrefWidth(90);
                    String matchColor = matchPercentage >= 70 ? "-fx-accent: #22C55E;" : "-fx-accent: #2563EB;";
                    matchBar.setStyle("-fx-background-radius: 3; -fx-border-radius: 3; -fx-padding: 0; -fx-control-inner-background: #F8FAFC; " + matchColor);
                    
                    HBox matchRow = new HBox(8, matchLabel, matchBar);
                    matchRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    // Shared skills flow pane
                    Label sharedTitleLabel = new Label("Shared Skills:");
                    sharedTitleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
                    
                    FlowPane sharedSkillsFlow = new FlowPane();
                    sharedSkillsFlow.setHgap(4);
                    sharedSkillsFlow.setVgap(4);
                    if (suggestion.sharedSkills().isEmpty()) {
                        Label noneLabel = new Label("None");
                        noneLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                        sharedSkillsFlow.getChildren().add(noneLabel);
                    } else {
                        for (String skill : suggestion.sharedSkills()) {
                            Label tag = new Label(skill);
                            tag.setStyle("-fx-background-color: rgba(34,197,94,0.12); -fx-text-fill: #22C55E; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold;");
                            sharedSkillsFlow.getChildren().add(tag);
                        }
                    }
                    
                    HBox sharedSkillsRow = new HBox(6, sharedTitleLabel, sharedSkillsFlow);
                    sharedSkillsRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    // Reason Badge
                    Label reasonBadge = new Label(suggestion.reason());
                    String reasonColor = "#FFFFFF";
                    String reasonBg = "#2563EB";
                    if ("Previous Team".equals(suggestion.reason())) {
                        reasonColor = "#FFFFFF";
                        reasonBg = "#F59E0B";
                    } else if ("Shared Skills".equals(suggestion.reason())) {
                        reasonColor = "#FFFFFF";
                        reasonBg = "#2563EB";
                    }
                    reasonBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + reasonColor + "; -fx-background-color: " + reasonBg + "; -fx-padding: 2 6; -fx-background-radius: 4;");
                    
                    HBox badgeRow = new HBox(6, new Label("Strength Link:") {{ setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;"); }}, reasonBadge);
                    badgeRow.setStyle("-fx-alignment: CENTER_LEFT;");

                    Button actionBtn = new Button();
                    boolean sent = controller.isRequestSent(suggestion.student().getId());
                    if (sent) {
                        actionBtn.setText("Connection Pending");
                        actionBtn.setDisable(true);
                        actionBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #9CA3AF; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-min-height: 32;");
                    } else {
                        actionBtn.setText("Send Connection");
                        actionBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-height: 32;");
                        applyBtnHover(actionBtn, "#2563EB", "#1D4ED8");
                        
                        actionBtn.setOnAction(event -> {
                            boolean confirmed = showConfirmation(
                                "Send Connection Request",
                                "Request Peer Collaboration",
                                "Do you want to send an official network connection request to " + suggestion.name() + "?"
                            );
                            if (confirmed) {
                                try {
                                    controller.sendRequest(suggestion.student().getId());
                                    showNotification("Connection request sent to " + suggestion.name() + "!", false);
                                    refresh();
                                } catch (RuntimeException e) {
                                    ViewFactory.error(e.getMessage());
                                }
                            }
                        });
                    }
                    
                    HBox actionRow = new HBox(12, matchRow, actionBtn);
                    actionRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");
                    
                    card.getChildren().addAll(titleRow, sharedSkillsRow, badgeRow, actionRow);
                    setGraphic(card);
                    setText(null);
                }
            }
        });

        // Initialize a highly illustrative, informative Empty State card for Recommended Teammates
        emptyRecsContainer.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1.5; -fx-border-style: dashed; -fx-alignment: CENTER;");
        
        Label emptyIcon = new Label("ðŸ’¡");
        emptyIcon.setStyle("-fx-font-size: 32px;");
        
        Label emptyTitle = new Label("Unlock Teammate Recommendations");
        emptyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Label emptyDesc = new Label(
            "We use advanced match analysis (Jaccard Similarity and Network Paths) to match you with peers. Try these active steps:\n\n" +
            "â€¢ Add skills to your profile (e.g. Java, Python, SQL)\n" +
            "â€¢ Create or join active teams in the list\n" +
            "â€¢ Send connections to other students in Discovery"
        );
        emptyDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-line-spacing: 4; -fx-text-alignment: CENTER;");
        emptyDesc.setWrapText(true);
        emptyRecsContainer.getChildren().addAll(emptyIcon, emptyTitle, emptyDesc);

        recCard.getChildren().addAll(recTitle, suggestionsList);

        // B. Collaboration Insights & Tips Card (Premium SaaS feature)
        insightsCard.setMaxWidth(Double.MAX_VALUE);
        insightsCard.setStyle("-fx-background-color: rgba(37,99,235,0.05); -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.05), 8, 0, 0, 2);");
        VBox.setVgrow(insightsCard, Priority.NEVER);
        rebuildInsightsCard();

        // C. Student Discovery Card (No BFS, direct from database)
        discoveryCard.setMaxWidth(Double.MAX_VALUE);
        discoveryCard.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 3);");
        VBox.setVgrow(discoveryCard, Priority.ALWAYS);
        
        HBox discoveryHeaderBox = new HBox(8);
        discoveryHeaderBox.setStyle("-fx-alignment: CENTER_LEFT;");
        Label discoveryTitle = new Label("ðŸ” Student Discovery");
        discoveryTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        Region discoveryHeaderSpacer = new Region();
        HBox.setHgrow(discoveryHeaderSpacer, Priority.ALWAYS);
        
        discoveryResultCounter.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
        discoveryHeaderBox.getChildren().addAll(discoveryTitle, discoveryHeaderSpacer, discoveryResultCounter);

        // Discovery Search & Sort & Filter controls row
        HBox discoveryControlRow = new HBox(8);
        searchDiscoveryField.setPromptText("ðŸ” Search students by name, university, degree, or skill...");
        styleTextField(searchDiscoveryField);
        searchDiscoveryField.textProperty().addListener((observable, oldValue, newValue) -> applyStudentFilteringAndSorting());
        
        studentSortChoice.setItems(FXCollections.observableArrayList("Sort: Default", "Name (A-Z)", "Most Skills First", "Grad Year"));
        studentSortChoice.setValue("Sort: Default");
        styleChoiceBox(studentSortChoice);
        studentSortChoice.setOnAction(e -> applyStudentFilteringAndSorting());

        studentFilterChoice.setItems(FXCollections.observableArrayList("Filter: All Students", "Filter: Same University", "Filter: Same Degree", "Filter: With Skills"));
        studentFilterChoice.setValue("Filter: All Students");
        styleChoiceBox(studentFilterChoice);
        studentFilterChoice.setOnAction(e -> applyStudentFilteringAndSorting());
        
        discoveryControlRow.getChildren().addAll(searchDiscoveryField, studentFilterChoice, studentSortChoice);
        HBox.setHgrow(searchDiscoveryField, Priority.ALWAYS);

        discoveryList.setPrefHeight(200);
        discoveryList.setMinHeight(160);
        VBox.setVgrow(discoveryList, Priority.ALWAYS);
        discoveryList.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-control-inner-background: transparent;");
        discoveryList.setCellFactory(ignored -> new ListCell<>() {
            @Override
            protected void updateItem(CollaborationController.StudentSuggestion suggestion, boolean empty) {
                super.updateItem(suggestion, empty);
                if (empty || suggestion == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 12 0; -fx-background-insets: 0;");
                    
                    VBox card = new VBox(8);
                    card.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1.5;");
                    
                    // Smooth hover lift animation and shadows
                    applyCardHoverAnimation(card, "#E5E7EB");

                    HBox nameRow = new HBox(8);
                    nameRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    // Colorful circular initials bubble based on student ID hash
                    String[] avatarColors = {"#283593", "#3949AB", "#10B981", "#14B8A6", "#F59E0B", "#EF4444"};
                    String avatarColor = avatarColors[Math.abs(suggestion.student().getId()) % avatarColors.length];
                    
                    Label avatarLabel = new Label(getInitials(suggestion.name()));
                    avatarLabel.setStyle("-fx-background-color: " + avatarColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-pref-width: 26; -fx-pref-height: 26; -fx-background-radius: 13;");
                    
                    Label nameLabel = new Label(suggestion.name());
                    nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                    
                    // Compact Copy Student ID Action
                    Button copyStudentIdBtn = new Button("ID: " + suggestion.student().getId());
                    copyStudentIdBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-cursor: hand; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 4;");
                    copyStudentIdBtn.setOnAction(e -> {
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        ClipboardContent content = new ClipboardContent();
                        content.putString(String.valueOf(suggestion.student().getId()));
                        clipboard.setContent(content);
                        showNotification("Student ID " + suggestion.student().getId() + " copied to clipboard!", false);
                    });

                    Region nameRowSpacer = new Region();
                    HBox.setHgrow(nameRowSpacer, Priority.ALWAYS);

                    nameRow.getChildren().addAll(avatarLabel, nameLabel, nameRowSpacer, copyStudentIdBtn);
                    
                    Label degreeLabel = new Label("ðŸŽ“ Degree: " + (suggestion.student().getDegree() == null || suggestion.student().getDegree().isBlank() ? "N/A" : suggestion.student().getDegree()));
                    degreeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                    
                    Label universityLabel = new Label("University: " + (suggestion.student().getUniversity() == null || suggestion.student().getUniversity().isBlank() ? "N/A" : suggestion.student().getUniversity()));
                    universityLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                    
                    Label skillsTitleLabel = new Label("Skills:");
                    skillsTitleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
                    
                    FlowPane skillsFlow = new FlowPane();
                    skillsFlow.setHgap(4);
                    skillsFlow.setVgap(4);
                    if (suggestion.skills().isEmpty()) {
                        Label noneLabel = new Label("None listed");
                        noneLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
                        skillsFlow.getChildren().add(noneLabel);
                    } else {
                        for (String skill : suggestion.skills()) {
                            Label tag = new Label(skill);
                            tag.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: bold; -fx-border-color: rgba(124,58,237,0.15); -fx-border-width: 1; -fx-border-radius: 4;");
                            skillsFlow.getChildren().add(tag);
                        }
                    }
                    
                    HBox skillsRow = new HBox(6, skillsTitleLabel, skillsFlow);
                    skillsRow.setStyle("-fx-alignment: CENTER_LEFT;");
                    
                    Button viewProfileBtn = new Button("View Profile");
                    viewProfileBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-cursor: hand; -fx-min-height: 32; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 6;");
                    applyBtnHover(viewProfileBtn, "#F8FAFC", "#3F4756");
                    viewProfileBtn.setOnAction(event -> showStudentProfileDialog(suggestion));

                    Button recommendBtn = new Button();
                    boolean recommended = controller.isStudentRecommended(suggestion.student().getId());
                    if (recommended) {
                        recommendBtn.setText("Recommended");
                        recommendBtn.setDisable(true);
                        recommendBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #9CA3AF; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-min-height: 32;");
                    } else {
                        recommendBtn.setText("Recommend");
                        recommendBtn.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-min-height: 32;");
                        applyBtnHover(recommendBtn, "#22C55E", "#15803D");
                        recommendBtn.setOnAction(event -> {
                            boolean confirmed = showConfirmation(
                                "Recommend Peer",
                                "Submit Peer Recommendation",
                                "Do you want to recommend " + suggestion.name() + " for outstanding collaboration?"
                            );
                            if (confirmed) {
                                try {
                                    controller.recommendStudent(suggestion.student().getId());
                                    showNotification("Successfully submitted recommendation for " + suggestion.name() + "!", false);
                                    refresh();
                                } catch (RuntimeException e) {
                                    ViewFactory.error(e.getMessage());
                                }
                            }
                        });
                    }
                    
                    HBox actionRow = new HBox(8, viewProfileBtn, recommendBtn);
                    actionRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 0 0 0;");
                    
                    card.getChildren().addAll(nameRow, degreeLabel, universityLabel, skillsRow, actionRow);
                    setGraphic(card);
                    setText(null);
                }
            }
        });

        // Double Click Micro-interaction on Discovery List
        discoveryList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                CollaborationController.StudentSuggestion selected = discoveryList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showStudentProfileDialog(selected);
                }
            }
        });

        discoveryCard.getChildren().addAll(discoveryHeaderBox, discoveryControlRow, discoveryList);
        rightColumn.getChildren().addAll(recCard, insightsCard, discoveryCard);

        HBox mainLayout = new HBox(14, leftColumn, rightColumn);
        mainLayout.setMaxWidth(Double.MAX_VALUE);
        mainLayout.setFillHeight(true);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        noStudentsLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-font-style: italic; -fx-padding: 10;");

        refresh();

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox overallLayout = new VBox(14, notificationBanner, headerRow, statsBar, scrollPane);
        overallLayout.setPadding(new Insets(10, 0, 10, 0));
        overallLayout.setMaxWidth(Double.MAX_VALUE);
        overallLayout.setMaxHeight(Double.MAX_VALUE);
        overallLayout.setStyle("-fx-background-color: #F8FAFC;");
        VBox.setVgrow(overallLayout, Priority.ALWAYS);

        BorderPane shell = ViewFactory.shell("Collaboration", overallLayout);
        shell.setStyle("-fx-font-family: 'Segoe UI';");
        VBox.setVgrow(shell, Priority.ALWAYS);

        
        // Ensure the center panel background matches the light theme
        if (shell.getCenter() instanceof VBox) {
            VBox centerVBox = (VBox) shell.getCenter();
            centerVBox.setStyle("-fx-background-color: #F8FAFC;");
        }


        getChildren().add(shell);
    }



    private void rebuildInsightsCard() {
        insightsCard.getChildren().clear();
        
        Label insightsTitle = new Label("ðŸ“Š Collaboration Insights");
        insightsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #14B8A6;");
        
        Label insightsDesc = new Label("Dynamic network analytics aggregated from across the SkillSync peer database.");
        insightsDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
        insightsDesc.setWrapText(true);

        insightsCard.getChildren().addAll(insightsTitle, insightsDesc);

        // 1. Calculate most popular skills dynamically from loaded student list
        if (!allDiscoveryList.isEmpty()) {
            Map<String, Integer> skillCounts = new HashMap<>();
            for (CollaborationController.StudentSuggestion s : allDiscoveryList) {
                for (String sk : s.skills()) {
                    skillCounts.put(sk, skillCounts.getOrDefault(sk, 0) + 1);
                }
            }
            
            List<Map.Entry<String, Integer>> sortedSkills = skillCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(Collectors.toList());
            
            if (!sortedSkills.isEmpty()) {
                VBox skillsBox = new VBox(4);
                Label skillLabel = new Label("ðŸ”¥ Most Requested Skills (Click to Filter):");
                skillLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
                
                FlowPane skillsChips = new FlowPane(4, 4);
                for (Map.Entry<String, Integer> entry : sortedSkills) {
                    Label chip = new Label(entry.getKey() + " (" + entry.getValue() + ")");
                    chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(124,58,237,0.15); -fx-border-width: 1; -fx-border-radius: 4;");
                    chip.setOnMouseEntered(e -> chip.setStyle("-fx-background-color: #3F4756; -fx-text-fill: #3B82F6; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(124,58,237,0.3); -fx-border-width: 1; -fx-border-radius: 4;"));
                    chip.setOnMouseExited(e -> chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(124,58,237,0.15); -fx-border-width: 1; -fx-border-radius: 4;"));
                    chip.setOnMouseClicked(e -> {
                        searchDiscoveryField.setText(entry.getKey());
                        showNotification("Filtering students by skill: " + entry.getKey(), false);
                    });
                    skillsChips.getChildren().add(chip);
                }
                skillsBox.getChildren().addAll(skillLabel, skillsChips);
                insightsCard.getChildren().add(skillsBox);
            }
        }

        // 2. Calculate trending domains from active teams
        if (!allTeamsList.isEmpty()) {
            Map<String, Integer> domainCounts = new HashMap<>();
            for (Team t : allTeamsList) {
                String d = (t.getDescription() == null || t.getDescription().isBlank()) ? "General" : t.getDescription().trim();
                domainCounts.put(d, domainCounts.getOrDefault(d, 0) + 1);
            }
            
            List<Map.Entry<String, Integer>> sortedDomains = domainCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(3)
                .collect(Collectors.toList());
            
            if (!sortedDomains.isEmpty()) {
                VBox domainsBox = new VBox(4);
                Label domainLabel = new Label("ðŸš€ Trending Project Domains (Click to Search):");
                domainLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
                
                FlowPane domainChips = new FlowPane(4, 4);
                for (Map.Entry<String, Integer> entry : sortedDomains) {
                    Label chip = new Label(entry.getKey());
                    chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #14B8A6; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(20,184,166,0.15); -fx-border-width: 1; -fx-border-radius: 4;");
                    chip.setOnMouseEntered(e -> chip.setStyle("-fx-background-color: #3F4756; -fx-text-fill: #2DD4BF; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 4;"));
                    chip.setOnMouseExited(e -> chip.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #14B8A6; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-size: 10px; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: rgba(20,184,166,0.15); -fx-border-width: 1; -fx-border-radius: 4;"));
                    chip.setOnMouseClicked(e -> {
                        searchTeamsField.setText(entry.getKey());
                        showNotification("Searching teams in domain: " + entry.getKey(), false);
                    });
                    domainChips.getChildren().add(chip);
                }
                domainsBox.getChildren().addAll(domainLabel, domainChips);
                insightsCard.getChildren().add(domainsBox);
            }
        }

        // 3. Jaccard similarity brief guide
        Label similarityLabel = new Label("ðŸ’¡ How recommendations work: We map previous student collaborations (BFS) and calculate skill matches (Jaccard coefficient) to recommend ideal capstone team matches.");
        similarityLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9CA3AF; -fx-line-spacing: 2.5;");
        similarityLabel.setWrapText(true);
        insightsCard.getChildren().add(similarityLabel);
    }

    private VBox createStatCard(String title, Label valueLabel, String colorHex, String subtitle, String iconSymbol) {
        VBox card = new VBox(4);
        card.setMinWidth(110);
        card.setPrefWidth(140);
        card.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E5E7EB #E5E7EB #E5E7EB " + colorHex + "; -fx-border-width: 1 1 1 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        HBox topRow = new HBox(6);
        topRow.setStyle("-fx-alignment: CENTER_LEFT;");
        Label iconLabel = new Label(iconSymbol);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
        titleLabel.setWrapText(true);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(titleLabel, spacer, iconLabel);

        valueLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + "; -fx-font-family: 'Segoe UI';");
        
        Label subLabel = new Label(subtitle);
        subLabel.setWrapText(true);
        subLabel.setStyle("-fx-font-size: 9.5px; -fx-text-fill: #6B7280; -fx-opacity: 0.8;");
        
        // Micro-interaction: Hover Lift & Accent Glow on stats cards using JavaFX smoothly
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-border-color: #D1D5DB #D1D5DB #D1D5DB " + colorHex + "; -fx-border-width: 1 1 1 5;");
            card.setEffect(new DropShadow(15, Color.web(colorHex, 0.25)));
            TranslateTransition tt = new TranslateTransition(Duration.millis(120), card);
            tt.setToY(-4);
            tt.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E5E7EB #E5E7EB #E5E7EB " + colorHex + "; -fx-border-width: 1 1 1 5;");
            card.setEffect(new DropShadow(10, Color.web("#000000", 0.15)));
            TranslateTransition tt = new TranslateTransition(Duration.millis(120), card);
            tt.setToY(0);
            tt.play();
        });

        card.getChildren().addAll(topRow, valueLabel, subLabel);
        return card;
    }

    private void showNotification(String message, boolean isError) {
        notificationMsg.setText(message);
        if (isError) {
            notificationBanner.setStyle("-fx-background-color: rgba(239,68,68,0.15); -fx-border-color: #EF4444; -fx-border-width: 1.5; -fx-background-radius: 8; -fx-padding: 12 18; -fx-alignment: CENTER_LEFT;");
            notificationMsg.setStyle("-fx-text-fill: #111827; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        } else {
            notificationBanner.setStyle("-fx-background-color: rgba(34,197,94,0.15); -fx-border-color: #22C55E; -fx-border-width: 1.5; -fx-background-radius: 8; -fx-padding: 12 18; -fx-alignment: CENTER_LEFT;");
            notificationMsg.setStyle("-fx-text-fill: #111827; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
        }
        notificationBanner.setVisible(true);
        notificationBanner.setManaged(true);
        notificationBanner.setOpacity(0.0);
        
        // Graceful fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationBanner);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        // Auto-hide after 4 seconds with graceful fade out
        Timeline hideTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationBanner);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(evt -> {
                notificationBanner.setVisible(false);
                notificationBanner.setManaged(false);
            });
            fadeOut.play();
        }));
        hideTimeline.play();
    }

    private boolean showConfirmation(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);
        
        DialogPane pane = confirm.getDialogPane();
        pane.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI';");
        
        // Fine-tune buttons inside Dialog Pane
        for (ButtonType bt : pane.getButtonTypes()) {
            Button btn = (Button) pane.lookupButton(bt);
            if (bt == ButtonType.OK) {
                btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 14; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-background-radius: 4; -fx-padding: 6 14; -fx-cursor: hand; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 4;");
            }
        }
        
        java.util.Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void applyTeamFilteringAndSorting() {
        String query = searchTeamsField.getText();
        String sortType = teamSortChoice.getValue();
        String filterType = teamFilterChoice.getValue();
        
        List<Team> filtered = new ArrayList<>(allTeamsList);
        
        // 1. Text Query Filter
        if (query != null && !query.trim().isEmpty()) {
            String lower = query.toLowerCase().trim();
            filtered = filtered.stream()
                .filter(t -> t.getName().toLowerCase().contains(lower) || 
                             (t.getDescription() != null && t.getDescription().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        }

        // 2. Dropdown Status Filter
        if ("Filter: Open (Has slots)".equals(filterType)) {
            filtered = filtered.stream()
                .filter(t -> controller.getTeamMemberNames(t.getId()).size() < 5)
                .collect(Collectors.toList());
        } else if ("Filter: Full".equals(filterType)) {
            filtered = filtered.stream()
                .filter(t -> controller.getTeamMemberNames(t.getId()).size() >= 5)
                .collect(Collectors.toList());
        } else if ("Filter: My Teams".equals(filterType)) {
            filtered = filtered.stream()
                .filter(t -> controller.isStudentInTeam(t.getId()))
                .collect(Collectors.toList());
        }
        
        // 3. Sorting logic
        if ("Name (A-Z)".equals(sortType)) {
            filtered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        } else if ("Members (High-Low)".equals(sortType)) {
            filtered.sort((a, b) -> Integer.compare(
                controller.getTeamMemberNames(b.getId()).size(),
                controller.getTeamMemberNames(a.getId()).size()
            ));
        } else if ("Members (Low-High)".equals(sortType)) {
            filtered.sort((a, b) -> Integer.compare(
                controller.getTeamMemberNames(a.getId()).size(),
                controller.getTeamMemberNames(b.getId()).size()
            ));
        }
        
        teamResultCounter.setText("Showing " + filtered.size() + " of " + allTeamsList.size() + " teams");
        teams.setItems(FXCollections.observableArrayList(filtered));
    }

    private void applyStudentFilteringAndSorting() {
        String query = searchDiscoveryField.getText();
        String sortType = studentSortChoice.getValue();
        String filterType = studentFilterChoice.getValue();
        Student currentStudent = controller.getCurrentStudent();

        List<CollaborationController.StudentSuggestion> filtered = new ArrayList<>(allDiscoveryList);
        
        // 1. Text Query Filter
        if (query != null && !query.trim().isEmpty()) {
            String lower = query.toLowerCase().trim();
            filtered = filtered.stream()
                .filter(s -> s.name().toLowerCase().contains(lower) ||
                             (s.student().getDegree() != null && s.student().getDegree().toLowerCase().contains(lower)) ||
                             (s.student().getUniversity() != null && s.student().getUniversity().toLowerCase().contains(lower)) ||
                             s.skills().stream().anyMatch(sk -> sk.toLowerCase().contains(lower)))
                .collect(Collectors.toList());
        }

        // 2. Dropdown Academic filter
        if (currentStudent != null) {
            if ("Filter: Same University".equals(filterType)) {
                String uni = currentStudent.getUniversity();
                if (uni != null && !uni.isBlank()) {
                    filtered = filtered.stream()
                        .filter(s -> uni.equalsIgnoreCase(s.student().getUniversity()))
                        .collect(Collectors.toList());
                }
            } else if ("Filter: Same Degree".equals(filterType)) {
                String degree = currentStudent.getDegree();
                if (degree != null && !degree.isBlank()) {
                    filtered = filtered.stream()
                        .filter(s -> degree.equalsIgnoreCase(s.student().getDegree()))
                        .collect(Collectors.toList());
                }
            } else if ("Filter: With Skills".equals(filterType)) {
                filtered = filtered.stream()
                    .filter(s -> !s.skills().isEmpty())
                    .collect(Collectors.toList());
            }
        }
        
        // 3. Sorting logic
        if ("Name (A-Z)".equals(sortType)) {
            filtered.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
        } else if ("Most Skills First".equals(sortType)) {
            filtered.sort((a, b) -> Integer.compare(b.skills().size(), a.skills().size()));
        } else if ("Grad Year".equals(sortType)) {
            filtered.sort((a, b) -> Integer.compare(a.student().getGraduationYear(), b.student().getGraduationYear()));
        }
        
        discoveryResultCounter.setText("Showing " + filtered.size() + " of " + allDiscoveryList.size() + " students");
        discoveryList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void animateStatCounter(Label label, int targetValue) {
        if (targetValue <= 0) {
            label.setText("0");
            return;
        }
        int steps = Math.min(15, targetValue);
        int increment = Math.max(1, targetValue / steps);
        
        Timeline timeline = new Timeline();
        for (int i = 1; i <= steps; i++) {
            final int val = Math.min(targetValue, i * increment);
            KeyFrame kf = new KeyFrame(Duration.millis(35 * i), e -> label.setText(String.valueOf(val)));
            timeline.getKeyFrames().add(kf);
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(35 * (steps + 1)), e -> label.setText(String.valueOf(targetValue))));
        timeline.play();
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) return "ST";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private void styleTextField(TextField tf) {
        String base = "-fx-background-color: #F8FAFC; -fx-border-color: #E5E7EB; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-text-fill: #111827; -fx-prompt-text-fill: #94A3B8; -fx-font-size: 12px;";
        String active = "-fx-background-color: #F8FAFC; -fx-border-color: #2563EB; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-text-fill: #111827; -fx-prompt-text-fill: #94A3B8; -fx-font-size: 12px; -fx-effect: dropshadow(three-pass-box, rgba(124,58,237,0.2), 8, 0, 0, 1);";
        tf.setStyle(base);
        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tf.setStyle(active);
            } else {
                tf.setStyle(base);
            }
        });
    }

    private void styleChoiceBox(ChoiceBox<String> cb) {
        String base = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #111827; -fx-text-base-color: #111827; -fx-mark-color: #111827; -fx-cursor: hand;";
        String hover = "-fx-background-color: #EFF6FF; -fx-border-color: #2563EB; -fx-border-width: 1.5; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1D4ED8; -fx-text-base-color: #1D4ED8; -fx-mark-color: #1D4ED8; -fx-cursor: hand;";
        cb.setStyle(base);
        cb.setOnMouseEntered(e -> cb.setStyle(hover));
        cb.setOnMouseExited(e -> cb.setStyle(base));
    }

    private void styleButton(Button btn, String baseBg, String hoverBg, boolean isGradient) {
        String baseStyle = "-fx-background-color: " + baseBg + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: " + hoverBg + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 14; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 1);";
        
        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> {
            btn.setStyle(hoverStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(baseStyle);
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        btn.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), btn);
            st.setToX(0.97);
            st.setToY(0.97);
            st.play();
        });
        btn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(50), btn);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
    }

    private void applyBtnHover(Button btn, String baseBg, String hoverBg) {
        styleButton(btn, baseBg, hoverBg, false);
    }

    private void applyCardHoverAnimation(VBox card, String borderCol) {
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #F8FAFC; -fx-padding: " + card.getPadding().getTop() + "; -fx-background-radius: 16; -fx-border-color: #2563EB; -fx-border-width: 1.5; -fx-border-radius: 16;");
            card.setEffect(new DropShadow(15, Color.web("#2563EB", 0.15)));
            TranslateTransition tt = new TranslateTransition(Duration.millis(120), card);
            tt.setToY(-4);
            tt.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-padding: " + card.getPadding().getTop() + "; -fx-background-radius: 16; -fx-border-color: " + borderCol + "; -fx-border-width: 1; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 3);");
            card.setEffect(null);
            TranslateTransition tt = new TranslateTransition(Duration.millis(120), card);
            tt.setToY(0);
            tt.play();
        });
    }

    private void showStudentProfileDialog(CollaborationController.StudentSuggestion suggestion) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Student Profile - SkillSync");
        alert.setHeaderText(null);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI';");
        
        // Premium buttons inside dialog pane
        for (ButtonType bt : dialogPane.getButtonTypes()) {
            Button btn = (Button) dialogPane.lookupButton(bt);
            btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
            applyBtnHover(btn, "#2563EB", "#1D4ED8");
        }

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setMinWidth(440);
        content.setPrefWidth(460);
        
        HBox headerRow = new HBox(14);
        headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
        
        String[] avatarColors = {"#283593", "#3949AB", "#10B981", "#14B8A6", "#F59E0B", "#EF4444"};
        String color = avatarColors[Math.abs(suggestion.student().getId()) % avatarColors.length];
        Label avatarLabel = new Label(getInitials(suggestion.name()));
        avatarLabel.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-pref-width: 48; -fx-pref-height: 48; -fx-background-radius: 24;");
        
        VBox nameEmailBox = new VBox(4);
        Label nameLabel = new Label(suggestion.name());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        User u = controller.getStudentUser(suggestion.student().getId());
        String email = (u != null) ? u.getEmail() : (suggestion.name().toLowerCase().replace(" ", ".") + "@skillsync.edu");
        Label emailLabel = new Label("ðŸ“§ " + email);
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        Button copyEmailBtn = new Button("Copy");
        copyEmailBtn.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #6B7280; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 4;");
        copyEmailBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipContent = new ClipboardContent();
            clipContent.putString(email);
            clipboard.setContent(clipContent);
            showNotification("Email copied to clipboard!", false);
        });
        
        HBox emailActionRow = new HBox(6, emailLabel, copyEmailBtn);
        emailActionRow.setStyle("-fx-alignment: CENTER_LEFT;");
        
        nameEmailBox.getChildren().addAll(nameLabel, emailActionRow);
        headerRow.getChildren().addAll(avatarLabel, nameEmailBox);
        
        VBox detailsCard = new VBox(10);
        detailsCard.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 16; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        
        Label universityLabel = new Label("University: " + (suggestion.student().getUniversity() == null || suggestion.student().getUniversity().isBlank() ? "N/A" : suggestion.student().getUniversity()));
        universityLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        
        Label degreeLabel = new Label("ðŸŽ“ Degree / Branch: " + (suggestion.student().getDegree() == null || suggestion.student().getDegree().isBlank() ? "N/A" : suggestion.student().getDegree()));
        degreeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        
        Label gradLabel = new Label("ðŸ“… Graduation Year: " + (suggestion.student().getGraduationYear() == 0 ? "N/A" : String.valueOf(suggestion.student().getGraduationYear())));
        gradLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        
        Label bioLabel = new Label("ðŸ“ Biography: " + (suggestion.student().getBio() == null || suggestion.student().getBio().isBlank() ? "No bio provided." : suggestion.student().getBio()));
        bioLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        bioLabel.setWrapText(true);
        
        detailsCard.getChildren().addAll(universityLabel, degreeLabel, gradLabel, bioLabel);
        
        Label skillsHeader = new Label("ðŸ’ª Professional Skills Portfolio");
        skillsHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
        
        FlowPane skillsFlow = new FlowPane();
        skillsFlow.setHgap(6);
        skillsFlow.setVgap(6);
        if (suggestion.skills().isEmpty()) {
            Label noneLabel = new Label("No skills listed.");
            noneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF; -fx-font-style: italic;");
            skillsFlow.getChildren().add(noneLabel);
        } else {
            for (String skill : suggestion.skills()) {
                Label tag = new Label(skill);
                tag.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-border-color: rgba(124,58,237,0.3); -fx-border-width: 1; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
                skillsFlow.getChildren().add(tag);
            }
        }
        
        content.getChildren().addAll(headerRow, detailsCard, skillsHeader, skillsFlow);
        dialogPane.setContent(content);
        alert.showAndWait();
    }

    private void showTeamDetailsDialog(Team team) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Team Profile - SkillSync");
        alert.setHeaderText(null);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-font-family: 'Segoe UI';");
        
        for (ButtonType bt : dialogPane.getButtonTypes()) {
            Button btn = (Button) dialogPane.lookupButton(bt);
            btn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 6 16; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px;");
            applyBtnHover(btn, "#2563EB", "#1D4ED8");
        }

        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setMinWidth(400);
        content.setPrefWidth(430);
        
        HBox headerRow = new HBox(12);
        headerRow.setStyle("-fx-alignment: CENTER_LEFT;");
        
        Label teamIcon = new Label("ðŸ‘¥");
        teamIcon.setStyle("-fx-font-size: 24px; -fx-background-color: #F8FAFC; -fx-text-fill: #2563EB; -fx-alignment: CENTER; -fx-pref-width: 44; -fx-pref-height: 44; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8;");
        
        VBox nameOwnerBox = new VBox(4);
        Label nameLabel = new Label(team.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        
        String creatorName = controller.getStudentName(team.getCreatedBy());
        Label creatorLabel = new Label("ðŸ‘‘ Project Lead: " + creatorName);
        creatorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        
        nameOwnerBox.getChildren().addAll(nameLabel, creatorLabel);
        headerRow.getChildren().addAll(teamIcon, nameOwnerBox);
        
        VBox detailsCard = new VBox(10);
        detailsCard.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 14; -fx-background-radius: 10; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        
        Label domainLabel = new Label("ðŸŒ Project Domain: " + (team.getDescription() == null || team.getDescription().isBlank() ? "General Project" : team.getDescription()));
        domainLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #14B8A6; -fx-font-weight: bold;");
        
        Button copyIdBtn = new Button("ðŸ“‹ Copy Team ID: " + team.getId());
        copyIdBtn.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px; -fx-text-fill: #6B7280; -fx-padding: 3 8; -fx-cursor: hand; -fx-font-weight: bold;");
        copyIdBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipContent = new ClipboardContent();
            clipContent.putString(String.valueOf(team.getId()));
            clipboard.setContent(clipContent);
            showNotification("Team ID copied to clipboard!", false);
        });
        
        detailsCard.getChildren().addAll(domainLabel, copyIdBtn);
        
        Label membersHeader = new Label("ðŸ‘¥ Project Team Members");
        membersHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");
        
        VBox membersListVBox = new VBox(6);
        membersListVBox.setStyle("-fx-padding: 0 0 0 6;");
        
        try {
            List<TeamRepository.TeamMember> members = controller.getTeamMemberDetails(team.getId());
            int count = members.size();
            
            for (TeamRepository.TeamMember m : members) {
                HBox memberRow = new HBox(8);
                memberRow.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 4 8; -fx-background-color: #F8FAFC; -fx-background-radius: 6; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1; -fx-border-radius: 6;");
                
                Label bullet = new Label("â€¢");
                bullet.setStyle("-fx-text-fill: #2563EB; -fx-font-weight: bold;");
                
                Label nameItem = new Label(m.name());
                nameItem.setStyle("-fx-font-size: 12px; -fx-text-fill: #111827; -fx-font-weight: bold;");
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                String roleText = (m.role() == null || m.role().isBlank()) ? "Member" : m.role();
                Label roleBadge = new Label(roleText);
                String roleBg = "Project Lead".equalsIgnoreCase(roleText) || "Owner".equalsIgnoreCase(roleText) ? "rgba(124,58,237,0.15)" : "white";
                String roleTxtCol = "Project Lead".equalsIgnoreCase(roleText) || "Owner".equalsIgnoreCase(roleText) ? "#2563EB" : "#CBD5E1";
                roleBadge.setStyle("-fx-font-size: 10px; -fx-background-color: " + roleBg + "; -fx-text-fill: " + roleTxtCol + "; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-weight: bold;");
                
                memberRow.getChildren().addAll(bullet, nameItem, spacer, roleBadge);
                membersListVBox.getChildren().add(memberRow);
            }
            
            Label totalLabel = new Label("Capacity Status: " + count + " / 5 Members (" + (5 - count) + " slots left)");
            totalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-padding: 4 0 0 0;");
            
            ProgressBar progressBar = new ProgressBar((double) count / 5.0);
            progressBar.setMaxHeight(8);
            progressBar.setPrefWidth(380);
            String barColor = count >= 5 ? "-fx-accent: #EF4444;" : (count >= 4 ? "-fx-accent: #F59E0B;" : "-fx-accent: #22C55E;");
            progressBar.setStyle("-fx-background-radius: 4; -fx-border-radius: 4; -fx-padding: 0; -fx-control-inner-background: #F8FAFC; " + barColor);
            
            content.getChildren().addAll(headerRow, detailsCard, membersHeader, membersListVBox, totalLabel, progressBar);
        } catch (Exception e) {
            content.getChildren().addAll(headerRow, detailsCard, new Label("Error loading members: " + e.getMessage()));
        }
        
        dialogPane.setContent(content);
        alert.showAndWait();
    }

    private void refresh() {
        try {
            // Load and animate stats
            CollaborationController.CollaborationStats stats = controller.getStats();
            animateStatCounter(activeTeamsStat, stats.activeTeams());
            animateStatCounter(myTeamsStat, stats.myTeams());
            animateStatCounter(studentsAvailableStat, stats.studentsAvailable());
            animateStatCounter(recTeammatesStat, stats.recommendedTeammates());
            animateStatCounter(pendingRequestsStat, stats.pendingRequests());

            // Format last refreshed time
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
            lastRefreshedLabel.setText("Last updated: " + java.time.LocalTime.now().format(dtf));

            // Load and keep all teams
            allTeamsList.clear();
            allTeamsList.addAll(controller.teams());
            applyTeamFilteringAndSorting();
            
            // Handle recommended list (empty state layout)
            List<CollaborationController.StudentSuggestion> recs = controller.getTeammateSuggestions();
            if (recs.isEmpty()) {
                recCard.getChildren().setAll(new Label("â­ Recommended Teammates") {{
                    setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                }}, emptyRecsContainer);
            } else {
                suggestionsList.setItems(FXCollections.observableArrayList(recs));
                recCard.getChildren().setAll(new Label("â­ Recommended Teammates") {{
                    setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
                }}, suggestionsList);
            }

            // Load and keep all discovery list
            allDiscoveryList.clear();
            allDiscoveryList.addAll(controller.getDiscoveryStudents());
            applyStudentFilteringAndSorting();

            // Rebuild visual insights card with fresh counts
            rebuildInsightsCard();
            
        } catch (RuntimeException e) {
            ViewFactory.error("Failed to load collaboration data: " + e.getMessage());
        }
    }
}

