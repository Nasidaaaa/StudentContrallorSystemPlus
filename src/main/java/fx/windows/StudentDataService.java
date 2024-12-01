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
        resetButton.setOnAction(e -> resetSearch());

        Button updateButton = new Button("更新数据");
        updateButton.setOnAction(e -> handleUpdate());

        buttonBox.getChildren().addAll(searchButton, resetButton, updateButton);

        // 将所有区域添加到搜索区域
        searchArea.getChildren().addAll(searchTypeBox, filterSortBox, buttonBox);

        // 创建表格
        setupTable();

        // 应用样式
        searchButton.getStyleClass().add("button");
        resetButton.getStyleClass().add("button");
        updateButton.getStyleClass().add("button");
        tableView.getStyleClass().add("table-view");

        // 添加组件到主布局
        content.getChildren().addAll(searchArea, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        // 设置搜索框提示文本根据查询方式变化
        searchTypeCombo.setOnAction(e -> updateSearchFieldPrompt());
        
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
            record.setStudentId(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, String> nameColumn = new TableColumn<>("姓名");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setName(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, String> genderColumn = new TableColumn<>("性别");
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        genderColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setGender(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, String> classColumn = new TableColumn<>("班级");
        classColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        classColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        classColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setClassName(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, String> idCardColumn = new TableColumn<>("身份证号");
        idCardColumn.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        idCardColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        idCardColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setIdNumber(event.getNewValue());
            record.setModified(true);
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
            record.setBirthdate(event.getNewValue());
            record.setModified(true);
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
            record.setChineseScore(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, Double> mathColumn = new TableColumn<>("数学");
        mathColumn.setCellValueFactory(new PropertyValueFactory<>("mathScore"));
        mathColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        mathColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setMathScore(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, Double> englishColumn = new TableColumn<>("英语");
        englishColumn.setCellValueFactory(new PropertyValueFactory<>("englishScore"));
        englishColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        englishColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setEnglishScore(event.getNewValue());
            record.setModified(true);
        });

        TableColumn<StudentRecord, Double> javaColumn = new TableColumn<>("Java课程");
        javaColumn.setCellValueFactory(new PropertyValueFactory<>("javaScore"));
        javaColumn.setCellFactory(TextFieldTableCell.forTableColumn(doubleConverter));
        javaColumn.setOnEditCommit(event -> {
            StudentRecord record = event.getRowValue();
            record.setJavaScore(event.getNewValue());
            record.setModified(true);
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

        tableView.getColumns().addAll(Arrays.asList(
            idColumn, nameColumn, genderColumn, classColumn, idCardColumn, birthDateColumn,
            chineseColumn, mathColumn, englishColumn, javaColumn
        ));

        tableView.setItems(masterData);
    }

    private void handleSearch() {
        String searchType = searchTypeCombo.getValue();
        String searchValue = searchField.getText().trim().toLowerCase();
        String subject = subjectCombo.getValue();
        String filter = filterCombo.getValue();
        String sortOrder = sortOrderCombo.getValue();

        // 从数据库获取数据
        List<Students> studentsList;
        if (searchValue.isEmpty()) {
            studentsList = studentsMapper.selectList(null);
        } else {
            // TODO: 根据不同的搜索类型查询数据库
            studentsList = studentsMapper.selectList(null);
        }

        // 清空并重新加载数据
        masterData.clear();
        for (Students student : studentsList) {
            masterData.add(new StudentRecord(student));
        }

        // 设置过滤条件
        filteredData = new FilteredList<>(masterData);
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
    }

    private double getScoreBySubject(StudentRecord record, String subject) {
        switch (subject) {
            case "语文": return record.getChineseScore();
            case "数学": return record.getMathScore();
            case "英语": return record.getEnglishScore();
            case "Java课程": return record.getJavaScore();
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
                
                studentsMapper.updateById(student);
                record.setModified(false);
            }
            showInfo("成功", "成功更新 " + modifiedRecords.size() + " 条记录");
        } catch (Exception e) {
            showError("错误", "更新数据失败", e.getMessage());
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

    private void resetSearch() {
        searchField.clear();
        searchTypeCombo.setValue("按学号查询");
        subjectCombo.setValue("无");
        filterCombo.setValue("无筛选");
        sortOrderCombo.setValue("无");
        sortOrderCombo.setDisable(true);
        masterData.clear();
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

        public boolean isModified() {
            return modified;
        }

        public void setModified(boolean modified) {
            this.modified = modified;
        }
    }
}
