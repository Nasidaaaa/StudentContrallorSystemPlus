package fx;

import fx.windows.ScoreInput;
import fx.windows.StatisticsAnalysis;
import fx.windows.StudentDataService;
import fx.windows.StudentDataUpdata;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Optional;

public class InitUI {
    private BorderPane mainLayout;
    private VBox menuLayout;
    private Scene scene;
    private Stage primaryStage;

    //主菜单初始化
    public void initUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("学生成绩管理系统");

        // 创建主布局
        mainLayout = new BorderPane();
        
        // 创建菜单布局
        createMenuLayout();
        
        // 设置菜单为主界面
        mainLayout.setCenter(menuLayout);

        scene = new Scene(mainLayout, 800, 600);
        scene.getStylesheets().add("styles.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //菜单栏选项卡添加
    private void createMenuLayout() {
        menuLayout = new VBox(10);
        menuLayout.setAlignment(Pos.CENTER);

        Button inputButton = new Button("学生成绩录入");
        inputButton.setOnAction(e -> showScoreInput());
        inputButton.getStyleClass().add("button");

        Button queryButton = new Button("学生成绩的查询");
        queryButton.setOnAction(e -> showStudentQuery());
        queryButton.getStyleClass().add("button");

        Button statisticsButton = new Button("学生成绩统计");
        statisticsButton.setOnAction(e -> showStatistics());
        statisticsButton.getStyleClass().add("button");

        Button updateButton = new Button("学生信息变更");
        updateButton.setOnAction(e -> showStudentUpdate());
        updateButton.getStyleClass().add("button");

        // 添加退出按钮
        Button exitButton = new Button("退出系统");
        exitButton.setOnAction(e -> exitApplication());
        exitButton.getStyleClass().addAll("button", "exit-button");

        menuLayout.getChildren().addAll(
            inputButton, 
            queryButton, 
            statisticsButton, 
            updateButton,
            exitButton  // 添加退出按钮到布局
        );
        decorateLayout(menuLayout);
    }

    // 退出应用程序的方法
    private void exitApplication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要退出系统吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
            System.exit(0);
        }
    }



    //四个选项卡生成方法
    private void showScoreInput() {
        ScoreInput scoreInput = new ScoreInput();
        VBox content = scoreInput.getContent();
        showContent(content, "学生成绩录入");
    }

    private void showStudentQuery() {
        StudentDataService dataService = new StudentDataService();
        VBox content = dataService.getContent();
        showContent(content, "学生成绩查询");
    }

    private void showStatistics() {
        StatisticsAnalysis statistics = new StatisticsAnalysis();
        VBox content = statistics.getContent();
        showContent(content, "成绩统计分析");
    }

    private void showStudentUpdate() {
        StudentDataUpdata dataUpdate = new StudentDataUpdata();
        VBox content = dataUpdate.getContent();
        showContent(content, "学生信息修改");
    }




    //返回按钮和更新布局
    private void showContent(VBox content, String title) {
        // 创建返回按钮
        Button backButton = new Button("返回主菜单");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> mainLayout.setCenter(menuLayout));

        // 将返回按钮添加到内容顶部
        VBox containerWithBack = new VBox(10);
        containerWithBack.getChildren().addAll(backButton, content);
        containerWithBack.setAlignment(Pos.TOP_CENTER);
        containerWithBack.setPadding(new javafx.geometry.Insets(20));

        // 更新主布局
        mainLayout.setCenter(containerWithBack);
        primaryStage.setTitle("学生成绩管理系统 - " + title);
    }

    //界面背景装饰
    private void decorateLayout(VBox layout) {
        layout.setStyle("-fx-background-color: #e0f7fa; " +
                "-fx-background-radius: 10; " +
                "-fx-background-image: linear-gradient(to bottom right, #80deea, #26c6da);" +
                "-fx-border-color: #00796b; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 10; " +
                "-fx-padding: 20;");

        layout.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.GRAY));
    }
}
