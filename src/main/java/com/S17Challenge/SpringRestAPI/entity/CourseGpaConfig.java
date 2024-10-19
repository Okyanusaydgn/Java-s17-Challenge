package com.S17Challenge.SpringRestAPI.entity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseGpaConfig {

    @Bean
    public CourseGpa lowCourseGpa() {
        return new LowCourseGpa();
    }

    @Bean
    public CourseGpa mediumCourseGpa() {
        return new MediumCourseGpa();
    }

    @Bean
    public CourseGpa highCourseGpa() {
        return new HighCourseGpa();
    }
}
