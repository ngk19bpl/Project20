package com.Employee.EmployeeTask.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
    
    @GetMapping("/aws")
    public String helloworld(){
        return ("spring boot application is running on Amazon EC2 Instance!!!!....");
    }
}
