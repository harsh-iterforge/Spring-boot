package com.example.spring_integration_sftp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // important for direct JDBC inserts
    private Long id;

    private String name;
    private Integer age;
    private Double salary;
}