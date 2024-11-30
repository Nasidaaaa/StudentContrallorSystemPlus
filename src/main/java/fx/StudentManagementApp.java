package fx;

import javafx.application.Application;
import javafx.stage.Stage;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("mapper")
public class StudentManagementApp extends Application {

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        InitUI ui = new InitUI();
        ui.initUI(primaryStage);
    }
}
