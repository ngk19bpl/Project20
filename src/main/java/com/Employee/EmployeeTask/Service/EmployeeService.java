package com.Employee.EmployeeTask.Service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {
    String importExcelData(MultipartFile file);

    void generateExcel() throws IOException;

    /*
     * Created by Anand Swaroop Redyy Pittu
     * On 24-02-2024
     */
    String exportDataToExcel();
}
