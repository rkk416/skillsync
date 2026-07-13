package skillsync.placement;

import skillsync.ai.GeminiResponse;
import skillsync.ai.GeminiService;
import skillsync.ai.ResumeTextExtractor;
import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.utils.ViewFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;

public final class PlacementView extends VBox {
private Label proTipTitle;
private Label proTipText;
    private ScrollPane scrollPane;
    private final GeminiService geminiService = new GeminiService();

    // ==========================
    // THEME CONSTANTS
    // ==========================
    private static final String COLOR_PRIMARY = "#2563EB";
    private static final String COLOR_PURPLE = "#6366F1";
    private static final String COLOR_GREEN = "#16A34A";
    private static final String COLOR_GREEN_BG = "#DCFCE7";
    private static final String COLOR_GREEN_TEXT = "#15803D";
    private static final String COLOR_RED = "#DC2626";
    private static final String COLOR_RED_BG = "#FEE2E2";
    private static final String COLOR_ORANGE = "#F59E0B";
    private static final String COLOR_MUTED = "#64748B";
    private static final String COLOR_DARK = "#111827";
    private static final String COLOR_BORDER = "#E5E7EB";

    private static final String CARD_STYLE =
            "-fx-background-color:white;" +
            "-fx-background-radius:20;" +
            "-fx-border-radius:20;" +
            "-fx-border-color:#EEF1F6;" +
            "-fx-border-width:1;" +
            "-fx-effect:dropshadow(gaussian,rgba(17,24,39,0.08),22,0.15,0,8);";

    private static final String ANALYZE_BUTTON_DEFAULT_TEXT = "\uD83D\uDD0D Analyze Resume";

