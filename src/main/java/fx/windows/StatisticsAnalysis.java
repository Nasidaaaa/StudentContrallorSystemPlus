package fx.windows;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.Map;
import java.util.HashMap;
import java.util.DoubleSummaryStatistics;
import java.util.stream.Collectors;

public class StatisticsAnalysis {
    private TableView<StatisticRecord> tableView;
    private ComboBox<String> subjectCombo;
    private ComboBox<String> analysisTypeCombo;
    private BarChart<String, Number> barChart;
    private ObservableList<StudentDataService.StudentRecord> studentData;
    private TextArea statisticsTextArea;

    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // 控制区域
        VBox controlArea = new VBox(10);
        controlArea.setAlignment(Pos.CENTER);

        // 第一行：选择区域
        HBox selectionBox = new HBox(10);
        selectionBox.setAlignment(Pos.CENTER);

        Label subjectLabel = new Label("统计科目:");
        subjectLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        subjectCombo = new ComboBox<>(
            FXCollections.observableArrayList("语文", "数学", "英语", "Java课程", "总分", "平均分")
        );
        subjectCombo.setValue("语文");

        Label analysisLabel = new Label("分析类型:");
        analysisLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        analysisTypeCombo = new ComboBox<>(
            FXCollections.observableArrayList(
                "成绩分布", "及格率分析", "优秀率分析", "班级对比"
            )
        );
        analysisTypeCombo.setValue("成绩分布");

        selectionBox.getChildren().addAll(subjectLabel, subjectCombo, 
                                        analysisLabel, analysisTypeCombo);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button analyzeButton = new Button("开始分析");
        analyzeButton.setOnAction(e -> performAnalysis());

        Button exportButton = new Button("导出报告");
        exportButton.setOnAction(e -> exportReport());

        buttonBox.getChildren().addAll(analyzeButton, exportButton);

        // 将选择区域和按钮区域添加到控制区域
        controlArea.getChildren().addAll(selectionBox, buttonBox);

        // 创建图表区域
        setupChart();
        VBox chartBox = new VBox(barChart);
        chartBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(chartBox, Priority.ALWAYS);

        // 创建统计结果文本区域
        statisticsTextArea = new TextArea();
        statisticsTextArea.setEditable(false);
        statisticsTextArea.setPrefRowCount(4);
        statisticsTextArea.setStyle("-fx-font-family: 'Microsoft YaHei'; -fx-font-size: 14px;");

        // 创建表格
        setupTable();
        
