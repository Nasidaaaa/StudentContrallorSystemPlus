package fx.windows;

import examples.Student;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tables.Students;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 学生数据更新界面类
 * 提供学生信息的查询、展示和更新功能
 */
@Component
public class StudentDataUpdata implements StudentDataGetter {
    @Setter
    private ObservableList<StudentDataService.StudentRecord> masterData = FXCollections.observableArrayList();
    private TextField studentIdField;
    private TableView<StudentDataService.StudentRecord> tableView;
    private Button searchButton;
    private Button submitButton;

    @Autowired
    private DatabaseThreadFactory databaseThreadFactory;

    private DatabaseGetThread databaseGetThread;
    private DatabaseInsertThread databaseInsertThread;


    public void setStudentsList(List<Student> studentsList) {
        // 将Student转换为StudentRecord并更新masterData
        if (masterData == null) {
            masterData = FXCollections.observableArrayList();
        }
        masterData.clear();
        for (Student student : studentsList) {
            StudentDataService.StudentRecord record = new StudentDataService.StudentRecord();
            record.setStudentId(student.getStudentId());
            record.setName(student.getName());
            record.setGender(student.getGender());
            record.setClassName(student.getClassName());
            record.setIdNumber(student.getIdCardNumber());
            record.setChineseScore(student.getChineseScores());
            record.setMathScore(student.getMathScores());
            record.setEnglishScore(student.getEnglishScores());
            record.setJavaScore(student.getJavaScores());
            masterData.add(record);
        }
    }

    //初始化数据库数据
    private void initStudentData() {
        databaseInsertThread = (DatabaseInsertThread) databaseThreadFactory.createInsertDatabaseThread(studentsList);
        databaseGetThread = (DatabaseGetThread) databaseThreadFactory.createGetDatabaseThread(studentsList);
        databaseGetThread.start();
    }

    /**
     * 获取界面内容
     * 创建并返回包含搜索区域和表格的主界面
     * @return VBox 包含所有UI组件的垂直布局容器
     */
    public VBox getContent() {

        //初始化数据库数据
        initStudentData();


        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // 学号搜索区域
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);

        Label idLabel = new Label("学号:");
        idLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        studentIdField = new TextField();
        studentIdField.setPromptText("请输入学号");
        studentIdField.setPrefWidth(200);

        searchButton = new Button("查找");
        searchButton.setOnAction(e -> searchStudent());

        submitButton = new Button("提交更新");
        submitButton.setOnAction(e -> submitChanges());
        submitButton.setDisable(true);

        searchBox.getChildren().addAll(idLabel, studentIdField, searchButton, submitButton);

        // 创建表格
        setupTable();

        // 添加所有组件到主布局
        content.getChildren().addAll(searchBox, tableView);

