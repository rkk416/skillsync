package skillsync.recommendation;

import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import skillsync.model.Company;
import skillsync.model.Skill;
import skillsync.model.Student;
import skillsync.utils.ViewFactory;

public final class RecommendationView extends VBox {
    public RecommendationView() {
        RecommendationController controller = new RecommendationController();
        TabPane tabs = new TabPane(); tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        try {
            TableView<Skill> skillTable = skillTable();
            skillTable.setItems(FXCollections.observableArrayList(controller.skills()));
            TableView<Company> companyTable = companyTable();
            companyTable.setItems(FXCollections.observableArrayList(controller.companies()));
            TableView<Student> teammateTable = teammateTable();
            teammateTable.setItems(FXCollections.observableArrayList(controller.teammates()));
            tabs.getTabs().addAll(new Tab("Recommended Skills", skillTable), new Tab("Recommended Companies", companyTable), new Tab("Recommended Teammates", teammateTable));
        } catch (RuntimeException exception) { ViewFactory.error(exception.getMessage()); }
        getChildren().add(ViewFactory.shell("Recommendations", tabs));
    }

    private static TableView<Skill> skillTable() {
        TableView<Skill> table = new TableView<>();
        table.getColumns().add(column("Skill Name", "name"));
        table.getColumns().add(column("Category", "category"));
        table.getColumns().add(column("Description", "description"));
        ViewFactory.prepareTable(table, "Skill recommendations will appear when company requirements exceed the student's current skill profile.");
        return table;
    }

    private static TableView<Company> companyTable() {
        TableView<Company> table = new TableView<>();
        table.getColumns().add(column("Company Name", "name"));
        table.getColumns().add(column("Industry", "industry"));
        table.getColumns().add(column("Minimum GPA", "minimumGpa"));
        ViewFactory.prepareTable(table, "Company recommendations will appear when company requirements are available for matching.");
        return table;
    }

    private static TableView<Student> teammateTable() {
        TableView<Student> table = new TableView<>();
        table.getColumns().add(column("Name", "fullName"));
        table.getColumns().add(column("University", "university"));
        table.getColumns().add(column("Degree", "degree"));
        table.getColumns().add(column("Graduation Year", "graduationYear"));
        table.getColumns().add(column("Bio", "bio"));
        ViewFactory.prepareTable(table, "Teammate recommendations will appear when shared team membership creates useful matches.");
        return table;
    }

    private static <S, T> TableColumn<S, T> column(String title, String property) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }
}
