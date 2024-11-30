package com.yang.studentcontrallorsystemplus;

import com.sun.tools.javac.Main;
import fx.StudentManagementApp;
import jakarta.annotation.Resource;
import mapper.StudentsMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tables.Students;

import java.util.List;

import static javafx.application.Application.launch;

@SpringBootApplication
@MapperScan("mapper")
public class StudentContrallorSystemPlusApplication {




	public static void main(String[] args) {


			// 启动Spring Boot应用程序
			SpringApplication.run(Main.class, args);
			// 启动JavaFX应用程序
			launchJavaFXApp(args);

	}



	// 使用新的线程启动JavaFX应用程序
	private static void launchJavaFXApp(String[] args) {

		new Thread(() -> {
			StudentManagementApp.main(args); // 启动JavaFX应用程序
		}).start();
	}

}