        return content;
    }

    /**
     * 设置表格
     * 初始化表格视图，创建所有列并设置它们的属性和编辑功能
     * 包括学号、姓名、性别、班级、身份证号和各科成绩列
     */
    private void setupTable() {
        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 学号列
        TableColumn<StudentDataService.StudentRecord, String> studentIdCol = new TableColumn<>("学号");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        studentIdCol.setOnEditCommit(event -> {
            event.getRowValue().setStudentId(event.getNewValue());
        });

        // 姓名列
        TableColumn<StudentDataService.StudentRecord, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            event.getRowValue().setName(event.getNewValue());
        });

        // 性别列
        TableColumn<StudentDataService.StudentRecord, String> genderCol = new TableColumn<>("性别");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setCellFactory(TextFieldTableCell.forTableColumn());
        genderCol.setOnEditCommit(event -> {
            event.getRowValue().setGender(event.getNewValue());
        });

        // 班级列
        TableColumn<StudentDataService.StudentRecord, String> classNameCol = new TableColumn<>("班级");
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        classNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        classNameCol.setOnEditCommit(event -> {
            event.getRowValue().setClassName(event.getNewValue());
        });

        // 身份证号列
        TableColumn<StudentDataService.StudentRecord, String> idNumberCol = new TableColumn<>("身份证号");
        idNumberCol.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        idNumberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        idNumberCol.setOnEditCommit(event -> {
            event.getRowValue().setIdNumber(event.getNewValue());
        });

        // 成绩列
        TableColumn<StudentDataService.StudentRecord, Double> chineseScoreCol = new TableColumn<>("语文成绩");
        chineseScoreCol.setCellValueFactory(new PropertyValueFactory<>("chineseScore"));
        setupScoreColumn(chineseScoreCol, "setChineseScore");

        TableColumn<StudentDataService.StudentRecord, Double> mathScoreCol = new TableColumn<>("数学成绩");
        mathScoreCol.setCellValueFactory(new PropertyValueFactory<>("mathScore"));
        setupScoreColumn(mathScoreCol, "setMathScore");

        TableColumn<StudentDataService.StudentRecord, Double> englishScoreCol = new TableColumn<>("英语成绩");
        englishScoreCol.setCellValueFactory(new PropertyValueFactory<>("englishScore"));
        setupScoreColumn(englishScoreCol, "setEnglishScore");

        TableColumn<StudentDataService.StudentRecord, Double> javaScoreCol = new TableColumn<>("Java成绩");
        javaScoreCol.setCellValueFactory(new PropertyValueFactory<>("javaScore"));
        setupScoreColumn(javaScoreCol, "setJavaScore");

        tableView.getColumns().addAll(
                studentIdCol, nameCol, genderCol, classNameCol, idNumberCol,
                chineseScoreCol, mathScoreCol, englishScoreCol, javaScoreCol
        );
    }

    /**
     * 设置成绩列
     * 为成绩列配置单元格工厂和编辑提交事件处理
     * @param column 要设置的成绩列
     * @param setter 用于更新成绩的setter方法名
     */
    private void setupScoreColumn(TableColumn<StudentDataService.StudentRecord, Double> column, String setter) {
        column.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
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
        }));
        column.setOnEditCommit(event -> {
            try {
                double newValue = event.getNewValue();
                if (newValue >= 0 && newValue <= 100) {
                    event.getRowValue().getClass().getMethod(setter, Double.class)
                            .invoke(event.getRowValue(), newValue);
                    event.getRowValue().setModified(true);
                } else {
                    showAlert("错误", "成绩必须在0-100之间", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("错误", "成绩格式不正确", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * 处理学生搜索
     * 根据输入的学号查找并显示学生信息
     */
    private void searchStudent() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            showAlert("错误", "请输入学号", Alert.AlertType.ERROR);
            return;
        }

        // 从studentsList中查找学生
        Student student = studentsList.stream()
                .filter(s -> s.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);

        if (student == null) {
            showAlert("提示", "未找到该学生", Alert.AlertType.WARNING);
            return;
        }

        // 将找到的Student转换为StudentRecord并显示在表格中
        StudentDataService.StudentRecord record = new StudentDataService.StudentRecord();
        record.setStudentId(student.getStudentId());
        record.setName(student.getName());
        record.setGender(student.getGender());
        record.setClassName(student.getClassName());
        record.setIdNumber(student.getIdCardNumber());
        record.setChineseScore(student.getChineseScores());
        record.setMathScore(student.getMathScores());
        record.setEnglishScore(student.getEnglishScores());
        record.setJavaScore(student.getJavaScores());

        // 清空表格并添加找到的学生记录
        tableView.getItems().clear();
        tableView.getItems().add(record);
        submitButton.setDisable(false);
    }

    /**
     * 处理提交更改
     * 更新学生信息并同步到数据库
     */
    private void submitChanges() {
        if (tableView.getItems().isEmpty()) {
            showAlert("错误", "没有可更新的数据", Alert.AlertType.ERROR);
            return;
        }

        StudentDataService.StudentRecord record = tableView.getItems().get(0);
        
        // 验证数据
        if (!validateRecord(record)) {
            return;
        }

        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认更新");
        alert.setHeaderText("是否确认提交以下更新？");
        
        String content = String.format(
                "学号: %s\n姓名: %s\n性别: %s\n班级: %s\n身份证号: %s\n语文: %.1f\n数学: %.1f\n英语: %.1f\nJava: %.1f",
                record.getStudentId(), record.getName(), record.getGender(), record.getClassName(),
                record.getIdNumber(), record.getChineseScore(), record.getMathScore(),
                record.getEnglishScore(), record.getJavaScore()
        );
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 更新studentsList中对应的Student对象
            Student studentToUpdate = studentsList.stream()
                    .filter(s -> s.getStudentId().equals(record.getStudentId()))
                    .findFirst()
                    .orElse(null);

            if (studentToUpdate != null) {
                // 更新Student对象
                studentToUpdate.setName(record.getName());
                studentToUpdate.setGender(record.getGender());
                studentToUpdate.setClassName(record.getClassName());
                studentToUpdate.setIdCardNumber(record.getIdNumber());
                studentToUpdate.setChineseScores(record.getChineseScore());
                studentToUpdate.setMathScores(record.getMathScore());
                studentToUpdate.setEnglishScores(record.getEnglishScore());
                studentToUpdate.setJavaScores(record.getJavaScore());

                // 更新数据库
                try {
                    Students dbStudent = new Students(studentToUpdate);
                    Thread updateThread = databaseThreadFactory.createUpdateDatabaseThread(dbStudent);
                    updateThread.start();
                    updateThread.join(); // 等待更新完成
                    
                    // 更新成功后刷新表格显示
                    tableView.refresh();
                    showAlert("成功", "学生信息更新成功", Alert.AlertType.INFORMATION);
                    submitButton.setDisable(true);
                } catch (Exception e) {
                    showAlert("错误", "数据库更新失败: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    /**
     * 验证学生记录
     * 检查学生信息的各个字段是否符合要求：
     * - 姓名：非空且长度不超过20字符
     * - 性别：必须是"男"或"女"
     * - 班级：非空且长度不超过20字符
     * - 身份证号：必须是18位
     * - 所有成绩：必须在0-100之间
     * @param record 要验证的学生记录
     * @return boolean 验证是否通过
     */
    private boolean validateRecord(StudentDataService.StudentRecord record) {
        StringBuilder errorMessage = new StringBuilder();

        // 验证姓名（非空且长度不超过20）
        if (record.getName() == null || record.getName().trim().isEmpty() ||
                record.getName().length() > 20) {
            errorMessage.append("姓名不能为空且长度不能超过20个字符\n");
        }

        // 验证性别（男或女）
        if (!"男".equals(record.getGender()) && !"女".equals(record.getGender())) {
            errorMessage.append("性别只能是'男'或'女'\n");
        }

        // 验证班级（非空且长度不超过20）
        if (record.getClassName() == null || record.getClassName().trim().isEmpty() ||
                record.getClassName().length() > 20) {
            errorMessage.append("班级不能为空且长度不能超过20个字符\n");
        }

        // 验证身份证号（18位）
        if (!record.getIdNumber().matches("[0-9X]{18}")) {
            errorMessage.append("身份证号必须是18位\n");
        }

        // 验证成绩（0-100）
        if (record.getChineseScore() < 0 || record.getChineseScore() > 100) {
            errorMessage.append("语文成绩必须在0-100之间\n");
        }
        if (record.getMathScore() < 0 || record.getMathScore() > 100) {
            errorMessage.append("数学成绩必须在0-100之间\n");
        }
        if (record.getEnglishScore() < 0 || record.getEnglishScore() > 100) {
            errorMessage.append("英语成绩必须在0-100之间\n");
        }
        if (record.getJavaScore() < 0 || record.getJavaScore() > 100) {
            errorMessage.append("Java成绩必须在0-100之间\n");
        }

        if (errorMessage.length() > 0) {
            showAlert("验证错误", errorMessage.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * 清空表格
     * 清除表格中的所有数据，重置搜索框
     * 并禁用提交按钮
     */
    private void clearTable() {
        tableView.getItems().clear();
        studentIdField.clear();
        submitButton.setDisable(true);
    }

    /**
     * 显示提示对话框
     * 用于显示错误、警告或信息提示
     * @param title 对话框标题
     * @param content 对话框内容
     * @param type 对话框类型（ERROR, WARNING, INFORMATION, CONFIRMATION）
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
