package com.example.bufschat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

import java.util.TimeZone;

@SpringBootApplication
public class BufschatApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    public static void main(String[] args) {
        SpringApplication.run(BufschatApplication.class, args);
    }
}
