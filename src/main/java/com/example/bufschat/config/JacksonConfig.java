package com.example.bufschat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        // LocalDateTime 등 Java8 날짜 타입을 처리하기 위한 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        
        // 숫자타임스탬프 대신 ISO-8601 문자열로 출력하려면 아래 옵션 추가 (선택)
        objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
}