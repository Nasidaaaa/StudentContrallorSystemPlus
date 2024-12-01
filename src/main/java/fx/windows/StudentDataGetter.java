package fx.windows;

import examples.Student;

import java.util.ArrayList;
import java.util.List;

//接口实现了创建List<Student> studentsList，并且默认有获取List<Student> studentsList的方法
public interface StudentDataGetter {
    List<Student> studentsList = new ArrayList<>();
    default List<Student> getStudentsList() {
        return studentsList;
    }
}
