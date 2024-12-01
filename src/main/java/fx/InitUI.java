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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class InitUI {
    @Autowired
    private ApplicationContext applicationContext;
    
    private Stage primaryStage;
    private BorderPane mainLayout;
    private VBox menuLayout;
    private Scene scene;

    public void initUI(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("学生成绩管理系统");

        // 创建主布局
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        // 创建顶部标题栏
        HBox titleBar = createTitleBar();
        mainLayout.setTop(titleBar);

        // 创建菜单布局
        createMenuLayout();
        
        // 设置菜单为主界面
        mainLayout.setCenter(menuLayout);

        // 设置场景
        scene = new Scene(mainLayout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // 设置窗口最小尺寸
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);
        
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

    private void createMenuLayout() {
        menuLayout = new VBox(30);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(50));
        menuLayout.setMaxWidth(600);

        // 创建功能按钮
        Button inputButton = createMenuButton("学生成绩录入");
        Button queryButton = createMenuButton("学生成绩的查询");
        Button updateButton = createMenuButton("学生信息变更");
        Button exitButton = createMenuButton("退出系统");
        exitButton.getStyleClass().add("exit-button");

        // 设置按钮点击事件
        inputButton.setOnAction(e -> showScoreInput());
        queryButton.setOnAction(e -> showStudentQuery());
        updateButton.setOnAction(e -> showStudentUpdate());
        exitButton.setOnAction(e -> exitSystem());

        // 添加按钮到容器
        menuLayout.getChildren().addAll(inputButton, queryButton, updateButton, exitButton);
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        
        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/icons/input.png")));
            imageView.setFitHeight(32);
            imageView.setFitWidth(32);
            button.setGraphic(imageView);
        } catch (Exception e) {
            System.out.println("Icon not found");
        }

        button.getStyleClass().add("menu-button");
        button.setPrefSize(200, 100);
        button.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        
        return button;
    }

    private void exitSystem() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出确认");
        alert.setHeaderText(null);
        alert.setContentText("确定要退出系统吗？");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            primaryStage.close();
            System.exit(0);
        }
    }

    private void showScoreInput() {
        ScoreInput scoreInput = applicationContext.getBean(ScoreInput.class);
        VBox content = scoreInput.getContent();
        showContent(content, "学生成绩录入");
    }

    private void showStudentQuery() {
        StudentDataService dataService = applicationContext.getBean(StudentDataService.class);
        VBox content = dataService.getContent();
        showContent(content, "学生成绩查询");
    }

    private void showStudentUpdate() {
        StudentDataUpdata dataUpdate = applicationContext.getBean(StudentDataUpdata.class);
        VBox content = dataUpdate.getContent();
        showContent(content, "学生信息修改");
    }

    private void showContent(VBox content, String title) {
        // 创建返回按钮
        Button backButton = new Button("返回主菜单");
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> mainLayout.setCenter(menuLayout));

        // 将返回按钮添加到内容顶部
        VBox containerWithBack = new VBox(10);
        containerWithBack.getChildren().addAll(backButton, content);
        containerWithBack.setAlignment(Pos.TOP_CENTER);
        containerWithBack.setPadding(new Insets(20));

        // 更新主布局
        mainLayout.setCenter(containerWithBack);
        primaryStage.setTitle("学生成绩管理系统 - " + title);
    }
}
