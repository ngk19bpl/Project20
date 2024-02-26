package com.Employee.EmployeeTask.Service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import java.util.List;
import com.Employee.EmployeeTask.Config.Response;
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

    /*
     * Created by Kappala Varalakshmi
     * On 26-02-2024
     */
    Response<Employee> saveEmployee(Employee employee);

    Response<String> deleteEmployee(String id);

    Response<List<Employee>> getAllEmployees();

    Response<Employee> getEmployeeById(String id);

    Response<Employee> updateEmployeeById(String id, Employee updatedEmployee);

    Response<List<Employee>> saveAllEmployees(List<Employee> employees);

    Response<String> deleteEmployeesByIds(List<String> employeeIds);

    List<Employee> findEmployeesWithSorting(String field);
}
