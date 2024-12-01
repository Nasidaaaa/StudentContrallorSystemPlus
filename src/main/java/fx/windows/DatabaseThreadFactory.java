package fx.windows;

import examples.Student;
import mapper.StudentsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import tables.Students;

import java.util.List;

@Component
public class DatabaseThreadFactory {
    
    @Autowired
    private StudentsMapper studentsMapper;
    
    public Thread createInsertDatabaseThread(List<Student> studentsList) {
        return new DatabaseInsertThread(studentsList, studentsMapper);
    }

    public Thread createGetDatabaseThread(List<Student> studentsList) {
        return new DatabaseGetThread(studentsList, studentsMapper);
    }

    public Thread createUpdateDatabaseThread(Students student) {
        return new DatabaseUpdateThread(student, studentsMapper);
    }
}
