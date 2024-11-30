package fx.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ScoreInput {
    private TextField studentIdField;
    private TextField nameField;
    private ComboBox<String> genderCombo;
    private TextField classField;
    private TextField idCardField;
    private TextField[] scoreFields;
    private Label messageLabel;

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
        
        // 添加输入验证
        addInputValidation();
        
        return content;
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
        int[] scores = new int[scoreFields.length];

        // 验证身份证格式
        if (!isValidIdCard(idCard)) {
            showAlert("输入错误", "身份证号码格式不正确！");
            return;
        }

        // 收集成绩
        for (int i = 0; i < scoreFields.length; i++) {
            try {
                scores[i] = Integer.parseInt(scoreFields[i].getText());
            } catch (NumberFormatException e) {
                scores[i] = 0;
            }
        }

        // TODO: 将数据保存到数据库
        // 这里后续会添加数据库操作代码

        // 显示成功消息
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText("学生信息提交成功！");
        alert.showAndWait();

        // 清空表单
        clearFields();
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
    }
}