    public PlacementView() {

        PlacementController controller = new PlacementController();

        // ==========================
        // MODERN HEADER
        // ==========================

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        topBar.setPadding(new Insets(0, 0, 4, 0));

        VBox titleBox = new VBox(4);

        Label title = new Label("Placement Analyzer");
        title.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Analyze your placement readiness and discover your best opportunities.");
        subtitle.setStyle("-fx-font-size:14px; -fx-text-fill:" + COLOR_MUTED + ";");
        subtitle.getStyleClass().add("page-subtitle");

        titleBox.getChildren().addAll(title, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        TextField searchBox = new TextField();
        searchBox.setPromptText("Search anything...");
        searchBox.setPrefWidth(260);
        searchBox.setPrefHeight(40);
        searchBox.setStyle(
                "-fx-background-color:white;" +
                "-fx-background-radius:14;" +
                "-fx-border-radius:14;" +
                "-fx-border-color:" + COLOR_BORDER + ";" +
                "-fx-border-width:1;" +
                "-fx-padding:6 14;" +
                "-fx-font-size:13px;"
        );
        searchBox.getStyleClass().add("search-field");

        StackPane bellWrap = new StackPane();
        Label bell = new Label("\uD83D\uDD14");
        bell.setStyle(
                "-fx-font-size:18;" +
                "-fx-background-color:white;" +
                "-fx-padding:10;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-border-color:" + COLOR_BORDER + ";" +
                "-fx-border-width:1;"
        );
        bellWrap.getChildren().add(bell);

        topBar.getChildren().addAll(titleBox, headerSpacer, searchBox, bellWrap);


        // ==========================
        // HERO SCORE CARD (gradient status card)
        // ==========================

        VBox scoreCard = new VBox(18);
        scoreCard.getStyleClass().add("score-card");
        scoreCard.setPadding(new Insets(30));

        BorderPane scoreTop = new BorderPane();

        Label scoreHeading = new Label("\uD83C\uDFAF Placement Readiness");
        scoreHeading.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:white;");
        scoreHeading.getStyleClass().add("score-title");

        Label statusLabel = new Label("\u26AA Beginner");
        statusLabel.setStyle(
                "-fx-text-fill:white;" +
                "-fx-background-color:rgba(255,255,255,0.22);" +
                "-fx-background-radius:20;" +
                "-fx-padding:6 16;" +
                "-fx-font-weight:bold;"
        );
        statusLabel.getStyleClass().add("status-label");

        scoreTop.setLeft(scoreHeading);
        scoreTop.setRight(statusLabel);

        Label scoreValue = new Label("-/ 100");
        scoreValue.setStyle("-fx-font-size:42px; -fx-font-weight:bold; -fx-text-fill:white;");
        scoreValue.getStyleClass().add("score-value");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(14);
        progressBar.setStyle("-fx-accent:white;");
        progressBar.getStyleClass().add("score-progress");

        Label scoreText = new Label("\uD83D\uDCDA Needs Improvement");
        scoreText.setStyle("-fx-text-fill:white; -fx-font-size:14px;");
        scoreText.getStyleClass().add("score-subtitle");

        Label tip = new Label("\uD83D\uDCA1 Tip : Improve your DSA, Aptitude and Core Skills to increase your placement score.");
        tip.setWrapText(true);
        tip.setStyle("-fx-text-fill:rgba(255,255,255,0.92); -fx-font-size:13px;");
        tip.getStyleClass().add("score-tip");

        scoreCard.getChildren().addAll(scoreTop, scoreValue, progressBar, scoreText, tip);
        scoreCard.setStyle(
                "-fx-background-color:linear-gradient(to right,#DC2626,#EF4444);" +
                "-fx-background-radius:20;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),18,0.2,0,6);"
        );

        // ==========================
        // RESUME UPLOAD SECTION
        // ==========================

        Label resumeTitle = new Label("\uD83D\uDCC4 Resume Analyzer");
        resumeTitle.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");
        resumeTitle.getStyleClass().add("section-title");

        Label resumeSubtitle = new Label("Upload your resume and get AI-powered analysis and insights.");
        resumeSubtitle.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:14px;");

        Label selectedFile = new Label("\uD83D\uDCC2 No Resume Selected");
        selectedFile.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        selectedFile.setWrapText(true);

        final File[] selectedResume = new File[1];

        Label resumeStatus = new Label("\uD83D\uDD34 No Resume Uploaded");
        resumeStatus.setStyle("-fx-font-size:13px; -fx-text-fill:" + COLOR_RED + "; -fx-font-weight:bold;");

        Label resumeType = new Label("\uD83D\uDCC4 File Type : -");
        resumeType.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:12px;");

        Label resumeSize = new Label("\uD83D\uDCE6 File Size : -");
        resumeSize.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:12px;");

        Label resumeScore = new Label("\u2B50 Resume Score : -");
        resumeScore.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:12px;");

        Label detectedSkills = new Label("\uD83E\uDDE0 Skills Detected : -");
        detectedSkills.setWrapText(true);
        detectedSkills.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:12px;");

        Label suggestedCompanies = new Label("\uD83C\uDFE2 Suggested Companies : -");
        suggestedCompanies.setWrapText(true);
        suggestedCompanies.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:12px;");

        // ==========================
        // CHOOSE RESUME BUTTON
        // ==========================

        Button chooseResume = ViewFactory.primaryButton("\uD83D\uDCC1 Choose Resume");
        chooseResume.setPrefWidth(180);
        chooseResume.setPrefHeight(42);
        chooseResume.setStyle(
                "-fx-background-color:" + COLOR_PRIMARY + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:12;"
        );

        chooseResume.setOnAction(event -> {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Resume");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Resume Files", "*.pdf", "*.doc", "*.docx")
            );

            Window window = getScene() == null ? null : getScene().getWindow();
            File file = fileChooser.showOpenDialog(window);
            selectedResume[0] = file;

            if (file == null) {
                return;
            }

            String name = file.getName().toLowerCase();

            if (!(name.endsWith(".pdf") || name.endsWith(".doc") || name.endsWith(".docx"))) {
                ViewFactory.error("Only PDF, DOC and DOCX files are allowed.");
                selectedResume[0] = null;
                return;
            }

            double sizeKB = file.length() / 1024.0;

            selectedFile.setText("\uD83D\uDCC4 " + file.getName() + "\n\uD83D\uDCE6 " + String.format("%.1f KB", sizeKB));

            resumeStatus.setText("\uD83D\uDFE2 Resume Uploaded");
            resumeStatus.setStyle("-fx-font-size:13px; -fx-text-fill:" + COLOR_GREEN + "; -fx-font-weight:bold;");

            resumeType.setText("\uD83D\uDCC4 File Type : " + name.substring(name.lastIndexOf('.') + 1).toUpperCase());
            resumeSize.setText("\uD83D\uDCE6 File Size : " + String.format("%.1f KB", sizeKB));
        });

        // ==========================
        // ANALYZE RESUME BUTTON
        // ==========================

