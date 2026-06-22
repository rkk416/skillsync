package skillsync.main;

import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX lifecycle entry point. Feature views will be attached here as the application evolves. */
public final class SkillSyncApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SkillSync");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
