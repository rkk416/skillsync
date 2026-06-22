package skillsync.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import skillsync.auth.LoginView;
import skillsync.auth.RegisterView;
import skillsync.collaboration.CollaborationView;
import skillsync.dashboard.AnalyticsView;
import skillsync.dashboard.DashboardView;
import skillsync.database.DatabaseConnection;
import skillsync.placement.PlacementView;
import skillsync.profile.ProfileView;
import skillsync.recommendation.RecommendationView;
import skillsync.utils.NavigationManager;

import java.sql.Connection;

public final class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> Platform.runLater(() -> showFatalError(error)));
        verifyDatabase();
        NavigationManager navigation = NavigationManager.getInstance(); navigation.initialize(primaryStage);
        navigation.register("login", LoginView::new); navigation.register("register", RegisterView::new); navigation.register("dashboard", DashboardView::new);
        navigation.register("profile", ProfileView::new); navigation.register("placement", PlacementView::new); navigation.register("collaboration", CollaborationView::new);
        navigation.register("recommendations", RecommendationView::new); navigation.register("analytics", AnalyticsView::new);
        primaryStage.setTitle("SkillSync – Career & Collaboration Intelligence Platform"); navigation.navigateTo("login");
    }

    private void verifyDatabase() {
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            if (!connection.isValid(5)) throw new IllegalStateException("Database connection validation failed");
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to connect to the SkillSync database", exception);
        }
    }

    private void showFatalError(Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle("SkillSync Error"); alert.setHeaderText("The application encountered an unexpected error");
        alert.setContentText(error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage()); alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
