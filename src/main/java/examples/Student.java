package examples;

import java.time.LocalDate;

public class Student extends Person {
    private String studentId;
    private String className;
    private double chineseScores;
    private double englishScores;
    private double mathScores;
    private double javaScores;

    public Student(int year, int month, int day, String name, String gender, String idCardNumber, String studentId, String className, double chineseScores, double englishScores, double mathScores, double javaScores) {
        super(year, month, day, name, gender, idCardNumber);
        this.studentId = studentId;
        this.className = className;
        this.chineseScores = chineseScores;
        this.englishScores = englishScores;
        this.mathScores = mathScores;
        this.javaScores = javaScores;
    }

    public Student(LocalDate date, String name, String gender, String idCardNumber, String studentId, String className, double chineseScores, double englishScores, double mathScores, double javaScores) {
        super(date, name, gender, idCardNumber);
        this.studentId = studentId;
        this.className = className;
        this.chineseScores = chineseScores;
        this.englishScores = englishScores;
        this.mathScores = mathScores;
        this.javaScores = javaScores;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public double getChineseScores() {
        return chineseScores;
    }

    public void setChineseScores(double chineseScores) {
        this.chineseScores = chineseScores;
    }

    public double getEnglishScores() {
        return englishScores;
    }

    public void setEnglishScores(double englishScores) {
        this.englishScores = englishScores;
    }

    public double getMathScores() {
        return mathScores;
    }

    public void setMathScores(double mathScores) {
        this.mathScores = mathScores;
    }

    public double getJavaScores() {
        return javaScores;
    }

    public void setJavaScores(double javaScores) {
        this.javaScores = javaScores;
    }
}