package com.example.spring_integration_sftp.service;

import com.example.spring_integration_sftp.entity.Employee;
import com.example.spring_integration_sftp.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class FileProcessingService {

    private final EmployeeRepository repository;

    public FileProcessingService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<Employee> processCsvFile(Path csvPath) throws IOException {
        List<Employee> saved = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csvPath)) {
            String line;
            boolean headerSkipped = false;
            while ((line = br.readLine()) != null) {
                if (!headerSkipped) {
                    // if header contains "name" word, skip it
                    if (line.toLowerCase().contains("name") && line.toLowerCase().contains("age")) {
                        headerSkipped = true;
                        continue;
                    }
                }
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                String name = parts[0].trim();
                Integer age = null;
                Double salary = null;
                try {
                    age = Integer.valueOf(parts[1].trim());
                } catch (Exception e) { /* leave null */ }
                try {
                    salary = Double.valueOf(parts[2].trim());
                } catch (Exception e) { /* leave null */ }

                Employee emp = Employee.builder()
                        .name(name)
                        .age(age)
                        .salary(salary)
                        .build();
                saved.add(emp);
            }
        }

        if (!saved.isEmpty()) {
            return repository.saveAll(saved);
        }
        return Collections.emptyList();
    }
}