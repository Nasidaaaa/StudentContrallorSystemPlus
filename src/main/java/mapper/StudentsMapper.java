package mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tables.Students;

import java.util.List;

@Mapper
public interface StudentsMapper extends BaseMapper<Students> {
    
    @Select("SELECT * FROM students WHERE studentid = #{studentId}")
    Students findByStudentId(@Param("studentId") String studentId);
    
    @Select("SELECT * FROM students WHERE name LIKE CONCAT('%', #{name}, '%')")
    List<Students> findByNameLike(@Param("name") String name);
    
    @Select("SELECT * FROM students WHERE classname = #{className}")
    List<Students> findByClassName(@Param("className") String className);
    
    @Select("SELECT * FROM students WHERE chinese_grade < 60 OR math_grade < 60 OR english_grade < 60 OR java_grade < 60")
    List<Students> findFailingStudents();
    
    @Select("SELECT * FROM students WHERE chinese_grade >= 90 AND math_grade >= 90 AND english_grade >= 90 AND java_grade >= 90")
    List<Students> findExcellentStudents();
}
