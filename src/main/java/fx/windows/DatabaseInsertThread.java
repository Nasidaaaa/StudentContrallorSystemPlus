package fx.windows;

import examples.Student;
import mapper.StudentsMapper;
import tables.Students;

import java.util.List;

public class DatabaseInsertThread extends Thread {
    private final List<Student> studentsList;
    private final StudentsMapper studentsMapper;

    public DatabaseInsertThread(List<Student> studentsList, StudentsMapper studentsMapper) {
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
            for (Student student : studentsList) {
                Students students = new Students(student);
                studentsMapper.insert(students);
                System.out.println("插入学生: " + student.getName());
            }
            System.out.println("数据插入数据库成功！");
        } catch (Exception e) {
            System.out.println("数据插入失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