        // 将所有组件添加到主布局
        content.getChildren().addAll(controlArea, chartBox, statisticsTextArea, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return content;
    }

    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("成绩分析图表");
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);
    }

    private void setupTable() {
        tableView = new TableView<>();
        
        TableColumn<StatisticRecord, String> categoryCol = new TableColumn<>("分类");
        categoryCol.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        
        TableColumn<StatisticRecord, Number> valueCol = new TableColumn<>("数值");
        valueCol.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        TableColumn<StatisticRecord, String> percentageCol = new TableColumn<>("百分比");
        percentageCol.setCellValueFactory(cellData -> cellData.getValue().percentageProperty());
        percentageCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        tableView.getColumns().addAll(categoryCol, valueCol, percentageCol);
        
        // 设置列宽
        categoryCol.setPrefWidth(150);
        valueCol.setPrefWidth(100);
        percentageCol.setPrefWidth(100);
    }

    public void setStudentData(ObservableList<StudentDataService.StudentRecord> data) {
        this.studentData = data;
    }

    private void performAnalysis() {
        if (studentData == null || studentData.isEmpty()) {
            showAlert("提示", "没有可分析的数据", Alert.AlertType.WARNING);
            return;
        }

        String subject = subjectCombo.getValue();
        String analysisType = analysisTypeCombo.getValue();

        // 清除旧数据
        barChart.getData().clear();
        tableView.getItems().clear();
        statisticsTextArea.clear();

        // 获取所选科目的成绩数据
        Map<String, Double> scoreMap = new HashMap<>();
        for (StudentDataService.StudentRecord student : studentData) {
            double score = getScoreBySubject(student, subject);
            scoreMap.put(student.getStudentId(), score);
        }

        // 计算基本统计数据
        DoubleSummaryStatistics stats = scoreMap.values().stream()
            .collect(Collectors.summarizingDouble(Double::doubleValue));

        // 更新统计文本区域
        StringBuilder statsText = new StringBuilder();
        statsText.append(String.format("科目：%s\n", subject));
        statsText.append(String.format("平均分：%.2f  最高分：%.0f  最低分：%.0f  参与人数：%d\n",
            stats.getAverage(), stats.getMax(), stats.getMin(), stats.getCount()));

        // 根据分析类型执行不同的分析
        switch (analysisType) {
            case "成绩分布":
                analyzeScoreDistribution(scoreMap);
                break;
            case "及格率分析":
                analyzePassRate(scoreMap);
                break;
            case "优秀率分析":
                analyzeExcellentRate(scoreMap);
                break;
            case "班级对比":
                analyzeClassComparison(subject);
                break;
        }

        statisticsTextArea.setText(statsText.toString());
    }

    private void analyzeScoreDistribution(Map<String, Double> scoreMap) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<StatisticRecord> tableData = FXCollections.observableArrayList();

        int[] ranges = {0, 60, 70, 80, 90, 100};
        String[] labels = {"<60分", "60-69分", "70-79分", "80-89分", "90-100分"};
        int[] counts = new int[5];
        
        // 统计各分数段人数
        scoreMap.values().forEach(score -> {
            for (int i = 0; i < ranges.length - 1; i++) {
                if (score >= ranges[i] && score < ranges[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
            // 处理满分情况
            if (score == 100) counts[4]++;
        });

        // 添加数据到图表和表格
        for (int i = 0; i < labels.length; i++) {
            series.getData().add(new XYChart.Data<>(labels[i], counts[i]));
            double percentage = counts[i] * 100.0 / scoreMap.size();
            tableData.add(new StatisticRecord(labels[i], counts[i], 
                String.format("%.1f%%", percentage)));
        }

        barChart.getData().add(series);
        tableView.setItems(tableData);
    }

    private void analyzePassRate(Map<String, Double> scoreMap) {
        int passCount = (int) scoreMap.values().stream()
            .filter(score -> score >= 60).count();
        double passRate = passCount * 100.0 / scoreMap.size();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("及格", passCount));
        series.getData().add(new XYChart.Data<>("不及格", scoreMap.size() - passCount));

        ObservableList<StatisticRecord> tableData = FXCollections.observableArrayList(
            new StatisticRecord("及格", passCount, String.format("%.1f%%", passRate)),
            new StatisticRecord("不及格", scoreMap.size() - passCount, 
                String.format("%.1f%%", 100 - passRate))
        );

        barChart.getData().add(series);
        tableView.setItems(tableData);
    }

    private void analyzeExcellentRate(Map<String, Double> scoreMap) {
        int excellentCount = (int) scoreMap.values().stream()
            .filter(score -> score >= 90).count();
        double excellentRate = excellentCount * 100.0 / scoreMap.size();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("优秀", excellentCount));
        series.getData().add(new XYChart.Data<>("非优秀", scoreMap.size() - excellentCount));

        ObservableList<StatisticRecord> tableData = FXCollections.observableArrayList(
            new StatisticRecord("优秀", excellentCount, String.format("%.1f%%", excellentRate)),
            new StatisticRecord("非优秀", scoreMap.size() - excellentCount, 
                String.format("%.1f%%", 100 - excellentRate))
        );

        barChart.getData().add(series);
        tableView.setItems(tableData);
    }

    private void analyzeClassComparison(String subject) {
        // 按班级分组计算平均分
        Map<String, DoubleSummaryStatistics> classStats = studentData.stream()
            .collect(Collectors.groupingBy(
                StudentDataService.StudentRecord::getClassName,
                Collectors.summarizingDouble(student -> getScoreBySubject(student, subject))
            ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        ObservableList<StatisticRecord> tableData = FXCollections.observableArrayList();

        classStats.forEach((className, stats) -> {
            series.getData().add(new XYChart.Data<>(className, stats.getAverage()));
            tableData.add(new StatisticRecord(className, stats.getAverage(), 
                String.format("%.1f分", stats.getAverage())));
        });

        barChart.getData().add(series);
        tableView.setItems(tableData);
    }

    private double getScoreBySubject(StudentDataService.StudentRecord record, String subject) {
        switch (subject) {
            case "语文": return record.getChinese();
            case "数学": return record.getMath();
            case "英语": return record.getEnglish();
            case "Java课程": return record.getJava();
            case "总分": return record.getTotal();
            case "平均分": return record.getAverage();
            default: return 0;
        }
    }

    private void exportReport() {
        // TODO: 实现导出功能
        showAlert("功能提示", "导出报告功能正在开发中", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 统计记录数据类
    public static class StatisticRecord {
        private final javafx.beans.property.StringProperty category;
        private final javafx.beans.property.DoubleProperty value;
        private final javafx.beans.property.StringProperty percentage;

        public StatisticRecord(String category, double value, String percentage) {
            this.category = new javafx.beans.property.SimpleStringProperty(category);
            this.value = new javafx.beans.property.SimpleDoubleProperty(value);
            this.percentage = new javafx.beans.property.SimpleStringProperty(percentage);
        }

        public javafx.beans.property.StringProperty categoryProperty() {
            return category;
        }

        public javafx.beans.property.DoubleProperty valueProperty() {
            return value;
        }

        public javafx.beans.property.StringProperty percentageProperty() {
            return percentage;
        }
    }
}
