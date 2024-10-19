package com.S17Challenge.SpringRestAPI.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Course {

    private Integer id;

    private String name;

    private Integer credit;

    private Grade grade;

}
