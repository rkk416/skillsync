package skillsync.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class NavigationManager {
    private static final NavigationManager INSTANCE = new NavigationManager();
    private final Map<String, Supplier<Parent>> screens = new HashMap<>();
    private Stage stage;

    private NavigationManager() {
    }

    public static NavigationManager getInstance() { return INSTANCE; }

    public void initialize(Stage primaryStage) {
        stage = Objects.requireNonNull(primaryStage);
        stage.setMinWidth(960); stage.setMinHeight(640);
    }

    public void register(String name, Supplier<Parent> screenFactory) {
        screens.put(Objects.requireNonNull(name), Objects.requireNonNull(screenFactory));
    }

    public void navigateTo(String name) {
        if (stage == null) throw new IllegalStateException("NavigationManager has not been initialized");
        Supplier<Parent> factory = screens.get(name);
        if (factory == null) throw new IllegalArgumentException("Unknown screen: " + name);
        Parent root = factory.get();
        if (stage.getScene() == null) stage.setScene(new Scene(root, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT));
        else stage.getScene().setRoot(root);
        stage.show();
    }
}
