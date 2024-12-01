package fx.windows;

import examples.Student;
import mapper.StudentsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseThreadFactory {
    
    @Autowired
    private StudentsMapper studentsMapper;
    
    public Thread createDatabaseThread(List<Student> studentsList) {
        return new DatabaseInsertThread(studentsList, studentsMapper);
    }
}
