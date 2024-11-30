package fx;

import fx.windows.ScoreInput;
import fx.windows.StatisticsAnalysis;
import fx.windows.StudentDataService;
import fx.windows.StudentDataUpdata;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.Menu;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InitUI {
    ScoreInput scoreInput = new ScoreInput();
    StudentDataService studentDataService = new StudentDataService();
    StatisticsAnalysis statisticsAnalysis = new StatisticsAnalysis();
    StudentDataUpdata studentDataUpdata = new StudentDataUpdata();


    public void initUI(Stage primaryStage) {
        primaryStage.setTitle("学生成绩管理系统");

        Button inputButton = new Button("学生成绩录入");
        inputButton.setOnAction(e -> scoreInput.openInputWindow());
        inputButton.getStyleClass().add("button");

        Button queryButton = new Button("学生成绩的查询");
        queryButton.setOnAction(e -> studentDataService.openQueryWindow());
        queryButton.getStyleClass().add("button");

        Button statisticsButton = new Button("学生成绩统计");
        statisticsButton.setOnAction(e -> statisticsAnalysis.openStatisticsWindow());
        statisticsButton.getStyleClass().add("button");

        Button updateButton = new Button("学生信息变更");
        updateButton.setOnAction(e -> studentDataUpdata.openUpdateWindow());
        updateButton.getStyleClass().add("button");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(inputButton, queryButton, statisticsButton, updateButton);



        // 调用装饰方法
        decorateLayout(layout);

        Scene scene = new Scene(layout, 500, 400);
        scene.getStylesheets().add("styles.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //主界面的颜色
    private void decorateLayout(VBox layout) {
        // 设置渐变背景
        layout.setStyle("-fx-background-color: #e0f7fa; " +  // 基础背景色
                "-fx-background-radius: 10; " +       // 圆角背景
                "-fx-background-image: linear-gradient(to bottom right, #80deea, #26c6da);");  // 渐变背景

        // 添加边框
        layout.setStyle(layout.getStyle() +
                "-fx-border-color: #00796b; " +     // 边框颜色
                "-fx-border-width: 2; " +           // 边框宽度
                "-fx-border-radius: 10; " +         // 边框圆角
                "-fx-padding: 10;");                // 内边距

        // 添加阴影效果
        layout.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.GRAY)); // 添加阴影效果
    }


}