        Button analyzeResume = ViewFactory.primaryButton(ANALYZE_BUTTON_DEFAULT_TEXT);
        analyzeResume.setPrefWidth(190);
        analyzeResume.setPrefHeight(42);
        analyzeResume.setStyle(
                "-fx-background-color:white;" +
                "-fx-text-fill:" + COLOR_PRIMARY + ";" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:12;" +
                "-fx-border-radius:12;" +
                "-fx-border-color:" + COLOR_PRIMARY + ";" +
                "-fx-border-width:1.5;"
        );

        // Circular resume score indicator (updated after analysis)
        ProgressIndicator resumeCircle = new ProgressIndicator(0);
        resumeCircle.setPrefSize(150, 150);
        resumeCircle.setStyle("-fx-progress-color:" + COLOR_PRIMARY + ";");

        Label resumeCircleValue = new Label("- /100");
        resumeCircleValue.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_PRIMARY + ";");

        StackPane resumeCircleStack = new StackPane(resumeCircle, resumeCircleValue);

        Label resumeCircleCaption = new Label("Upload and analyze your resume.");
        resumeCircleCaption.setWrapText(true);
        resumeCircleCaption.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:13px;");
        resumeCircleCaption.setAlignment(Pos.CENTER);

        Label resumeStars = new Label("\u2606\u2606\u2606\u2606\u2606");
        resumeStars.setStyle("-fx-font-size:16px;");

        FlowPane skillPane = new FlowPane();
        skillPane.setHgap(8);
        skillPane.setVgap(8);

        FlowPane missingPane = new FlowPane();
        missingPane.setHgap(8);
        missingPane.setVgap(8);

        Label skillsTitle = new Label("\uD83E\uDDE0 Skills Analysis");
        skillsTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");

        Label haveTitle = new Label("Skills you have");
        haveTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + "; -fx-font-size:13px;");

        Label missingTitle = new Label("Skills you are missing");
        missingTitle.setStyle("-fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + "; -fx-font-size:13px;");

        VBox skillsCard = new VBox(10, skillsTitle, haveTitle, skillPane, missingTitle, missingPane);
        skillsCard.setPadding(new Insets(18));
        skillsCard.setStyle(CARD_STYLE);
        skillsCard.setPrefWidth(280);
        skillsCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(skillsCard, Priority.ALWAYS);

        Label resumeScoreHeading = new Label("\u2B50 Resume Score");
        resumeScoreHeading.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");

        VBox resumeScoreBox = new VBox(12, resumeScoreHeading, resumeCircleStack, resumeCircleCaption, resumeStars);
        resumeScoreBox.setAlignment(Pos.CENTER);
        resumeScoreBox.setPadding(new Insets(18));
        resumeScoreBox.setStyle(CARD_STYLE);
        resumeScoreBox.setPrefWidth(260);
        resumeScoreBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(resumeScoreBox, Priority.ALWAYS);

        // ==========================
        // BUTTON BOX
        // ==========================

        HBox buttonBox = new HBox(15, chooseResume, analyzeResume);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // ==========================
        // TOP MATCHING COMPANIES (static illustrative panel, non-breaking)
        // ==========================

        VBox companyMatchCard = new VBox(10);
        companyMatchCard.setPadding(new Insets(18));
        companyMatchCard.setStyle(CARD_STYLE);
        companyMatchCard.setPrefWidth(300);
        companyMatchCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(companyMatchCard, Priority.ALWAYS);

        Label companyMatchTitle = new Label("\uD83C\uDFE2 Top Matching Companies");
        companyMatchTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");
        companyMatchCard.getChildren().add(companyMatchTitle);
VBox companyMatchList = new VBox(12);

companyMatchCard.getChildren().add(companyMatchList);

    
        // ==========================
        // RESUME FILE INFO CARD
        // ==========================

        VBox fileCard = new VBox(10);
        fileCard.setPrefWidth(300);
        fileCard.setMaxWidth(Double.MAX_VALUE);
        fileCard.setStyle(CARD_STYLE);
        fileCard.setPadding(new Insets(18));
        HBox.setHgrow(fileCard, Priority.ALWAYS);

        Label fileCardTitle = new Label("\uD83D\uDCC1 Resume File");
        fileCardTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");

        fileCard.getChildren().addAll(
                fileCardTitle, selectedFile, resumeStatus, resumeType,
                resumeSize, resumeScore, detectedSkills, suggestedCompanies
        );

        HBox resumeDashboard = new HBox(20, fileCard, resumeScoreBox, skillsCard, companyMatchCard);
        resumeDashboard.setAlignment(Pos.CENTER);
        resumeDashboard.setFillHeight(true);

        VBox resumeCard = new VBox(18);
        resumeCard.setPadding(new Insets(22));
        resumeCard.setStyle(CARD_STYLE);
        resumeCard.getChildren().addAll(resumeTitle, resumeSubtitle, buttonBox, new Separator(), resumeDashboard);

        // ==========================
        // STATISTICS SECTION
        // ==========================

        FlowPane statsPane = new FlowPane();
        statsPane.setHgap(20);
        statsPane.setVgap(20);

       VBox placementScoreStat =
        createModernStatCard(
                "📊",
                "Placement Score",
                "-",
                COLOR_PRIMARY
        );

VBox atsScoreStat =
        createModernStatCard(
                "📄",
                "ATS Resume Score",
                "-",
                COLOR_GREEN
        );

VBox companiesMatchedStat =
        createModernStatCard(
                "🏢",
                "Companies Matched",
                "0",
                COLOR_PURPLE
        );

VBox missingSkillsStat =
        createModernStatCard(
                "🧠",
                "Missing Skills",
                "0",
                COLOR_ORANGE
        );

        statsPane.getChildren().addAll(placementScoreStat, atsScoreStat, companiesMatchedStat, missingSkillsStat);

        // ==========================
        // ANALYZE RESUME BUTTON ACTION (single handler)
        // ==========================

        analyzeResume.setOnAction(event -> {

            if (selectedResume[0] == null) {
                ViewFactory.error("Please choose a resume first.");
                return;
            }

            analyzeResume.setDisable(true);
            analyzeResume.setText("\u23F3 Analyzing...");
            resumeCircleCaption.setText("AI is analyzing your resume...");

            Task<GeminiResponse> task = new Task<>() {
                @Override
                protected GeminiResponse call() throws Exception {
                    String resumeText = ResumeTextExtractor.extractText(selectedResume[0]);
                    return geminiService.analyzeResume(resumeText);
                }
            };

            task.setOnSucceeded(e -> {
                GeminiResponse response = task.getValue();

                int atsScore = response.getAtsScore();
                double placementScore = response.getPlacementScore();
                List<String> skills = response.getSkills();
                List<String> missingSkills = response.getMissingSkills();
                List<String> matchedCompanies = response.getMatchedCompanies();
                companyMatchList.getChildren().clear();
                

int rank = 1;

for (String company : matchedCompanies) {

    String medal;

    switch (rank) {

        case 1 -> medal = "🥇";
        case 2 -> medal = "🥈";
        case 3 -> medal = "🥉";
        default -> medal = "⭐";

    }

    int percent;

switch (rank) {

    case 1 -> percent = atsScore;
    case 2 -> percent = Math.max(0, atsScore - 3);
    case 3 -> percent = Math.max(0, atsScore - 6);
    case 4 -> percent = Math.max(0, atsScore - 9);
    default -> percent = Math.max(0, atsScore - 12);

}

    Label rowLabel =
            new Label(medal + " " + company);

    rowLabel.setStyle(
            "-fx-font-size:14px;" +
            "-fx-text-fill:" + COLOR_DARK + ";"
    );
    rowLabel.setMinWidth(120);
rowLabel.setPrefWidth(140);
rowLabel.setMaxWidth(Double.MAX_VALUE);

    ProgressBar bar =
            new ProgressBar(percent / 100.0);

    bar.setPrefWidth(140);

    bar.setStyle(
            "-fx-accent:" + COLOR_PRIMARY + ";"
    );

    Label score =
            new Label(percent + "%");

    score.setStyle(
            "-fx-text-fill:" + COLOR_MUTED + ";"
    );

    HBox row =
            new HBox(10, rowLabel, bar, score);

    row.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(rowLabel, Priority.ALWAYS);

    companyMatchList.getChildren().add(row);

    rank++;

    if (rank > 5)
        break;

}

                // Resume score circle + caption + stars
                resumeCircle.setProgress(atsScore / 100.0);
                resumeCircleValue.setText(atsScore + " /100");
                resumeCircleCaption.setText(
                        response.getSummary() != null ? response.getSummary() : "Analysis complete."
                );
                resumeStars.setText(buildStarRating(atsScore));

                // File card details
                resumeScore.setText("\u2B50 Resume Score : " + atsScore + " /100");
                detectedSkills.setText("\uD83E\uDDE0 Skills Detected : " + joinOrDash(skills));
                suggestedCompanies.setText("\uD83C\uDFE2 Suggested Companies : " + joinOrDash(matchedCompanies));

                // Skills you have / missing chips
                skillPane.getChildren().clear();
                if (skills != null) {
                    for (String skill : skills) {
                        skillPane.getChildren().add(createChip(skill, COLOR_GREEN_BG, COLOR_GREEN_TEXT));
                    }
                }

                missingPane.getChildren().clear();
                if (missingSkills != null) {
                    for (String skill : missingSkills) {
                        missingPane.getChildren().add(createChip(skill, COLOR_RED_BG, COLOR_RED));
                    }
                }

                // Statistics cards
                updateStatValue(atsScoreStat, atsScore + " /100");
                updateStatValue(placementScoreStat, String.format("%.1f", placementScore));
                updateStatValue(missingSkillsStat, String.valueOf(missingSkills != null ? missingSkills.size() : 0));
                updateStatValue(companiesMatchedStat, String.valueOf(matchedCompanies != null ? matchedCompanies.size() : 0));

                // Placement readiness hero card
                updateScoreCard(scoreCard, scoreValue, scoreText, progressBar, placementScore);
                StringBuilder plan = new StringBuilder();

plan.append("Priority 1 • ");

if (missingSkills != null && !missingSkills.isEmpty()) {
    plan.append(missingSkills.get(0));
} else {
    plan.append("Build More Projects");
}

plan.append("\n");
plan.append("Priority 2 • Improve Aptitude");

plan.append("\n");
plan.append("Priority 3 • Practice Mock Interviews");

plan.append("\n\n");

plan.append("🎯 Target Placement Score : ");
plan.append((int) Math.min(100, placementScore + 20));

proTipText.setText(plan.toString());

                analyzeResume.setDisable(false);
                analyzeResume.setText(ANALYZE_BUTTON_DEFAULT_TEXT);
            });

            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                ViewFactory.error(ex != null ? ex.getMessage() : "Resume analysis failed. Please try again.");
                resumeCircleCaption.setText("Upload and analyze your resume.");
                analyzeResume.setDisable(false);
                analyzeResume.setText(ANALYZE_BUTTON_DEFAULT_TEXT);
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        });

        // ==========================
        // AVAILABLE COMPANIES SECTION
        // ==========================

        Label companyHeading = new Label("\uD83C\uDFE2 Available Companies");
        companyHeading.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");
        companyHeading.getStyleClass().add("section-title");

        Label companySubtitle = new Label("Search and analyze companies based on your skills.");
        companySubtitle.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:14px;");
        companySubtitle.getStyleClass().add("section-subtitle");

        Label totalCompanies = new Label("0 Companies");
        totalCompanies.setStyle(
                "-fx-text-fill:" + COLOR_PRIMARY + ";" +
                "-fx-font-weight:bold;" +
                "-fx-background-color:#EFF6FF;" +
                "-fx-background-radius:14;" +
                "-fx-padding:6 14;"
        );
        totalCompanies.getStyleClass().add("company-count");

        TextField searchField = new TextField();
        searchField.setPrefHeight(42);
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setPromptText("\uD83D\uDD0D Search Company...");
        searchField.setStyle(
                "-fx-background-color:#F8FAFC;" +
                "-fx-background-radius:14;" +
                "-fx-border-radius:14;" +
                "-fx-border-color:" + COLOR_BORDER + ";" +
                "-fx-border-width:1;" +
                "-fx-padding:8 16;" +
                "-fx-font-size:13px;"
        );
        searchField.getStyleClass().add("search-field");

        ListView<Company> companies = new ListView<>();
        companies.setPrefHeight(430);
        companies.setStyle("-fx-background-color:transparent; -fx-background-insets:0;");
        VBox.setVgrow(companies, Priority.ALWAYS);

        HBox companyHeader = new HBox();
        Region companyHeaderSpacer = new Region();
        HBox.setHgrow(companyHeaderSpacer, Priority.ALWAYS);
        companyHeader.getChildren().addAll(companyHeading, companyHeaderSpacer, totalCompanies);
        companyHeader.setAlignment(Pos.CENTER_LEFT);

        VBox companyCard = new VBox(18);
        companyCard.setStyle(CARD_STYLE);
        companyCard.setPadding(new Insets(22));
        companyCard.getChildren().addAll(companyHeader, companySubtitle, searchField, companies);

        // ==========================
        // ELIGIBILITY SECTION
        // ==========================

        Label eligibilityHeading = new Label("\u2705 Eligibility Result");
        eligibilityHeading.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");
        eligibilityHeading.getStyleClass().add("section-title");

        Label companyResult = new Label("\uD83C\uDFE2 Company : -");
        Label eligibilityResult = new Label("\u2705 Eligibility : -");
        Label scoreResult = new Label("\u2B50 Placement Score : -");
        Label gpaResult = new Label("\uD83C\uDF93 Minimum GPA : -");
        Label skillGapResult = new Label("\uD83E\uDDE0 Missing Skills : -");
        Label recommendationResult = new Label("\uD83D\uDCA1 Recommendation : Select a company.");

       companyResult.setWrapText(true);
companyResult.setStyle(
        "-fx-font-size:17px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#2563EB;"
);

eligibilityResult.setWrapText(true);
eligibilityResult.setStyle(
        "-fx-font-size:17px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#10B981;"
);

scoreResult.setWrapText(true);
scoreResult.setStyle(
        "-fx-font-size:17px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#7C3AED;"
);

gpaResult.setWrapText(true);
gpaResult.setStyle(
        "-fx-font-size:17px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#F59E0B;"
);

skillGapResult.setWrapText(true);
skillGapResult.setStyle(
        "-fx-font-size:16px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#EF4444;"
);

recommendationResult.setWrapText(true);
recommendationResult.setStyle(
        "-fx-font-size:16px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#4F46E5;"
);
        VBox resultBox = new VBox(18);
resultBox.setPadding(new Insets(10, 5, 5, 10));
        resultBox.getChildren().addAll(
                companyResult, eligibilityResult, scoreResult, gpaResult, skillGapResult, recommendationResult
        );

        // ==========================
        // PRO TIP CARD
        // ==========================
      
        Label proTipIcon = new Label("🤖");
proTipIcon.setStyle("-fx-font-size:24px;");
       
       proTipTitle = new Label("AI Improvement Plan");
proTipTitle.setStyle(
        "-fx-font-size:17px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:white;"
);
    

        proTipText = new Label(
        "Analyze your resume to generate a personalized roadmap."
);

proTipText.setWrapText(true);

proTipText.setStyle(
        "-fx-font-size:14px;" +
        "-fx-text-fill:rgba(255,255,255,0.92);"
);
        proTipText.setWrapText(true);
        proTipText.setStyle("-fx-font-size:13px; -fx-text-fill:rgba(255,255,255,0.9);");

        VBox proTipTextBox = new VBox(4, proTipTitle, proTipText);

        Region proTipSpacer = new Region();
        HBox.setHgrow(proTipSpacer, Priority.ALWAYS);

        Button exploreCourses = new Button("Generate Plan");
        exploreCourses.setStyle(
                "-fx-background-color:white;" +
                "-fx-text-fill:" + COLOR_PRIMARY + ";" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:12;" +
                "-fx-padding:10 20;"
        );
        exploreCourses.setOnAction(e -> skillsync.utils.NavigationManager.getInstance().navigateTo("recommendations"));


        HBox proTipCard = new HBox(16, proTipIcon, proTipTextBox, proTipSpacer, exploreCourses);
        proTipCard.setAlignment(Pos.CENTER_LEFT);
        proTipCard.setPadding(new Insets(20));
        proTipCard.setStyle(
                "-fx-background-color:linear-gradient(to right,#4F46E5,#6366F1);" +
                "-fx-background-radius:20;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),16,0.2,0,6);"
        );

        // ==========================
        // LOAD DATA
        // ==========================

        try {
          updateScoreCard(
        scoreCard,
        scoreValue,
        scoreText,
        progressBar,
        0
);

updateStatValue(
        placementScoreStat,
        "-"
);

            var companyList = FXCollections.observableArrayList(controller.companies());
            companies.setItems(companyList);
            totalCompanies.setText(companyList.size() + " Companies");
            updateStatValue(companiesMatchedStat, String.valueOf(companyList.size()));

           searchField.textProperty().addListener((obs, oldValue, newValue) -> {

    String search = newValue.toLowerCase().trim();

    companies.setItems(companyList.filtered(company -> {

        if (search.isEmpty()) {
            return true;
        }

        // Company Name
        if (company.getName() != null &&
                company.getName().toLowerCase().contains(search)) {
            return true;
        }

        // Industry
        if (company.getIndustry() != null &&
                company.getIndustry().toLowerCase().contains(search)) {
            return true;
        }

        // GPA
        if (String.valueOf(company.getMinimumGpa()).contains(search)) {
            return true;
        }

        return false;

    }));

});
        } catch (RuntimeException e) {
            ViewFactory.error(e.getMessage());
        }

        // ==========================
        // COMPANY LIST CELL FACTORY (Analyze Company action)
        // ==========================

        companies.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(Company company, boolean empty) {
                super.updateItem(company, empty);

                if (empty || company == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label name = new Label("\uD83C\uDFE2 " + company.getName());
                name.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:" + COLOR_DARK + ";");

                Label industry = new Label("\uD83D\uDCBC " + company.getIndustry());
                industry.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:13px;");

                Label gpa = new Label("\uD83C\uDF93 GPA : " + company.getMinimumGpa());
                gpa.setStyle("-fx-text-fill:" + COLOR_MUTED + "; -fx-font-size:13px;");

                VBox details = new VBox(6, name, industry, gpa);
                details.setPrefWidth(450);

                Region rowSpacer = new Region();
                HBox.setHgrow(rowSpacer, Priority.ALWAYS);

                Button analyze = ViewFactory.primaryButton("\uD83D\uDD0D Analyze");
                analyze.setPrefWidth(120);
                analyze.setPrefHeight(42);
                analyze.setStyle(
                        "-fx-background-color:" + COLOR_PRIMARY + ";" +
                        "-fx-text-fill:white;" +
                        "-fx-font-weight:bold;" +
                        "-fx-background-radius:12;"
                );

              

               HBox row = new HBox(20, details);
               HBox.setHgrow(details, Priority.ALWAYS);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(18));
                row.setMinHeight(95);
                row.setMaxWidth(Double.MAX_VALUE);
                row.setStyle(
                        "-fx-background-color:white;" +
                        "-fx-background-radius:16;" +
                        "-fx-border-color:" + COLOR_BORDER + ";" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:16;"
                );

                setGraphic(row);
            }
        });

        // ==========================
        // MAIN CONTENT
        // ==========================

        VBox content = new VBox(24);
        content.setFillWidth(true);
        content.setPadding(new Insets(25));

        VBox.setVgrow(companyCard, Priority.ALWAYS);

        try {
            content.getStylesheets().add(getClass().getResource("/css/placement.css").toExternalForm());
        } catch (Exception ignored) {
            // stylesheet optional; inline styles already provide full design
        }

        content.setStyle("-fx-background-color:linear-gradient(to bottom,#F5F7FB,#EEF1F8);");
        content.getStyleClass().add("placement-root");
        content.getChildren().addAll(
    topBar,
    statsPane,
    resumeCard,
    scoreCard,
    companyCard,
    proTipCard
);

        scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color:transparent; -fx-background:transparent;");

        getChildren().add(ViewFactory.shell("Placement", scrollPane));
    }

    // ==========================
    // HELPER METHODS
    // ==========================

    private void updateStatValue(VBox statCard, String value) {
        for (var node : statCard.getChildren()) {
            if (node instanceof Label lbl && lbl.getStyle() != null && lbl.getStyle().contains("40")) {
                lbl.setText(value);
                return;
            }
        }
    }

    private void updateScoreCard(VBox scoreCard, Label scoreValue, Label scoreText, ProgressBar progressBar, double score) {
if (score <= 0) {

    scoreValue.setText("- / 100");

    progressBar.setProgress(0);

    scoreCard.setStyle(
            "-fx-background-color:linear-gradient(to right,#64748B,#94A3B8);" +
            "-fx-background-radius:20;" +
            "-fx-padding:28;"
    );

    scoreText.setText(
            "📄 Upload your resume to calculate placement readiness."
    );

    return;
}
        scoreValue.setText(String.format("%.1f / 100", score));
        progressBar.setProgress(score / 100.0);
        scoreCard.setPadding(new Insets(28));
        scoreCard.setSpacing(18);

        if (score >= 80) {
            scoreCard.setStyle(
                    "-fx-background-color:linear-gradient(to right,#16A34A,#22C55E);" +
                    "-fx-background-radius:20;" +
                    "-fx-padding:28;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),18,0.2,0,6);"
            );
            scoreText.setText("\uD83D\uDE80 Excellent Readiness\n\nYou are fully prepared for placements.");

        } else if (score >= 60) {
            scoreCard.setStyle(
                    "-fx-background-color:linear-gradient(to right,#2563EB,#3B82F6);" +
                    "-fx-background-radius:20;" +
                    "-fx-padding:28;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),18,0.2,0,6);"
            );
            scoreText.setText("\u2B50 Good Progress\n\nKeep improving to become placement ready.");

        } else if (score >= 40) {
            scoreCard.setStyle(
                    "-fx-background-color:linear-gradient(to right,#F59E0B,#FB923C);" +
                    "-fx-background-radius:20;" +
                    "-fx-padding:28;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),18,0.2,0,6);"
            );
            scoreText.setText("\uD83D\uDCD8 Average Readiness\n\nImprove technical skills and aptitude.");

        } else {
            scoreCard.setStyle(
                    "-fx-background-color:linear-gradient(to right,#DC2626,#EF4444);" +
                    "-fx-background-radius:20;" +
                    "-fx-padding:28;" +
                    "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.18),18,0.2,0,6);"
            );
            scoreText.setText("\uD83D\uDCDA Needs Improvement\n\nComplete missing skills before applying.");
        }
    }

    private VBox createModernStatCard(String icon, String title, String value, String color) {

        Label iconLabel = new Label(icon);
     iconLabel.setStyle(
        "-fx-font-size:30px;" +
        "-fx-background-color:" + color + "22;" +
        "-fx-background-radius:18;" +
        "-fx-padding:12;"
);
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
        "-fx-font-size:15px;" +
        "-fx-font-weight:bold;" +
        "-fx-text-fill:#475569;"
);

        Label valueLabel = new Label(value);
       valueLabel.setStyle(
        "-fx-font-size:40px;" +
        "-fx-font-weight:900;" +
        "-fx-text-fill:" + color + ";"
);

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setOpacity(0);
        yAxis.setOpacity(0);
        xAxis.setTickLabelsVisible(false);
        yAxis.setTickLabelsVisible(false);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setPrefSize(120, 40);
      chart.setStyle(
        "-fx-background-color:transparent;" +
        "-fx-padding:0;"
);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(1, 2));
        series.getData().add(new XYChart.Data<>(2, 4));
        series.getData().add(new XYChart.Data<>(3, 3));
        series.getData().add(new XYChart.Data<>(4, 6));
        series.getData().add(new XYChart.Data<>(5, 5));
        series.getData().add(new XYChart.Data<>(6, 8));
        series.getData().add(new XYChart.Data<>(7, 7));
        chart.getData().add(series);
        Platform.runLater(() -> {

    var line = chart.lookup(".chart-series-line");

    if (line != null) {

        line.setStyle(
                "-fx-stroke:" + color + ";" +
                "-fx-stroke-width:3px;"
        );

    }

});

        HBox topRow = new HBox(iconLabel);

        VBox card = new VBox(16, topRow, titleLabel, valueLabel, chart);
        card.setPadding(new Insets(22));
        card.setPrefWidth(260);
        card.setMaxWidth(Double.MAX_VALUE);
       card.setStyle(
        "-fx-background-color:linear-gradient(to bottom,#FFFFFF,#F8FAFF);" +
        "-fx-background-radius:22;" +
        "-fx-border-radius:22;" +
        "-fx-border-color:#E5E7EB;" +
        "-fx-border-width:1;" +
        "-fx-effect:dropshadow(gaussian,rgba(37,99,235,0.12),22,0.25,0,8);"
);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setOnMouseEntered(e -> {

    card.setTranslateY(-6);

    card.setScaleX(1.03);
    card.setScaleY(1.03);

});

card.setOnMouseExited(e -> {

    card.setTranslateY(0);

    card.setScaleX(1.0);
    card.setScaleY(1.0);

});

        return card;
    }

    /** Builds a small pill-style chip used to display an individual skill. */
    private Label createChip(String text, String bgColor, String textColor) {
        Label chip = new Label(text);
        chip.setStyle(
                "-fx-background-color:" + bgColor + ";" +
                "-fx-text-fill:" + textColor + ";" +
                "-fx-font-size:12px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:12;" +
                "-fx-padding:6 12;"
        );
        return chip;
    }

    /** Converts a 0-100 score into a 5-star rating string. */
    private String buildStarRating(int score) {
        int filled = Math.max(0, Math.min(5, Math.round(score / 20.0f)));
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < filled ? '\u2605' : '\u2606');
        }
        return stars.toString();
    }

    /** Joins a list of strings for display, falling back to "-" when empty or null. */
    private String joinOrDash(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "-";
        }
        return String.join(", ", items);
    }
}