package com.yang.studentcontrallorsystemplus;

import examples.Student;
import jakarta.annotation.Resource;
import mapper.StudentsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import tables.Students;

import java.util.List;

@SpringBootTest
class StudentContrallorSystemPlusApplicationTests {


	@Resource
	StudentsMapper studentsMapper;

	@Test
	void contextLoads() {
		List<Students> studentsList = studentsMapper.selectList(null);
		studentsList.forEach(System.out::println);
	}

}
