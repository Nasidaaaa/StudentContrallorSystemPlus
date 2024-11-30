package fx.windows;

import examples.Student;

import java.util.ArrayList;
import java.util.List;

public interface StudentDataGetter {
    List<Student> studentsList = new ArrayList<>();
    default List<Student> getStudentsList() {
        studentsList.forEach(System.out::println);
        return studentsList;
    }
}
