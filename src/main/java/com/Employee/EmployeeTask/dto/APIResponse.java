package com.Employee.EmployeeTask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<R> {
     int recordCount;
    private String status;
    private String message;
    private R data;
    private String error;
    
}
