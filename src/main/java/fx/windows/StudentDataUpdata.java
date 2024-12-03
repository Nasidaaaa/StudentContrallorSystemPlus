package fx.windows;

import examples.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private ObservableList<Object> masterData = FXCollections.observableArrayList();
    private TextField studentIdField;
    private TextField nameField;
    private ComboBox<String> genderComboBox;
    private TextField classNameField;
    private TextField idNumberField;
    private TextField chineseScoreField;
    private TextField mathScoreField;
    private TextField englishScoreField;
    private TextField javaScoreField;
    private Button searchButton;
    private Button submitButton;
    private Button resetButton;
    private Button deleteButton;
    private GridPane formGrid;

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

        resetButton = new Button("重置");
        resetButton.setOnAction(e -> clearForm());

        deleteButton = new Button("删除学生");
        deleteButton.setOnAction(e -> deleteStudent());
        deleteButton.setDisable(true);

        searchBox.getChildren().addAll(idLabel, studentIdField, searchButton, submitButton, resetButton, deleteButton);

        // 创建表单
        setupForm();

        // 添加组件到主布局
        content.getChildren().addAll(searchBox, formGrid);

        return content;
    }

    private void setupForm() {
        formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(20));
        formGrid.setAlignment(Pos.CENTER);

        // 创建所有输入字段
        nameField = new TextField();
        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("男", "女");
        classNameField = new TextField();
        idNumberField = new TextField();
        chineseScoreField = new TextField();
        mathScoreField = new TextField();
        englishScoreField = new TextField();
        javaScoreField = new TextField();

        // 添加标签和输入字段到表单
        int row = 0;
        formGrid.add(new Label("姓名:"), 0, row);
        formGrid.add(nameField, 1, row++);

        formGrid.add(new Label("性别:"), 0, row);
        formGrid.add(genderComboBox, 1, row++);

        formGrid.add(new Label("班级:"), 0, row);
        formGrid.add(classNameField, 1, row++);

        formGrid.add(new Label("身份证号:"), 0, row);
        formGrid.add(idNumberField, 1, row++);

        formGrid.add(new Label("语文成绩:"), 0, row);
        formGrid.add(chineseScoreField, 1, row++);

        formGrid.add(new Label("数学成绩:"), 0, row);
        formGrid.add(mathScoreField, 1, row++);

        formGrid.add(new Label("英语成绩:"), 0, row);
        formGrid.add(englishScoreField, 1, row++);

        formGrid.add(new Label("Java成绩:"), 0, row);
        formGrid.add(javaScoreField, 1, row);

        // 设置所有字段为禁用状态
        setFormFieldsDisabled(true);
    }

    private void setFormFieldsDisabled(boolean disabled) {
        nameField.setDisable(disabled);
        genderComboBox.setDisable(disabled);
        classNameField.setDisable(disabled);
        idNumberField.setDisable(disabled);
        chineseScoreField.setDisable(disabled);
        mathScoreField.setDisable(disabled);
        englishScoreField.setDisable(disabled);
        javaScoreField.setDisable(disabled);
    }

    private void clearForm() {
        studentIdField.clear();
        nameField.clear();
        genderComboBox.setValue(null);
        classNameField.clear();
        idNumberField.clear();
        chineseScoreField.clear();
        mathScoreField.clear();
        englishScoreField.clear();
        javaScoreField.clear();
        setFormFieldsDisabled(true);
        submitButton.setDisable(true);
        deleteButton.setDisable(true);
    }

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
            clearForm();
            return;
        }

        // 填充表单
        nameField.setText(student.getName());
        genderComboBox.setValue(student.getGender());
        classNameField.setText(student.getClassName());
        idNumberField.setText(student.getIdCardNumber());
        chineseScoreField.setText(String.format("%.1f", student.getChineseScores()));
        mathScoreField.setText(String.format("%.1f", student.getMathScores()));
        englishScoreField.setText(String.format("%.1f", student.getEnglishScores()));
        javaScoreField.setText(String.format("%.1f", student.getJavaScores()));

        // 启用表单字段和提交按钮
        setFormFieldsDisabled(false);
        submitButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    private void submitChanges() {
        // 验证输入
        if (!validateForm()) {
            return;
        }

        // 显示确认对话框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认更新");
        alert.setHeaderText("是否确认提交以下更新？");
        
        String content = String.format(
                "学号: %s\n姓名: %s\n性别: %s\n班级: %s\n身份证号: %s\n语文: %s\n数学: %s\n英语: %s\nJava: %s",
                studentIdField.getText(), nameField.getText(), genderComboBox.getValue(), 
                classNameField.getText(), idNumberField.getText(), chineseScoreField.getText(),
                mathScoreField.getText(), englishScoreField.getText(), javaScoreField.getText()
        );
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 更新studentsList中对应的Student对象
            Student studentToUpdate = studentsList.stream()
                    .filter(s -> s.getStudentId().equals(studentIdField.getText()))
                    .findFirst()
                    .orElse(null);

            if (studentToUpdate != null) {
                // 更新Student对象
                studentToUpdate.setName(nameField.getText());
                studentToUpdate.setGender(genderComboBox.getValue());
                studentToUpdate.setClassName(classNameField.getText());
                studentToUpdate.setIdCardNumber(idNumberField.getText());
                studentToUpdate.setChineseScores(Double.parseDouble(chineseScoreField.getText()));
                studentToUpdate.setMathScores(Double.parseDouble(mathScoreField.getText()));
                studentToUpdate.setEnglishScores(Double.parseDouble(englishScoreField.getText()));
                studentToUpdate.setJavaScores(Double.parseDouble(javaScoreField.getText()));

                // 更新数据库
                try {
                    Students dbStudent = new Students(studentToUpdate);
                    Thread updateThread = databaseThreadFactory.createUpdateDatabaseThread(dbStudent);
                    updateThread.start();
                    updateThread.join();
                    
                    showAlert("成功", "学生信息更新成功", Alert.AlertType.INFORMATION);
                    clearForm();
                } catch (Exception e) {
                    showAlert("错误", "数据库更新失败: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }
    }

    private void deleteStudent() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            showAlert("错误", "请先查找要删除的学生", Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("是否确认删除该学生？");
        alert.setContentText("此操作不可撤销！");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 先从数据库中删除
                Thread deleteThread = databaseThreadFactory.createDeleteDatabaseThread(studentId);
                deleteThread.start();
                deleteThread.join();

                // 从内存列表中删除
                studentsList.removeIf(s -> s.getStudentId().equals(studentId));
                
                showAlert("成功", "学生删除成功", Alert.AlertType.INFORMATION);
                clearForm();
            } catch (Exception e) {
                showAlert("错误", "删除失败: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        // 验证姓名
        if (nameField.getText().trim().isEmpty()) {
            showAlert("错误", "姓名不能为空", Alert.AlertType.ERROR);
            return false;
        }

        // 验证性别
        if (genderComboBox.getValue() == null) {
            showAlert("错误", "请选择性别", Alert.AlertType.ERROR);
            return false;
        }

        // 验证班级
        if (classNameField.getText().trim().isEmpty()) {
            showAlert("错误", "班级不能为空", Alert.AlertType.ERROR);
            return false;
        }

        // 验证身份证号
        if (idNumberField.getText().trim().length() != 18) {
            showAlert("错误", "身份证号必须为18位", Alert.AlertType.ERROR);
            return false;
        }

        // 验证成绩
        try {
            double chinese = Double.parseDouble(chineseScoreField.getText());
            double math = Double.parseDouble(mathScoreField.getText());
            double english = Double.parseDouble(englishScoreField.getText());
            double java = Double.parseDouble(javaScoreField.getText());

            if (chinese < 0 || chinese > 100 || math < 0 || math > 100 ||
                english < 0 || english > 100 || java < 0 || java > 100) {
                showAlert("错误", "成绩必须在0-100之间", Alert.AlertType.ERROR);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("错误", "成绩必须为数字", Alert.AlertType.ERROR);
            return false;
        }

        return true;
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
