package fx.windows;

import examples.Student;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScoreInput implements StudentDataGetter {
    @Autowired
    private DatabaseThreadFactory databaseThreadFactory;

    private TextField studentIdField;
    private TextField nameField;
    private ComboBox<String> genderCombo;

    private TextField classField;
    private TextField idCardField;
    private TextField[] scoreFields;



    private Label messageLabel;
    // 添加出生日期相关的字段
    private ComboBox<String> yearCombo;
    private ComboBox<String> monthCombo;
    private ComboBox<String> dayCombo;

    public VBox getContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        // 创建表单网格
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);

        // 基本信息输入
        int row = 0;
        
        // 学号
        grid.add(new Label("学号:"), 0, row);
        studentIdField = new TextField();
        studentIdField.setPromptText("请输入学号（仅限字母和数字）");
        grid.add(studentIdField, 1, row++);

        // 姓名
        grid.add(new Label("姓名:"), 0, row);
        nameField = new TextField();
        nameField.setPromptText("请输入姓名");
        grid.add(nameField, 1, row++);

        // 性别
        grid.add(new Label("性别:"), 0, row);
        genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("男", "女");
        genderCombo.setValue("男"); // 设置默认值为男
        grid.add(genderCombo, 1, row++);

        // 班级
        grid.add(new Label("班级:"), 0, row);
        classField = new TextField();
        classField.setPromptText("请输入班级");
        grid.add(classField, 1, row++);

        // 身份证号码
        grid.add(new Label("身份证号码:"), 0, row);
        idCardField = new TextField();
        idCardField.setPromptText("请输入18位身份证号码");
        grid.add(idCardField, 1, row++);

        // 出生日期
        grid.add(new Label("出生日期:"), 0, row);
        HBox birthDateBox = createBirthDateBox();
        grid.add(birthDateBox, 1, row++);

        // 成绩输入
        String[] subjects = {"语文", "数学", "英语", "Java课程"};
        scoreFields = new TextField[subjects.length];
        
        for (int i = 0; i < subjects.length; i++) {
            grid.add(new Label(subjects[i] + ":"), 0, row);
            scoreFields[i] = new TextField();
            scoreFields[i].setPromptText("0-100");
            grid.add(scoreFields[i], 1, row++);
        }

        // 消息标签
        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: red;");

        // 按钮容器
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        Button submitButton = new Button("提交");
        submitButton.setOnAction(e -> handleSubmit());

        Button clearButton = new Button("清空");
        clearButton.setOnAction(e -> clearFields());

        buttonBox.getChildren().addAll(submitButton, clearButton);

        // 应用样式
        grid.getStyleClass().add("grid-pane");
        submitButton.getStyleClass().add("button");
        clearButton.getStyleClass().add("button");

        // 添加所有组件到主容器
        content.getChildren().addAll(grid, messageLabel, buttonBox);
        
        // 设置滚动面板的增长属性
        VBox.setVgrow(grid, Priority.ALWAYS);
        
        // 添加输入验证
        addInputValidation();
        
        return content;
    }

    /**
     * 创建出生日期选择框
     * @return 包含年月日选择框的HBox
     */
    private HBox createBirthDateBox() {
        HBox birthDateBox = new HBox(10);
        
        // 年份选择框
        yearCombo = new ComboBox<>();
        for (int year = 2000; year <= 2024; year++) {
            yearCombo.getItems().add(String.valueOf(year));
        }
        yearCombo.setValue("2010");
        yearCombo.setPromptText("年");

        // 月份选择框
        monthCombo = new ComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.getItems().add(String.format("%02d", month));
        }
        monthCombo.setValue("01");
        monthCombo.setPromptText("月");

        // 日期选择框
        dayCombo = new ComboBox<>();
        updateDayComboBox(dayCombo, 1);
        dayCombo.setPromptText("日");

        // 当月份改变时更新日期选项
        monthCombo.setOnAction(e -> 
            updateDayComboBox(dayCombo, Integer.parseInt(monthCombo.getValue()))
        );

        birthDateBox.getChildren().addAll(yearCombo, monthCombo, dayCombo);
        return birthDateBox;
    }

    /**
     * 根据年月更新日期下拉框的可选值
     * @param dayCombo 日期下拉框
     * @param month 月份
     */
    private void updateDayComboBox(ComboBox<String> dayCombo, int month) {
        String currentDay = dayCombo.getValue();
        dayCombo.getItems().clear();
        
        int maxDay = getDaysInMonth(month);
        for (int day = 1; day <= maxDay; day++) {
            dayCombo.getItems().add(String.format("%02d", day));
        }
        
        // 如果之前选择的日期仍然有效，则保持选择
        if (currentDay != null && dayCombo.getItems().contains(currentDay)) {
            dayCombo.setValue(currentDay);
        } else {
            dayCombo.setValue("01");
        }
    }

    /**
     * 获取指定月份的天数
     * @param month 月份
     * @return 该月的天数
     */
    private int getDaysInMonth(int month) {
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return 28; // 简化处理，不考虑闰年
            default:
                return 31;
        }
    }

    /**
     * 获取当前选择的出生日期
     * @return 格式化的日期字符串 (yyyy-MM-dd)
     */
    private String getBirthDate() {
        return String.format("%s-%s-%s", 
            yearCombo.getValue(),
            monthCombo.getValue(),
            dayCombo.getValue()
        );
    }

    private void addInputValidation() {
        // 学号验证（只允许字母和数字）
        studentIdField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isValidStudentId(newValue)) {
                studentIdField.setText(oldValue);
            }
        });

        // 身份证号码验证
        idCardField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 18) {
                idCardField.setText(oldValue);
            }
        });

        // 成绩验证（0-100的数字）
        for (TextField scoreField : scoreFields) {
            scoreField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    scoreField.setText(newValue.replaceAll("[^\\d]", ""));
                } else {
                    try {
                        int score = Integer.parseInt(newValue);
                        if (score > 100) {
                            scoreField.setText("100");
                        }
                    } catch (NumberFormatException ignored) {}
                }
            });
        }
    }

    // 验证学号格式（只允许字母和数字）
    private boolean isValidStudentId(String studentId) {
        return studentId.matches("[a-zA-Z0-9]*");
    }

    // 验证身份证号码格式
    private boolean isValidIdCard(String idCard) {
        // 1. 长度必须是18位
        if (idCard.length() != 18) {
            return false;
        }

        // 2. 前17位必须是数字
        if (!idCard.substring(0, 17).matches("\\d+")) {
            return false;
        }

        // 3. 最后一位可以是数字或X/x
        char last = idCard.charAt(17);
        if (!Character.isDigit(last) && last != 'X' && last != 'x') {
            return false;
        }

        // 4. 验证出生日期
        try {
            String year = idCard.substring(6, 10);
            String month = idCard.substring(10, 12);
            String day = idCard.substring(12, 14);
            
            int yearNum = Integer.parseInt(year);
            int monthNum = Integer.parseInt(month);
            int dayNum = Integer.parseInt(day);
            
            // 年份范围检查（假设有效范围为1900-2024）
            if (yearNum < 1900 || yearNum > 2024) {
                return false;
            }
            
            // 月份检查
            if (monthNum < 1 || monthNum > 12) {
                return false;
            }
            
            // 日期检查
            int maxDays;
            switch (monthNum) {
                case 4: case 6: case 9: case 11:
                    maxDays = 30;
                    break;
                case 2:
                    // 闰年判断
                    if ((yearNum % 4 == 0 && yearNum % 100 != 0) || (yearNum % 400 == 0)) {
                        maxDays = 29;
                    } else {
                        maxDays = 28;
                    }
                    break;
                default:
                    maxDays = 31;
            }
            
            if (dayNum < 1 || dayNum > maxDays) {
                return false;
            }
            
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    // 显示错误提示框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // 处理提交按钮点击事件
    private void handleSubmit() {
        // 验证必填字段
        if (!validateFields()) {
            return;
        }

        // 获取所有输入值
        String studentId = studentIdField.getText();
        String name = nameField.getText();
        String gender = genderCombo.getValue();
        String className = classField.getText();
        String idCard = idCardField.getText();
        double[] scores = new double[scoreFields.length];
        String date = getBirthDate(); // 获取出生日期
        LocalDate birthDate = LocalDate.parse(date);

        // 验证身份证格式
        if (!isValidIdCard(idCard)) {
            showAlert("输入错误", "身份证号码格式不正确！");
            return;
        }

        // 收集成绩
        for (int i = 0; i < scoreFields.length; i++) {
            try {
                scores[i] = Double.parseDouble(scoreFields[i].getText());
            } catch (NumberFormatException e) {
                scores[i] = 0;
            }
        }

        //将已经检查的数据导入到studentsList
        importData(studentId,name,gender,className,idCard,birthDate,scores[0],scores[1],scores[2],scores[3]);
        databaseThreadFactory.createDatabaseThread(studentsList).start();

        // 显示成功消息
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText("学生信息提交成功！");
        alert.showAndWait();

        // 清空表单
        clearFields();
    }

    //将数据导入到studentsList
    private void importData(String studentId,String name,String gender,String className,String idCard,LocalDate birthDate,double chinese,double math,double english,double java) {
        studentsList.add(new Student(name,gender,studentId,idCard,birthDate,className,chinese,math,english,java));
        studentsList.forEach(System.out::println);
    }

    // 验证所有字段
    private boolean validateFields() {
        StringBuilder errorMessage = new StringBuilder();

        if (studentIdField.getText().trim().isEmpty()) {
            errorMessage.append("请输入学号\n");
        } else if (!isValidStudentId(studentIdField.getText().trim())) {
            errorMessage.append("学号只能包含字母和数字\n");
        }

        if (nameField.getText().trim().isEmpty()) {
            errorMessage.append("请输入姓名\n");
        }

        if (classField.getText().trim().isEmpty()) {
            errorMessage.append("请输入班级\n");
        }

        if (idCardField.getText().trim().isEmpty()) {
            errorMessage.append("请输入身份证号码\n");
        }

        // 验证成绩
        String[] subjects = {"语文", "数学", "英语", "Java课程"};
        for (int i = 0; i < scoreFields.length; i++) {
            String score = scoreFields[i].getText().trim();
            if (score.isEmpty()) {
                errorMessage.append("请输入").append(subjects[i]).append("成绩\n");
            } else {
                try {
                    int scoreValue = Integer.parseInt(score);
                    if (scoreValue < 0 || scoreValue > 100) {
                        errorMessage.append(subjects[i]).append("成绩必须在0-100之间\n");
                    }
                } catch (NumberFormatException e) {
                    errorMessage.append(subjects[i]).append("成绩必须是数字\n");
                }
            }
        }

        if (errorMessage.length() > 0) {
            showAlert("输入错误", errorMessage.toString());
            return false;
        }

        return true;
    }

    //清空表单
    private void clearFields() {
        studentIdField.clear();
        nameField.clear();
        genderCombo.setValue("男"); // 重置为默认值"男"
        classField.clear();
        idCardField.clear();
        for (TextField scoreField : scoreFields) {
            scoreField.clear();
        }
        messageLabel.setText("");
        yearCombo.setValue("2010");
        monthCombo.setValue("01");
        updateDayComboBox(dayCombo, 1);
    }
}
