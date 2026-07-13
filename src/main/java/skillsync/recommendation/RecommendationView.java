package skillsync.recommendation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.service.recommendation.RankedRecommendation;
import skillsync.utils.ViewFactory;

public final class RecommendationView extends VBox {
    private final FlowPane cardGrid = new FlowPane(16, 16);
    private final List<VBox> recommendationCards = new ArrayList<>();

    public RecommendationView() {
        RecommendationController controller = new RecommendationController();
        RecommendationData data = RecommendationData.preview();
        try {
            data = RecommendationData.fromLive(controller.skillRecommendations(), controller.companyRecommendations(), controller.teammateRecommendations());
        } catch (RuntimeException exception) {
            ViewFactory.error(exception.getMessage());
        }

        TextField search = ViewFactory.searchField("Search recommendations, skills, companies, or roles");
        search.textProperty().addListener((observable, oldValue, newValue) -> filterCards(newValue));

        VBox content = new VBox(18,
                header(data),
                toolbar(search),
                statistics(data),
                recommendationSection(data),
                insights(data)
        );
        getChildren().add(ViewFactory.shell("Recommendation Center", ViewFactory.scroll(content)));
    }

    private HBox header(RecommendationData data) {
        VBox title = new VBox(5,
                ViewFactory.sectionTitle("Recommendation Center"),
                ViewFactory.caption("High-signal guidance for skills, companies, and teammates."));
        HBox header = new HBox(12, title, ViewFactory.spacer(),
                ViewFactory.badge(data.live ? "Live recommendations" : "Preview recommendations", data.live ? ViewFactory.SUCCESS : ViewFactory.WARNING));
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private HBox toolbar(TextField search) {
        HBox filters = new HBox(8,
                ViewFactory.badge("Highest Match", ViewFactory.PRIMARY),
                ViewFactory.badge("Most Relevant", ViewFactory.ACCENT),
                ViewFactory.badge("Newest", ViewFactory.SUCCESS),
                ViewFactory.badge("Alphabetical", ViewFactory.WARNING));
        filters.setAlignment(Pos.CENTER_LEFT);
        HBox toolbar = new HBox(14, search, filters);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(search, Priority.ALWAYS);
        return toolbar;
    }

    private FlowPane statistics(RecommendationData data) {
        FlowPane stats = new FlowPane(14, 14);
        stats.getChildren().addAll(
                ViewFactory.metricCard("Skill Signals", String.valueOf(data.skills.size()), "Skill gaps ready for action", ViewFactory.PRIMARY),
                ViewFactory.metricCard("Company Matches", String.valueOf(data.companies.size()), "Roles aligned to current profile", ViewFactory.ACCENT),
                ViewFactory.metricCard("Teammate Matches", String.valueOf(data.teammates.size()), "Collaboration candidates", ViewFactory.SUCCESS),
                ViewFactory.metricCard("Average Score", data.averageScore() + "%", "Weighted recommendation quality", ViewFactory.WARNING)
        );
        return stats;
    }

    private VBox recommendationSection(RecommendationData data) {
        cardGrid.getChildren().clear();
        recommendationCards.clear();
        data.skills.forEach(item -> addCard(skillCard(item)));
        data.companies.forEach(item -> addCard(companyCard(item)));
        data.teammates.forEach(item -> addCard(teammateCard(item)));
        if (cardGrid.getChildren().isEmpty()) {
            cardGrid.getChildren().add(ViewFactory.emptyState("No recommendations yet", "Complete profile details and skill data to generate useful recommendations."));
        }
        return ViewFactory.card(ViewFactory.sectionHeader("Recommended Next Moves", "Search and scan cards by match quality, reason, and recommended action."), cardGrid);
    }

    private HBox insights(RecommendationData data) {
        VBox signals = ViewFactory.card(ViewFactory.sectionHeader("Insights Panel", "Why these recommendations matter."),
                ViewFactory.caption("Skill and company signals are ranked by current profile gaps and workspace demand."),
                ViewFactory.caption("Teammate cards emphasize collaboration readiness and shared technical direction."),
                ViewFactory.caption(data.live ? "Live data is active for this session." : "Preview cards are isolated so live integration can replace them later."));

        javafx.scene.control.Button reviewTop = ViewFactory.primaryButton("Review Top Match");
        reviewTop.setOnAction(e -> {
            var all = new ArrayList<RankedRecommendation<?>>();
            all.addAll(data.skills);
            all.addAll(data.companies);
            all.addAll(data.teammates);
            var top = all.stream().max(java.util.Comparator.comparingDouble(RankedRecommendation::score)).orElse(null);
            if (top != null) {
                ViewFactory.info("Top Match: " + top.confidence() + " confidence recommendation with score " + Math.round(top.score()) + "%\nReason: " + String.join(" ", top.reasons()));
            } else {
                ViewFactory.info("No recommendations available to review.");
            }
        });

        javafx.scene.control.Button saveSet = ViewFactory.secondaryButton("Save Recommendation Set");
        saveSet.setOnAction(e -> ViewFactory.info("Recommendation set successfully saved to your profile workspace."));

        javafx.scene.control.Button createPlan = ViewFactory.secondaryButton("Create Collaboration Plan");
        createPlan.setOnAction(e -> skillsync.utils.NavigationManager.getInstance().navigateTo("collaboration"));

        VBox actions = ViewFactory.card(ViewFactory.sectionHeader("Action Panel", "Move quickly without leaving context."),
                reviewTop, saveSet, createPlan);

        HBox row = new HBox(16, signals, actions);
        HBox.setHgrow(signals, Priority.ALWAYS);
        HBox.setHgrow(actions, Priority.ALWAYS);
        return row;
    }

    private VBox skillCard(RankedRecommendation<Skill> recommendation) {
        Skill skill = recommendation.item();
        String category = valueOrDefault(skill.getCategory(), "Skill Development");
        return recommendationCard("Skill", skill.getName(), category, recommendation,
                List.of(category, "Confidence: " + recommendation.confidence()),
                "Build proficiency");
    }

    private VBox companyCard(RankedRecommendation<Company> recommendation) {
        Company company = recommendation.item();
        String gpa = company.getMinimumGpa() == null ? "Flexible GPA" : "Min GPA " + company.getMinimumGpa();
        return recommendationCard("Company", company.getName(), valueOrDefault(company.getIndustry(), "Hiring Partner"), recommendation,
                List.of(valueOrDefault(company.getIndustry(), "Industry"), "Confidence: " + recommendation.confidence(), gpa),
                "Prepare application");
    }

    private VBox teammateCard(RankedRecommendation<Student> recommendation) {
        Student student = recommendation.item();
        String name = valueOrDefault(student.getFullName(), "Student " + student.getId());
        return recommendationCard("Teammate", name, valueOrDefault(student.getDegree(), "Collaboration Candidate"), recommendation,
                List.of(valueOrDefault(student.getBranch(), "Shared Goals"), valueOrDefault(student.getUniversity(), "Student Network"), "Confidence: " + recommendation.confidence()),
                "Start collaboration");
    }

    private VBox recommendationCard(String type, String title, String subtitle, RankedRecommendation<?> recommendation, List<String> tags, String action) {
        double normalizedScore = recommendation.score() / 100.0;
        String reason = String.join(" ", recommendation.reasons());
        Label titleLabel = new Label(title);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: " + ViewFactory.TEXT + ";");
        Label subtitleLabel = ViewFactory.caption(subtitle);
        ProgressBar bar = new ProgressBar(normalizedScore);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setStyle("-fx-accent: " + scoreColor(normalizedScore) + ";");
        Label scoreLabel = ViewFactory.badge(Math.round(recommendation.score()) + "% " + recommendation.confidence(), scoreColor(normalizedScore));
        FlowPane tagRow = new FlowPane(6, 6);
        tags.forEach(tag -> tagRow.getChildren().add(ViewFactory.tag(tag)));
        recommendation.suggestedImprovements().stream().limit(3).forEach(improvement -> tagRow.getChildren().add(ViewFactory.tag(improvement)));
        HBox header = new HBox(10, ViewFactory.badge(type, scoreColor(normalizedScore)), ViewFactory.spacer(), scoreLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.control.Button actBtn = ViewFactory.primaryButton(action);
        actBtn.setOnAction(e -> {
            if ("Start collaboration".equalsIgnoreCase(action)) {
                skillsync.utils.NavigationManager.getInstance().navigateTo("collaboration");
            } else {
                ViewFactory.info("Initiating action for: " + title + "\nAction: " + action);
            }
        });
        javafx.scene.control.Button viewProfileBtn = ViewFactory.secondaryButton("View Profile");
        viewProfileBtn.setOnAction(e -> {
            if ("Teammate".equalsIgnoreCase(type)) {
                ViewFactory.info("Viewing profile details for potential teammate: " + title);
            } else {
                skillsync.utils.NavigationManager.getInstance().navigateTo("profile");
            }
        });
        javafx.scene.control.Button saveBtn = ViewFactory.textButton("Save");
        saveBtn.setOnAction(e -> ViewFactory.info("Saved recommendation for: " + title));

        HBox buttons = new HBox(8, actBtn, viewProfileBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);
        VBox card = ViewFactory.card(header, titleLabel, subtitleLabel, bar, ViewFactory.caption(reason), tagRow, buttons);
        card.setPrefWidth(342);
        card.setUserData((type + " " + title + " " + subtitle + " " + reason + " " + String.join(" ", tags)).toLowerCase(Locale.ROOT));
        return card;
    }

    private void addCard(VBox card) {
        recommendationCards.add(card);
        cardGrid.getChildren().add(card);
    }

    private void filterCards(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (VBox card : recommendationCards) {
            boolean match = normalized.isBlank() || String.valueOf(card.getUserData()).contains(normalized);
            card.setVisible(match);
            card.setManaged(match);
        }
    }

    private String scoreColor(double score) {
        if (score >= 0.82) return ViewFactory.SUCCESS;
        if (score >= 0.68) return ViewFactory.WARNING;
        return ViewFactory.ACCENT;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record RecommendationData(List<RankedRecommendation<Skill>> skills,
                                      List<RankedRecommendation<Company>> companies,
                                      List<RankedRecommendation<Student>> teammates,
                                      boolean live) {
        static RecommendationData preview() {
            return new RecommendationData(
                    List.of(ranked(new Skill(1, "Advanced Java", "Backend", "Strengthen enterprise service design"), 86, "High",
                                    "Required by backend placement paths.", "Build a Java service project."),
                            ranked(new Skill(2, "SQL Optimization", "Database", "Improve placement readiness"), 81, "High",
                                    "Database skill demand is visible across companies.", "Tune queries in a portfolio project."),
                            ranked(new Skill(3, "JavaFX UI Patterns", "Desktop UI", "Polish the portfolio experience"), 74, "Medium",
                                    "Desktop UI polish strengthens the SkillSync portfolio.", "Create reusable JavaFX components.")),
                    List.of(ranked(new Company(1, "Northstar Labs", "Software", "https://example.com", BigDecimal.valueOf(7.5)), 79, "Medium",
                                    "Strong software industry alignment.", "Review missing backend skills."),
                            ranked(new Company(2, "CloudWave Systems", "Cloud", "https://example.com", BigDecimal.valueOf(8.0)), 72, "Medium",
                                    "Cloud roles align with database and backend skills.", "Prepare cloud fundamentals.")),
                    List.of(ranked(new Student(1, 1, "SkillSync University", "B.Tech", 2027, "Backend collaborator", "Aarav Mehta", "Computer Science", 8.4), 76, "Medium",
                                    "Shared education and complementary technical direction.", "Define project roles before collaborating."),
                            ranked(new Student(2, 2, "SkillSync University", "B.Tech", 2027, "Data project partner", "Maya Rao", "Data Science", 8.7), 71, "Medium",
                                    "Data skills can balance a backend-heavy team.", "Agree on project scope.")),
                    false);
        }

        static RecommendationData fromLive(List<RankedRecommendation<Skill>> skills, List<RankedRecommendation<Company>> companies, List<RankedRecommendation<Student>> teammates) {
            RecommendationData preview = preview();
            return new RecommendationData(
                    skills.isEmpty() ? preview.skills : skills,
                    companies.isEmpty() ? preview.companies : companies,
                    teammates.isEmpty() ? preview.teammates : teammates,
                    true);
        }

        int averageScore() {
            int total = skills.size() + companies.size() + teammates.size();
            if (total == 0) return 0;
            double score = skills.stream().mapToDouble(RankedRecommendation::score).sum()
                    + companies.stream().mapToDouble(RankedRecommendation::score).sum()
                    + teammates.stream().mapToDouble(RankedRecommendation::score).sum();
            return (int) Math.round(score / total);
        }

        private static <T> RankedRecommendation<T> ranked(T item, double score, String confidence, String reason, String improvement) {
            return new RankedRecommendation<>(item, score, confidence, List.of(reason), List.of(improvement));
        }
    }
}
