package fx;

import fx.windows.ScoreInput;
import fx.windows.StudentDataService;
import fx.windows.StudentDataUpdata;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class InitUI extends Application {
    private VBox mainContent;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("学生成绩管理系统");

        // 创建主布局
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // 创建顶部标题栏
        HBox titleBar = createTitleBar();
        root.setTop(titleBar);

        // 创建主内容区域
        mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(30));
        root.setCenter(mainContent);

        // 创建功能按钮区域
        VBox buttonContainer = createButtonContainer();
        root.setCenter(buttonContainer);

        // 设置场景
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setPadding(new Insets(20, 0, 20, 0));
        titleBar.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("学生成绩管理系统");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        titleBar.getChildren().add(title);
        return titleBar;
    }

    private VBox createButtonContainer() {
        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));
        container.setMaxWidth(600);

        // 创建两行按钮布局
        HBox topRow = new HBox(40);
        HBox bottomRow = new HBox(40);
        topRow.setAlignment(Pos.CENTER);
        bottomRow.setAlignment(Pos.CENTER);

        // 创建功能按钮
        Button inputButton = createMenuButton("录入学生信息", "/icons/input.png");
        Button queryButton = createMenuButton("查询学生信息", "/icons/query.png");
        Button updateButton = createMenuButton("修改学生信息", "/icons/update.png");
        Button analysisButton = createMenuButton("成绩分析", "/icons/analysis.png");

        // 设置按钮点击事件
        inputButton.setOnAction(e -> showScoreInput());
        queryButton.setOnAction(e -> showStudentData());
        updateButton.setOnAction(e -> showStudentUpdate());
        analysisButton.setOnAction(e -> showStatisticsAnalysis());

        // 将按钮添加到行中
        topRow.getChildren().addAll(inputButton, queryButton);
        bottomRow.getChildren().addAll(updateButton, analysisButton);

        // 将行添加到容器中
        container.getChildren().addAll(topRow, bottomRow);
        
        return container;
    }

    private Button createMenuButton(String text, String iconPath) {
        Button button = new Button(text);
        
        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
            imageView.setFitHeight(32);
            imageView.setFitWidth(32);
            button.setGraphic(imageView);
        } catch (Exception e) {
            System.out.println("Icon not found: " + iconPath);
        }

        button.getStyleClass().add("menu-button");
        button.setPrefSize(200, 100);
        button.setWrapText(true);
        button.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        
        return button;
    }

    private void showScoreInput() {
        clearMainContent();
        ScoreInput scoreInput = new ScoreInput();
        mainContent.getChildren().add(scoreInput.getContent());
    }

    private void showStudentData() {
        clearMainContent();
        StudentDataService studentDataService = new StudentDataService();
        mainContent.getChildren().add(studentDataService.getContent());
    }

    private void showStudentUpdate() {
        clearMainContent();
        StudentDataUpdata studentDataUpdata = new StudentDataUpdata();
        mainContent.getChildren().add(studentDataUpdata.getContent());
    }

    private void showStatisticsAnalysis() {
        // 由于已删除StatisticsAnalysis类，此处可以显示一个提示信息
        clearMainContent();
        Label label = new Label("成绩分析功能正在开发中...");
        label.setFont(Font.font("Microsoft YaHei", 16));
        mainContent.getChildren().add(label);
    }

    private void clearMainContent() {
        mainContent.getChildren().clear();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
