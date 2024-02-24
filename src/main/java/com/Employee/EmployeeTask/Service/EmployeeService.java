package com.Employee.EmployeeTask.Service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.util.List;
import com.Employee.EmployeeTask.entity.Employee;

public interface EmployeeService {
    String importExcelData(MultipartFile file);

    void generateExcel() throws IOException;

    /*
     * Created by Anand Swaroop Redyy Pittu
     * On 24-02-2024
     */
    String exportDataToExcel();

    List<Employee> getAllProfilePictures();

    void uploadImage(MultipartFile file);

    Optional<Employee> getImageById(String profilePicture);

}
