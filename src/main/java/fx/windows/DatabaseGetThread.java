package fx.windows;

import examples.Student;
import mapper.StudentsMapper;
import tables.Students;
import java.util.List;
import examples.Student;
import mapper.StudentsMapper;
import java.util.List;

public class DatabaseGetThread extends  Thread{
    private final List<Student> studentsList;
    private final StudentsMapper studentsMapper;

    public DatabaseGetThread(List<Student> studentsList, StudentsMapper studentsMapper) {
        this.studentsList = studentsList;
        this.studentsMapper = studentsMapper;
    }

    @Override
    public void run() {
        if (studentsMapper == null) {
            System.out.println("studentsMapper为空！");
            return;
        }
        if (studentsList == null) {
            System.out.println("studentsList为空！");
            return;
        }
        try {
            List<Students> StudentsList = studentsMapper.selectList(null);
            for (Students students : StudentsList){
                studentsList.add(new Student(students));
            }
            System.out.println("数据插入数据库成功！");
        } catch (Exception e) {
            System.out.println("数据插入失败：" + e.getMessage());
            e.printStackTrace();
        }


    }
}
