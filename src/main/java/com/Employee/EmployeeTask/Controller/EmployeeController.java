package com.Employee.EmployeeTask.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public class EmployeeController {
    
    @GetMapping("/aws")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("spring boot application is running on Amazon EC2 Instance!!!!....");
    }
}
