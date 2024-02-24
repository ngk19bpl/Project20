package com.Employee.EmployeeTask.Service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {
    String importExcelData(MultipartFile file);
    void generateExcel() throws IOException;
    String exportDataToExcel();
}
