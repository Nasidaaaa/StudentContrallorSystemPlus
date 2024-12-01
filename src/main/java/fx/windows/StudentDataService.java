package fx.windows;

import examples.Student;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.util.StringConverter;
import lombok.Data;
import mapper.StudentsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tables.Students;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

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
    private StudentsMapper studentsMapper;

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

        // 筛选和排序区域
        VBox filterSortBox = new VBox(10);
        filterSortBox.setAlignment(Pos.CENTER);

        // 科目选择和筛选条件
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

        // 排序方式选择
        HBox sortBox = new HBox(10);
        sortBox.setAlignment(Pos.CENTER);
        
        Label paiXu = new Label("排序方式:");
        paiXu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        sortOrderCombo = new ComboBox<>(
            FXCollections.observableArrayList("无", "升序", "降序")
        );
        sortOrderCombo.setValue("无");
        
        sortBox.getChildren().addAll(paiXu, sortOrderCombo);

        filterSortBox.getChildren().addAll(filterBox, sortBox);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button searchButton = new Button("查询");
        searchButton.setOnAction(e -> handleSearch());

        Button resetButton = new Button("重置");
        resetButton.setOnAction(e -> handleReset());

        buttonBox.getChildren().addAll(searchButton, resetButton);

        // 将所有区域添加到搜索区域
        searchArea.getChildren().addAll(searchTypeBox, filterSortBox, buttonBox);

        // 创建表格
        setupTable();

        // 应用样式
        searchButton.getStyleClass().add("button");
        resetButton.getStyleClass().add("button");
        tableView.getStyleClass().add("table-view");

        // 添加组件到主布局
        content.getChildren().addAll(searchArea, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // 设置搜索框提示文本根据查询方式变化
        searchTypeCombo.setOnAction(e -> updateSearchFieldPrompt());
        
        // 添加科目选择监听器
        subjectCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            // 当选择了具体科目时启用排序
            sortOrderCombo.setDisable("无".equals(newVal));
            // 更新显示的列
            updateVisibleColumns(newVal);
        });
        
        // 初始时禁用排序
        sortOrderCombo.setDisable(true);
        
        return content;
    }

    private void setupTable() {
        tableView = new TableView<>();
        tableView.setEditable(true);
        
        // 创建所有可能的列
        TableColumn<StudentRecord, String> idColumn = new TableColumn<>("学号");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateStudentId(event.getNewValue())) {
                record.setStudentId(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "学号格式错误", "学号必须是8位数字");
            }
        });

        TableColumn<StudentRecord, String> nameColumn = new TableColumn<>("姓名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateName(event.getNewValue())) {
                record.setName(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "姓名格式错误", "姓名不能为空且长度不能超过20个字符");
            }
        });

        TableColumn<StudentRecord, String> genderColumn = new TableColumn<>("性别");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genderColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateGender(event.getNewValue())) {
                record.setGender(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "性别格式错误", "性别只能是'男'或'女'");
            }
        });

        TableColumn<StudentRecord, String> classColumn = new TableColumn<>("班级");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        classColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        classColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateClassName(event.getNewValue())) {
                record.setClassName(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "班级格式错误", "班级不能为空且长度不能超过20个字符");
            }
        });

        TableColumn<StudentRecord, String> idCardColumn = new TableColumn<>("身份证号");
        idCardColumn.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        idCardColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idCardColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateIdNumber(event.getNewValue())) {
                record.setIdNumber(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "身份证号格式错误", "身份证号必须是18位");
            }
        });

        TableColumn<StudentRecord, LocalDate> birthDateColumn = new TableColumn<>("出生日期");
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        
        // 为LocalDate创建专门的转换器
        StringConverter<LocalDate> dateConverter = new StringConverter<LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    try {
                        return LocalDate.parse(string, dateFormatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
            }
        };
        
        birthDateColumn.setCellFactory(TextFieldTableCell.forTableColumn(dateConverter));
        birthDateColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateBirthdate(event.getNewValue())) {
                record.setBirthdate(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "出生日期格式错误", "日期格式必须为yyyy-MM-dd");
            }
        });

        // 为数值列创建特殊的编辑器
        StringConverter<Double> doubleConverter = new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return object == null ? "" : String.format("%.1f", object);
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        };

        TableColumn<StudentRecord, Double> chineseColumn = new TableColumn<>("语文");
        chineseColumn.setCellValueFactory(new PropertyValueFactory<>("chineseScore"));
        chineseColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        chineseColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateScore(event.getNewValue())) {
                record.setChineseScore(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "成绩格式错误", "成绩必须在0-100之间");
            }
        });

        TableColumn<StudentRecord, Double> mathColumn = new TableColumn<>("数学");
        mathColumn.setCellValueFactory(new PropertyValueFactory<>("mathScore"));
        mathColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        mathColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateScore(event.getNewValue())) {
                record.setMathScore(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "成绩格式错误", "成绩必须在0-100之间");
            }
        });

        TableColumn<StudentRecord, Double> englishColumn = new TableColumn<>("英语");
        englishColumn.setCellValueFactory(new PropertyValueFactory<>("englishScore"));
        englishColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        englishColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateScore(event.getNewValue())) {
                record.setEnglishScore(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "成绩格式错误", "成绩必须在0-100之间");
            }
        });

        TableColumn<StudentRecord, Double> javaColumn = new TableColumn<>("Java课程");
        javaColumn.setCellValueFactory(new PropertyValueFactory<>("javaScore"));
        javaColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        javaColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            if (validateScore(event.getNewValue())) {
                record.setJavaScore(event.getNewValue());
                record.setModified(true);
            } else {
                tableView.refresh();
                showError("错误", "成绩格式错误", "成绩必须在0-100之间");
            }
        });

        // 添加总分列
        TableColumn<StudentRecord, Double> totalScoreColumn = new TableColumn<>("总分");
        totalScoreColumn.setCellValueFactory(new PropertyValueFactory<>("totalScore"));
        totalScoreColumn.setCellFactory(column -> new TableCell<StudentRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", item));
                }
            }
        });

        // 添加平均分列
        TableColumn<StudentRecord, Double> averageScoreColumn = new TableColumn<>("平均分");
        averageScoreColumn.setCellValueFactory(new PropertyValueFactory<>("averageScore"));
        averageScoreColumn.setCellFactory(column -> new TableCell<StudentRecord, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f", item));
                }
            }
        });

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
        columnMap.put("总分", totalScoreColumn);
        columnMap.put("平均分", averageScoreColumn);

        // 初始时显示所有列
        updateVisibleColumns("无");
    }

    private void updateVisibleColumns(String selectedSubject) {
        // 获取所有基础信息列
        List<TableColumn<StudentRecord, ?>> baseColumns = Arrays.asList(
            columnMap.get("学号"),
            columnMap.get("姓名"),
            columnMap.get("性别"),
            columnMap.get("班级"),
            columnMap.get("身份证号"),
            columnMap.get("出生日期")
        );

        // 清空当前所有列
        tableView.getColumns().clear();
        
        // 添加基础信息列
        tableView.getColumns().addAll(baseColumns);

        // 根据选择的科目添加对应的列
        switch (selectedSubject) {
            case "总分":
                tableView.getColumns().add(columnMap.get("总分"));
                break;
            case "语文":
                tableView.getColumns().add(columnMap.get("语文"));
                break;
            case "数学":
                tableView.getColumns().add(columnMap.get("数学"));
                break;
            case "英语":
                tableView.getColumns().add(columnMap.get("英语"));
                break;
            case "Java课程":
                tableView.getColumns().add(columnMap.get("Java课程"));
                break;
            case "平均分":
                tableView.getColumns().add(columnMap.get("平均分"));
                break;
            case "无":
                // 显示所有列
                tableView.getColumns().addAll(
                    columnMap.get("语文"),
                    columnMap.get("数学"),
                    columnMap.get("英语"),
                    columnMap.get("Java课程"),
                    columnMap.get("总分"),
                    columnMap.get("平均分")
                );
                break;
        }
    }

    private void handleSearch() {
        String searchType = searchTypeCombo.getValue();
        String searchValue = searchField.getText().trim();
        String subject = subjectCombo.getValue();
        String filter = filterCombo.getValue();
        String sortOrder = sortOrderCombo.getValue();

        // 从数据库获取数据
        List<Students> studentsList;
        try {
            if (searchValue.isEmpty()) {
                studentsList = studentsMapper.selectList(null);
            } else {
                // 根据不同的搜索类型查询数据库
                switch (searchType) {
                    case "按学号查询":
                        Students student = studentsMapper.findByStudentId(searchValue);
                        studentsList = student != null ? List.of(student) : List.of();
                        break;
                    case "按姓名查询":
                        studentsList = studentsMapper.findByNameLike(searchValue);
                        break;
                    case "按班级查询":
                        studentsList = studentsMapper.findByClassName(searchValue);
                        break;
                    default:
                        studentsList = studentsMapper.selectList(null);
                }
            }

            // 清空并重新加载数据
            masterData.clear();
            for (Students student : studentsList) {
                masterData.add(new StudentRecord(student));
            }

            // 设置过滤条件
            filteredData = new FilteredList<>(masterData);
            filteredData.setPredicate(student -> {
                // 分数筛选
                if (!"无筛选".equals(filter) && !"无".equals(subject)) {
                    double score = getScoreBySubject(student, subject);
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
            if (!"无".equals(sortOrder) && !"无".equals(subject)) {
                sortedData.setComparator((s1, s2) -> {
                    double score1 = getScoreBySubject(s1, subject);
                    double score2 = getScoreBySubject(s2, subject);
                    return "升序".equals(sortOrder) ? Double.compare(score1, score2) : Double.compare(score2, score1);
                });
            }

            tableView.setItems(sortedData);

            // 显示查询结果
            int count = sortedData.size();
            StringBuilder message = new StringBuilder();
            message.append("查询结果：共找到 ").append(count).append(" 条记录\n");
            message.append("查询方式：").append(searchType).append("\n");
            
            if (!"无".equals(subject)) {
                message.append("筛选科目：").append(subject).append("\n");
            }
            if (!"无筛选".equals(filter)) {
                message.append("分数筛选：").append(filter).append("\n");
            }
            if (!"无".equals(sortOrder)) {
                message.append("排序方式：").append(sortOrder);
            }
            
            showInfo("查询结果", message.toString());
        } catch (Exception e) {
            showError("查询错误", "数据库查询失败", e.getMessage());
        }
    }

    private double getScoreBySubject(StudentRecord record, String subject) {
        switch (subject) {
            case "语文": return record.getChineseScore();
            case "数学": return record.getMathScore();
            case "英语": return record.getEnglishScore();
            case "Java课程": return record.getJavaScore();
            case "总分": return record.getTotalScore();
            case "平均分": return record.getAverageScore();
            default: return 0.0;
        }
    }

    // 更新数据的处理方法
    private void handleUpdate() {
        List<StudentRecord> modifiedRecords = masterData.stream()
                .filter(StudentRecord::isModified)
                .toList();

        if (modifiedRecords.isEmpty()) {
            showInfo("提示", "没有需要更新的数据");
            return;
        }

        try {
            for (StudentRecord record : modifiedRecords) {
                Students student = new Students();
                student.setStudentid(record.getStudentId());
                student.setName(record.getName());
                student.setGender(record.getGender());
                student.setClassname(record.getClassName());
                student.setIdnumber(record.getIdNumber());
                student.setBirthdate(record.getBirthdate());
                student.setChinesegrade(record.getChineseScore());
                student.setEnglishgrade(record.getEnglishScore());
                student.setMathgrade(record.getMathScore());
                student.setJavagrade(record.getJavaScore());
                
                // 检查记录是否存在
                Students existingStudent = studentsMapper.findByStudentId(record.getStudentId());
                if (existingStudent != null) {
                    // 更新现有记录
                    studentsMapper.updateById(student);
                } else {
                    // 插入新记录
                    studentsMapper.insert(student);
                }
                record.setModified(false);
            }
            showInfo("成功", "成功更新 " + modifiedRecords.size() + " 条记录");
            
            // 刷新表格数据
            handleSearch();
        } catch (Exception e) {
            showError("错误", "更新数据失败", e.getMessage());
        }
    }

    private void handleReset() {
        // 重置搜索条件
        searchField.clear();
        searchTypeCombo.setValue("按学号查询");
        subjectCombo.setValue("无");
        filterCombo.setValue("无筛选");
        sortOrderCombo.setValue("无");
        sortOrderCombo.setDisable(true);

        // 清空学生数据
        masterData.clear();
        tableView.setItems(masterData);

        // 清除筛选条件
        if (filteredData != null) {
            filteredData.setPredicate(null);
        }

        // 更新搜索框提示
        updateSearchFieldPrompt();
        
        // 重置显示的列为全部列
        updateVisibleColumns("无");
    }

    private void loadInitialData() {
        try {
            List<Students> students = studentsMapper.selectList(null);
            masterData.clear();
            for (Students student : students) {
                masterData.add(new StudentRecord(student));
            }
            tableView.setItems(masterData);
        } catch (Exception e) {
            showError("错误", "加载数据失败", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateSearchFieldPrompt() {
        String searchType = searchTypeCombo.getValue();
        if (searchType.equals("按学号查询")) {
            searchField.setPromptText("请输入学号");
        } else if (searchType.equals("按姓名查询")) {
            searchField.setPromptText("请输入姓名");
        } else if (searchType.equals("按班级查询")) {
            searchField.setPromptText("请输入班级");
        }
    }

    // 数据验证方法
    private boolean validateStudentId(String studentId) {
        return studentId != null && studentId.matches("\\d{8}");
    }

    private boolean validateName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 20;
    }

    private boolean validateGender(String gender) {
        return gender != null && (gender.equals("男") || gender.equals("女"));
    }

    private boolean validateClassName(String className) {
        return className != null && !className.trim().isEmpty() && className.length() <= 20;
    }

    private boolean validateIdNumber(String idNumber) {
        return idNumber != null && idNumber.matches("[0-9Xx]{18}");
    }

    private boolean validateBirthdate(LocalDate date) {
        return date != null && !date.isAfter(LocalDate.now());
    }

    private boolean validateScore(Double score) {
        return score != null && score >= 0 && score <= 100;
    }

    @Data
    public static class StudentRecord {
        private String studentId;
        private String name;
        private String gender;
        private String className;
        private String idNumber;
        private LocalDate birthdate;
        private double chineseScore;
        private double mathScore;
        private double englishScore;
        private double javaScore;
        private boolean modified = false;  // 新增：标记记录是否被修改

        public double getTotalScore() {
            return chineseScore + mathScore + englishScore + javaScore;
        }

        public double getAverageScore() {
            return getTotalScore() / 4.0;
        }

        public StudentRecord(Students student) {
            this.studentId = student.getStudentid();
            this.name = student.getName();
            this.gender = student.getGender();
            this.className = student.getClassname();
            this.idNumber = student.getIdnumber();
            this.birthdate = student.getBirthdate();
            this.chineseScore = student.getChinesegrade();
            this.mathScore = student.getMathgrade();
            this.englishScore = student.getEnglishgrade();
            this.javaScore = student.getJavagrade();
        }

        public StudentRecord() {

        }

        public boolean isModified() {
            return modified;
        }

        public void setModified(boolean modified) {
            this.modified = modified;
        }
    }
}
