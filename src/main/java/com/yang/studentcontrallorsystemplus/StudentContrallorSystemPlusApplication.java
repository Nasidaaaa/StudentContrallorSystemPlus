package com.yang.studentcontrallorsystemplus;

import fx.StudentManagementApp;
import javafx.application.Application;
import lombok.Getter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"com.yang", "fx", "mapper"})
@MapperScan("mapper")
public class StudentContrallorSystemPlusApplication {

    @Getter
    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        // 启动Spring Boot
        springContext = SpringApplication.run(StudentContrallorSystemPlusApplication.class, args);
        
        // 启动JavaFX
        Application.launch(StudentManagementApp.class, args);
    }

}
