package fx.windows;

import examples.Student;
import mapper.StudentsMapper;
import tables.Students;

public class DatabaseUpdateThread extends Thread {
    private final Students student;
    private final StudentsMapper studentsMapper;

    public DatabaseUpdateThread(Students student, StudentsMapper studentsMapper) {
        this.student = student;
        this.studentsMapper = studentsMapper;
    }

    @Override
    public void run() {
        if (studentsMapper == null) {
            System.out.println("studentsMapper为空！");
            return;
        }
        if (student == null) {
            System.out.println("student为空！");
            return;
        }
        try {
            // 使用MyBatis-Plus的updateById方法更新学生信息
            studentsMapper.updateById(student);
            System.out.println("更新学生信息成功：" + student.getName());
        } catch (Exception e) {
            System.out.println("更新失败：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
