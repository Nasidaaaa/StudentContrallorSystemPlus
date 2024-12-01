package com.yang.studentcontrallorsystemplus;

import examples.Student;
import jakarta.annotation.Resource;
import mapper.StudentsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tables.Students;

import java.util.List;

@SpringBootTest
class StudentContrallorSystemPlusApplicationTests {


	@Resource
	StudentsMapper studentsMapperText;

	@Test
	void contextLoads() {
		List<Students> studentsList = studentsMapperText.selectList(null);
		System.out.println("数据库的mapper:"+studentsMapperText);
		studentsList.forEach(System.out::println);
	}

}
