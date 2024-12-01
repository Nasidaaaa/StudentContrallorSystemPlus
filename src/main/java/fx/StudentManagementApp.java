package fx;

import com.yang.studentcontrallorsystemplus.StudentContrallorSystemPlusApplication;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class StudentManagementApp extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {

        springContext = StudentContrallorSystemPlusApplication.getSpringContext();
    }

    @Override
    public void start(Stage primaryStage) {
        InitUI ui = springContext.getBean(InitUI.class);
        ui.initUI(primaryStage);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
