package fx.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class StudentDataUpdata implements StudentDataGetter {
    @Setter
    private ObservableList<StudentDataService.StudentRecord> masterData;
    private TextField studentIdField;
    private TextField nameField;
    private ComboBox<String> genderCombo;
    private TextField classField;
    private TextField idCardField;
    private TextField chineseField;
    private TextField mathField;
    private TextField englishField;
    private TextField javaField;
    private RadioButton modifyButton;
    private RadioButton deleteButton;
    private VBox modifyBox;
    private Button searchButton;
    private Button submitButton;

    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // 操作类型选择
        HBox operationBox = new HBox(20);
        operationBox.setAlignment(Pos.CENTER);
        
        ToggleGroup operationGroup = new ToggleGroup();
        modifyButton = new RadioButton("修改学生信息");
        deleteButton = new RadioButton("删除学生信息");
        modifyButton.setToggleGroup(operationGroup);
        deleteButton.setToggleGroup(operationGroup);
        modifyButton.setSelected(true);
        
        operationBox.getChildren().addAll(modifyButton, deleteButton);

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
        
        searchBox.getChildren().addAll(idLabel, studentIdField, searchButton);

        // 修改信息区域
        modifyBox = new VBox(10);
        modifyBox.setAlignment(Pos.CENTER);
        
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setAlignment(Pos.CENTER);

        // 基本信息
        nameField = new TextField();
        genderCombo = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        classField = new TextField();
        idCardField = new TextField();

        // 成绩信息
        chineseField = new TextField();
        mathField = new TextField();
        englishField = new TextField();
        javaField = new TextField();

        int row = 0;
        infoGrid.add(new Label("姓名:"), 0, row);
        infoGrid.add(nameField, 1, row);
        infoGrid.add(new Label("性别:"), 2, row);
        infoGrid.add(genderCombo, 3, row);

        row++;
        infoGrid.add(new Label("班级:"), 0, row);
        infoGrid.add(classField, 1, row);
        infoGrid.add(new Label("身份证号:"), 2, row);
        infoGrid.add(idCardField, 3, row);

        row++;
        infoGrid.add(new Label("语文成绩:"), 0, row);
        infoGrid.add(chineseField, 1, row);
        infoGrid.add(new Label("数学成绩:"), 2, row);
        infoGrid.add(mathField, 3, row);

        row++;
        infoGrid.add(new Label("英语成绩:"), 0, row);
        infoGrid.add(englishField, 1, row);
        infoGrid.add(new Label("Java成绩:"), 2, row);
        infoGrid.add(javaField, 3, row);

        submitButton = new Button("提交修改");
        submitButton.setOnAction(e -> submitChanges());
        
        modifyBox.getChildren().addAll(infoGrid, submitButton);
        modifyBox.setVisible(false);

        // 切换操作类型的监听器
        operationGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            clearFields();
            modifyBox.setVisible(false);
        });

        // 添加所有组件到主布局
        content.getChildren().addAll(operationBox, searchBox, modifyBox);

        return content;
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

        if (modifyButton.isSelected()) {
            // 显示学生信息以供修改
            nameField.setText(student.getName());
            genderCombo.setValue(student.getGender());
            classField.setText(student.getClassName());
            idCardField.setText(student.getIdNumber());
            chineseField.setText(String.valueOf(student.getChineseScore()));
            mathField.setText(String.valueOf(student.getMathScore()));
            englishField.setText(String.valueOf(student.getEnglishScore()));
            javaField.setText(String.valueOf(student.getJavaScore()));
            modifyBox.setVisible(true);
        } else {
            // 删除学生
            String message = String.format("已找到学生：\n学号：%s\n班级：%s\n是否确认删除？",
                student.getStudentId(), student.getClassName());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
            alert.setTitle("确认删除");
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    masterData.remove(student);
                    showAlert("成功", "学生信息已删除", Alert.AlertType.INFORMATION);
                    clearFields();
                }
            });
        }
    }

    private void submitChanges() {
        String studentId = studentIdField.getText().trim();
        
        // 验证输入
        if (!validateInput()) {
            return;
        }

        // 查找并更新学生信息
        StudentDataService.StudentRecord student = masterData.stream()
            .filter(s -> s.getStudentId().equals(studentId))
            .findFirst()
            .orElse(null);

        if (student != null) {
            student.setName(nameField.getText().trim());
            student.setGender(genderCombo.getValue());
            student.setClassName(classField.getText().trim());
            student.setIdNumber(idCardField.getText().trim());
            student.setChineseScore(Double.parseDouble(chineseField.getText().trim()));
            student.setMathScore(Double.parseDouble(mathField.getText().trim()));
            student.setEnglishScore(Double.parseDouble(englishField.getText().trim()));
            student.setJavaScore(Double.parseDouble(javaField.getText().trim()));

            showAlert("成功", "学生信息已更新", Alert.AlertType.INFORMATION);
            clearFields();
            modifyBox.setVisible(false);
        }
    }

    private boolean validateInput() {
        // 验证必填字段
        if (nameField.getText().trim().isEmpty() ||
            genderCombo.getValue() == null ||
            classField.getText().trim().isEmpty() ||
            idCardField.getText().trim().isEmpty()) {
            showAlert("错误", "请填写所有必填信息", Alert.AlertType.ERROR);
            return false;
        }

        // 验证成绩格式
        try {
            double chinese = Double.parseDouble(chineseField.getText().trim());
            double math = Double.parseDouble(mathField.getText().trim());
            double english = Double.parseDouble(englishField.getText().trim());
            double java = Double.parseDouble(javaField.getText().trim());

            // 验证成绩范围
            if (chinese < 0 || chinese > 100 ||
                math < 0 || math > 100 ||
                english < 0 || english > 100 ||
                java < 0 || java > 100) {
                showAlert("错误", "成绩必须在0-100之间", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("错误", "成绩必须为数字", Alert.AlertType.ERROR);
            return false;
        }

        // 验证身份证号格式（简单验证）
        String idCard = idCardField.getText().trim();
        if (idCard.length() != 18) {
            showAlert("错误", "身份证号必须为18位", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void clearFields() {
        studentIdField.clear();
        nameField.clear();
        genderCombo.setValue(null);
        classField.clear();
        idCardField.clear();
        chineseField.clear();
        mathField.clear();
        englishField.clear();
        javaField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
