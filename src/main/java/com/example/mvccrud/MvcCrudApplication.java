package com.example.mvccrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MvcCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcCrudApplication.class, args);
    }

}
