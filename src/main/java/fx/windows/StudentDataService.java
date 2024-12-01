package fx.windows;

import examples.Student;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.Priority;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentDataService implements StudentDataGetter {
    private TableView<StudentRecord> tableView;
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;
    private ComboBox<String> subjectCombo;
    private ComboBox<String> filterCombo;
    private ComboBox<String> sortOrderCombo;
    private ObservableList<StudentRecord> masterData = FXCollections.observableArrayList();
    private FilteredList<StudentRecord> filteredData;
    private Map<String, TableColumn<StudentRecord, ?>> columnMap = new HashMap<>();
    @Autowired
    private DatabaseThreadFactory databaseThreadFactory;



    //从数据库中初始化本类中的studentsList
    private void getData(){

    }
    /**
     * 获取主要内容区域
     * 包括搜索条件区域、表格区域和按钮区域
     * @return 主要内容区域
     */
    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // 搜索条件区域
        VBox searchArea = new VBox(10);
        searchArea.setAlignment(Pos.CENTER);

        // 查询方式选择区域
        HBox searchTypeBox = new HBox(10);
        searchTypeBox.setAlignment(Pos.CENTER);
        
        Label searchTypeLabel = new Label("查询方式:");
        searchTypeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        searchTypeCombo = new ComboBox<>(
            FXCollections.observableArrayList("按学号查询", "按姓名查询", "按班级查询")
        );
        searchTypeCombo.setValue("按学号查询");
        
        searchField = new TextField();
        searchField.setPromptText("请输入学号");
        searchField.setPrefWidth(200);
        
        searchTypeBox.getChildren().addAll(searchTypeLabel, searchTypeCombo, searchField);

        // 筛选和排序区域 - 使用两行布局
        VBox filterSortBox = new VBox(10);
        filterSortBox.setAlignment(Pos.CENTER);

        // 科目选择和筛选条件（第一行）
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);
        
        Label saiXuan = new Label("筛选条件:");
        saiXuan.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        subjectCombo = new ComboBox<>(
            FXCollections.observableArrayList("无", "语文", "数学", "英语", "Java课程", "总分", "平均分")
        );
        filterCombo = new ComboBox<>(
            FXCollections.observableArrayList("无筛选", "分数<60", "分数>60", "分数>90")
        );
        subjectCombo.setValue("无");
        filterCombo.setValue("无筛选");
        
        filterBox.getChildren().addAll(saiXuan, subjectCombo, filterCombo);

        // 排序方式选择（第二行）
        HBox sortBox = new HBox(10);
        sortBox.setAlignment(Pos.CENTER);
        
        Label paiXu = new Label("排序方式:");
        paiXu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        sortOrderCombo = new ComboBox<>(
            FXCollections.observableArrayList("无", "升序", "降序")
        );
        sortOrderCombo.setValue("无");
        
        sortBox.getChildren().addAll(paiXu, sortOrderCombo);

        // 将筛选和排序区域添加到VBox
        filterSortBox.getChildren().addAll(filterBox, sortBox);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button searchButton = new Button("查询");
        searchButton.setOnAction(e -> handleSearch());

        Button resetButton = new Button("重置");
        resetButton.setOnAction(e -> resetSearch());

        Button exportButton = new Button("导出数据");
        exportButton.setOnAction(e -> exportData());

        buttonBox.getChildren().addAll(searchButton, resetButton, exportButton);

        // 将所有区域添加到搜索区域
        searchArea.getChildren().addAll(searchTypeBox, filterSortBox, buttonBox);

        // 创建表格
        setupTable();

        // 应用样式
        searchButton.getStyleClass().add("button");
        resetButton.getStyleClass().add("button");
        exportButton.getStyleClass().add("button");
        tableView.getStyleClass().add("table-view");

        // 添加组件到主布局
        content.getChildren().addAll(searchArea, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // 初始化测试数据
        loadData();
        
        // 设置搜索框提示文本根据查询方式变化
        searchTypeCombo.setOnAction(e -> updateSearchFieldPrompt());
        
        subjectCombo.setOnAction(e -> {
            String selectedSubject = subjectCombo.getValue();
            updateTableColumns();
            if ("无".equals(selectedSubject)) {
                sortOrderCombo.setValue("无");
                sortOrderCombo.setDisable(true);
            } else {
                sortOrderCombo.setDisable(false);
            }
        });
        
        // 初始时禁用排序
        sortOrderCombo.setDisable(true);
        
        return content;
    }

    /**
     * 更新搜索框的提示文本
     */
    private void updateSearchFieldPrompt() {
        switch (searchTypeCombo.getValue()) {
            case "按学号查询":
                searchField.setPromptText("请输入学号");
                break;
            case "按姓名查询":
                searchField.setPromptText("请输入姓名");
                break;
            case "按班级查询":
                searchField.setPromptText("请输入班级");
                break;
        }
    }

    /**
     * 处理搜索逻辑
     */
    private void handleSearch() {
        String searchType = searchTypeCombo.getValue();
        String searchValue = searchField.getText().trim().toLowerCase();
        String subject = subjectCombo.getValue();
        String filter = filterCombo.getValue();
        String sortOrder = sortOrderCombo.getValue();

        // 设置过滤条件
        filteredData.setPredicate(student -> {
            // 基本信息匹配
            boolean matchBasic = true;
            switch (searchType) {
                case "按学号查询":
                    matchBasic = student.getStudentId().toLowerCase().contains(searchValue);
                    break;
                case "按姓名查询":
                    matchBasic = student.getName().toLowerCase().contains(searchValue);
                    break;
                case "按班级查询":
                    matchBasic = student.getClassName().toLowerCase().contains(searchValue);
                    break;
            }

            if (!matchBasic) return false;

            // 分数筛选
            if (!"无筛选".equals(filter)) {
                double score = "无".equals(subject) ? student.getTotal() : getScoreBySubject(student, subject);
                switch (filter) {
                    case "分数<60": return score < 60;
                    case "分数>60": return score > 60;
                    case "分数>90": return score > 90;
                    default: return true;
                }
            }
            return true;
        });

        // 应用排序
        SortedList<StudentRecord> sortedData = new SortedList<>(filteredData);
        if (!"无".equals(sortOrder)) {
            sortedData.setComparator((s1, s2) -> {
                double score1 = "无".equals(subject) ? s1.getTotal() : getScoreBySubject(s1, subject);
                double score2 = "无".equals(subject) ? s2.getTotal() : getScoreBySubject(s2, subject);
                return sortOrder.equals("升序") ? Double.compare(score1, score2) : Double.compare(score2, score1);
            });
        }
        
        tableView.setItems(sortedData);

        // 显示查询结果
        int count = sortedData.size();
        StringBuilder message = new StringBuilder();
        message.append("查询结果：共找到 ").append(count).append(" 条记录\n");
        message.append("查询方式：").append(searchType).append("\n");
        
        // 只有在有筛选条件时才显示
        if (!"无".equals(subject)) {
            message.append("筛选科目：").append(subject).append("\n");
        }
        if (!"无筛选".equals(filter)) {
            message.append("分数筛选：").append(filter).append("\n");
        }
        if (!"无".equals(sortOrder)) {
            message.append("排序方式：").append(sortOrder);
        }
        
        showAlert("查询结果", message.toString(), Alert.AlertType.INFORMATION);
    }

    /**
     * 重置搜索条件
     */
    private void resetSearch() {
        searchField.clear();
        searchTypeCombo.setValue("按学号查询");
        subjectCombo.setValue("无");
        filterCombo.setValue("无筛选");
        sortOrderCombo.setValue("无");
        sortOrderCombo.setDisable(true);
        
        filteredData.setPredicate(p -> true);
        tableView.setItems(filteredData);
        
        showAlert("重置完成", "已清空所有查询条件，显示全部数据。", Alert.AlertType.INFORMATION);
    }

    /**
     * 获取选中科目的分数
     * @param record 学生记录
     * @param subject 科目名称
     * @return 对应科目的分数
     */
    private double getScoreBySubject(StudentRecord record, String subject) {
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

    /**
     * 初始化表格和列
     * 设置表格的列属性、数据绑定和样式
     * 包括：学号、姓名、性别、班级、身份证号、各科成绩、总分和平均分
     */
    private void setupTable() {
        tableView = new TableView<>();
        tableView.setEditable(false);

        // 创建所有可能的列
        TableColumn<StudentRecord, String> idColumn = new TableColumn<>("学号");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<StudentRecord, String> nameColumn = new TableColumn<>("姓名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<StudentRecord, String> genderColumn = new TableColumn<>("性别");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<StudentRecord, String> classColumn = new TableColumn<>("班级");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));

        TableColumn<StudentRecord, String> idCardColumn = new TableColumn<>("身份证号");
        idCardColumn.setCellValueFactory(new PropertyValueFactory<>("idCard"));

        TableColumn<StudentRecord, String> birthDateColumn = new TableColumn<>("出生日期");
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));

        TableColumn<StudentRecord, Double> chineseColumn = new TableColumn<>("语文");
        chineseColumn.setCellValueFactory(new PropertyValueFactory<>("chinese"));

        TableColumn<StudentRecord, Double> mathColumn = new TableColumn<>("数学");
        mathColumn.setCellValueFactory(new PropertyValueFactory<>("math"));

        TableColumn<StudentRecord, Double> englishColumn = new TableColumn<>("英语");
        englishColumn.setCellValueFactory(new PropertyValueFactory<>("english"));

        TableColumn<StudentRecord, Double> javaColumn = new TableColumn<>("Java课程");
        javaColumn.setCellValueFactory(new PropertyValueFactory<>("java"));

        TableColumn<StudentRecord, Double> totalColumn = new TableColumn<>("总分");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));

        TableColumn<StudentRecord, Double> averageColumn = new TableColumn<>("平均分");
        averageColumn.setCellValueFactory(new PropertyValueFactory<>("average"));

        // 存储所有列的映射关系
        columnMap.put("学号", idColumn);
        columnMap.put("姓名", nameColumn);
        columnMap.put("性别", genderColumn);
        columnMap.put("班级", classColumn);
        columnMap.put("身份证号", idCardColumn);
        columnMap.put("出生日期", birthDateColumn);
        columnMap.put("语文", chineseColumn);
        columnMap.put("数学", mathColumn);
        columnMap.put("英语", englishColumn);
        columnMap.put("Java课程", javaColumn);
        columnMap.put("总分", totalColumn);
        columnMap.put("平均分", averageColumn);

        // 设置列宽
        Arrays.asList(idColumn, nameColumn, genderColumn, classColumn)
              .forEach(column -> column.setPrefWidth(100));
        Arrays.asList(chineseColumn, mathColumn, englishColumn, javaColumn, 
                     totalColumn, averageColumn)
              .forEach(column -> column.setPrefWidth(80));
        idCardColumn.setPrefWidth(180);
        birthDateColumn.setPrefWidth(100);

        // 为成绩列设置数字格式
        Arrays.asList(chineseColumn, mathColumn, englishColumn, javaColumn, 
                     totalColumn, averageColumn)
              .forEach(column -> column.setStyle("-fx-alignment: CENTER-RIGHT;"));

        // 添加科目选择监听器来动态更新表格列
        subjectCombo.setOnAction(e -> {
            updateTableColumns();
            // 当选择"无"时，自动将排序设置为"无"
            if ("无".equals(subjectCombo.getValue())) {
                sortOrderCombo.setValue("无");
            }
        });

        // 初始显示所有列
        updateTableColumns();

        // 设置表格的选择模式
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 初始化过滤数据
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);
    }

    /**
     * 根据选择的科目更新表格显示的列
     */
    private void updateTableColumns() {
        String subject = subjectCombo.getValue();
        tableView.getColumns().clear();

        // 基本信息列（总是显示）
        List<String> baseColumns = Arrays.asList(
            "学号", "姓名", "性别", "班级", "身份证号", "出生日期"
        );

        // 添加基本信息列
        baseColumns.forEach(colName -> 
            tableView.getColumns().add(columnMap.get(colName))
        );

        if ("无".equals(subject)) {
            // 显示所有成绩列
            Arrays.asList("语文", "数学", "英语", "Java课程", "总分", "平均分")
                  .forEach(colName -> 
                      tableView.getColumns().add(columnMap.get(colName))
                  );
        } else {
            // 只显示选中的科目列
            tableView.getColumns().add(columnMap.get(subject));
        }
    }

    /**
     * 导出数据功能
     * TODO: 将表格数据导出为Excel或CSV格式
     */
    private void exportData() {
        // TODO: 实现导出功能
        showAlert("功能提示", "导出功能正在开发中", Alert.AlertType.INFORMATION);
    }

    /**
     * 统一的提示框显示方法
     * @param title 提示框标题
     * @param message 提示信息
     * @param alertType 提示框类型（信息、警告、错误等）
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 加载数据
     * 表格的示例数据
     * 包含学生基本信息和各科成绩
     */
    private void loadData() {
        // 添加一些测试数据
        masterData.addAll(
            new StudentRecord("2021001", "张三", "男", "软件2101", "330101200001011234", 85, 90, 88, 92),
            new StudentRecord("2021002", "李四", "女", "软件2101", "330101200002022345", 92, 88, 95, 87),
            new StudentRecord("2021003", "王五", "男", "软件2102", "330101200003033456", 78, 85, 80, 88)
        );
    }

    /**
     * 学生记录数据类
     * 存储学生的基本信息和成绩数据
     * 包括：学号、姓名、性别、班级、身份证号、各科成绩、总分和平均分
     */
    public static class StudentRecord {
        private String studentId;
        @Setter
        private String name;
        @Setter
        private String gender;
        @Setter
        private String className;
        @Setter
        private String idCard;
        private double chinese;
        private double math;
        private double english;
        private double java;
        private double total;
        private double average;

        public StudentRecord(String studentId, String name, String gender, String className, 
                           String idCard, double chinese, double math, double english, double java) {
            this.studentId = studentId;
            this.name = name;
            this.gender = gender;
            this.className = className;
            this.idCard = idCard;
            this.chinese = chinese;
            this.math = math;
            this.english = english;
            this.java = java;
            this.total = chinese + math + english + java;
            this.average = this.total / 4;
        }

        public StudentRecord(Student student){

            this.studentId = student.getStudentId();
            this.name = student.getName();
            this.gender = student.getGender();
            this.className = student.getClassName();
            this.idCard = student.getIdCardNumber();
            this.chinese = student.getChineseScores();
            this.math = student.getMathScores();
            this.english = student.getEnglishScores();
            this.java = student.getJavaScores();
            this.total = student.getChineseScores() + student.getMathScores() + student.getEnglishScores() + student.getJavaScores();
            this.average = this.total / 4;
        }

        // Getters
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public String getGender() { return gender; }
        public String getClassName() { return className; }
        public String getIdCard() { return idCard; }
        public double getChinese() { return chinese; }
        public double getMath() { return math; }
        public double getEnglish() { return english; }
        public double getJava() { return java; }
        public double getTotal() { return total; }
        public double getAverage() { return average; }

        // Setters
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public void setChinese(double chinese) {
            this.chinese = chinese;
            updateTotalAndAverage();
        }
        public void setMath(double math) { 
            this.math = math;
            updateTotalAndAverage();
        }
        public void setEnglish(double english) { 
            this.english = english;
            updateTotalAndAverage();
        }
        public void setJava(double java) { 
            this.java = java;
            updateTotalAndAverage();
        }
        
        private void updateTotalAndAverage() {
            this.total = chinese + math + english + java;
            this.average = total / 4;
        }
    }
}
