package fx.windows;

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
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class StudentDataUpdata implements StudentDataGetter {
    @Setter
    private ObservableList<StudentDataService.StudentRecord> masterData;
    private TextField studentIdField;
    private TableView<StudentDataService.StudentRecord> tableView;
    private Button searchButton;
    private Button submitButton;

    public VBox getContent() {
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

    private void setupTable() {
        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 创建列
        TableColumn<StudentDataService.StudentRecord, String> studentIdCol = new TableColumn<>("学号");
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<StudentDataService.StudentRecord, String> nameCol = new TableColumn<>("姓名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            event.getRowValue().setName(event.getNewValue());
            event.getRowValue().setModified(true);
        });

        TableColumn<StudentDataService.StudentRecord, String> genderCol = new TableColumn<>("性别");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setCellFactory(TextFieldTableCell.forTableColumn());
        genderCol.setOnEditCommit(event -> {
            event.getRowValue().setGender(event.getNewValue());
            event.getRowValue().setModified(true);
        });

        TableColumn<StudentDataService.StudentRecord, String> classNameCol = new TableColumn<>("班级");
        classNameCol.setCellValueFactory(new PropertyValueFactory<>("className"));
        classNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        classNameCol.setOnEditCommit(event -> {
            event.getRowValue().setClassName(event.getNewValue());
            event.getRowValue().setModified(true);
        });

        TableColumn<StudentDataService.StudentRecord, String> idNumberCol = new TableColumn<>("身份证号");
        idNumberCol.setCellValueFactory(new PropertyValueFactory<>("idNumber"));
        idNumberCol.setCellFactory(TextFieldTableCell.forTableColumn());
        idNumberCol.setOnEditCommit(event -> {
            event.getRowValue().setIdNumber(event.getNewValue());
            event.getRowValue().setModified(true);
        });

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

    private void searchStudent() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            showAlert("错误", "请输入学号", Alert.AlertType.ERROR);
            return;
        }

        // 查找学生
        StudentDataService.StudentRecord student = masterData.stream()
            .filter(s -> s.getStudentId().equals(studentId))
            .findFirst()
            .orElse(null);

        if (student == null) {
            showAlert("提示", "未找到该学生", Alert.AlertType.WARNING);
            return;
        }

        // 清空表格并添加找到的学生
        tableView.getItems().clear();
        tableView.getItems().add(student);
        submitButton.setDisable(false);
    }

    private void submitChanges() {
        if (tableView.getItems().isEmpty()) {
            showAlert("错误", "没有可更新的数据", Alert.AlertType.ERROR);
            return;
        }

        StudentDataService.StudentRecord student = tableView.getItems().get(0);
        
        // 验证数据
        if (!validateRecord(student)) {
            return;
        }

        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认更新");
        alert.setHeaderText("是否确认提交以下更新？");
        
        String content = String.format(
            "学号: %s\n姓名: %s\n性别: %s\n班级: %s\n身份证号: %s\n语文: %.1f\n数学: %.1f\n英语: %.1f\nJava: %.1f",
            student.getStudentId(), student.getName(), student.getGender(), student.getClassName(),
            student.getIdNumber(), student.getChineseScore(), student.getMathScore(),
            student.getEnglishScore(), student.getJavaScore()
        );
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 更新主数据集中的学生信息
            StudentDataService.StudentRecord masterStudent = masterData.stream()
                .filter(s -> s.getStudentId().equals(student.getStudentId()))
                .findFirst()
                .orElse(null);

            if (masterStudent != null) {
                masterStudent.setName(student.getName());
                masterStudent.setGender(student.getGender());
                masterStudent.setClassName(student.getClassName());
                masterStudent.setIdNumber(student.getIdNumber());
                masterStudent.setChineseScore(student.getChineseScore());
                masterStudent.setMathScore(student.getMathScore());
                masterStudent.setEnglishScore(student.getEnglishScore());
                masterStudent.setJavaScore(student.getJavaScore());
                masterStudent.setModified(true);

                showAlert("成功", "学生信息已更新", Alert.AlertType.INFORMATION);
                clearTable();
            }
        }
    }

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

    private void clearTable() {
        tableView.getItems().clear();
        studentIdField.clear();
        submitButton.setDisable(true);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
