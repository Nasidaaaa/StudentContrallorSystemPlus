package tables;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import examples.Student;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Students implements Serializable {
    @TableId
    private String studentid;
    @TableField
    private String name;
    @TableField
    private String gender;
    @TableField
    private String classname;
    @TableField
    private String idnumber;
    @TableField
    private LocalDate birthdate;
    @TableField
    private Double chinesegrade;
    @TableField
    private Double englishgrade;
    @TableField
    private Double mathgrade;
    @TableField
    private Double javagrade;

    public Students(Student student) {
        this.studentid = student.getStudentId();
        this.name = student.getName();
        this.gender = student.getGender();
        this.classname = student.getClassName();
        this.idnumber = student.getIdCardNumber();
        this.birthdate = student.getDate();
        this.chinesegrade = student.getChineseScores();
        this.englishgrade = student.getEnglishScores();
        this.mathgrade = student.getMathScores();
        this.javagrade = student.getJavaScores();
    }
}
