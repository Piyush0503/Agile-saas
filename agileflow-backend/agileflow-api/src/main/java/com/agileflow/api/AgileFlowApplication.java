package com.agileflow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.agileflow")
@EntityScan(basePackages = "com.agileflow.core.domain")
@EnableJpaRepositories(basePackages = "com.agileflow.infrastructure.repository")
public class AgileFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgileFlowApplication.class, args);
    }
}
