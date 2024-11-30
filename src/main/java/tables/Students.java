package tables;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class Students implements Serializable {
    @TableId
    private  String studentid;
    @TableField
    private  String name;
    @TableField
    private  String gender;
    @TableField
    private  String classname;
    @TableField
    private  String idnumber;
    @TableField
    private LocalDate birthdate;
    @TableField
    private  Double chinesegrade;
    @TableField
    private  Double englishgrade;
    @TableField
    private  Double mathgrade;
    @TableField
    private  Double javagrade;

}
