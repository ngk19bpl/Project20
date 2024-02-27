package com.Employee.EmployeeTask.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Employee.EmployeeTask.Config.Response;
import com.Employee.EmployeeTask.Constant.EmployeeConstant;
import com.Employee.EmployeeTask.Service.EmployeeService;
import com.Employee.EmployeeTask.dto.APIResponse;
import com.Employee.EmployeeTask.entity.Employee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
    private final String UPLOAD_FOLDER = "Project20/src/main/java/com/Employee/uploads/";

    @Autowired
    private EmployeeService employeeService;

     /*
     * Created by Anand Navya Gunduboina
     * On 27-02-2024
     */
    @Operation(
        summary = "Fetch all employee",
        description = "fetches all employee entities and their data from data source")
     @ApiResponses(value = {
     @ApiResponse(responseCode = "200", description = "successful operation")
})

    @GetMapping("/generate-excel")
    public ResponseEntity<String> generateExcel() {
        try {
            employeeService.generateExcel();
            return ResponseEntity.ok("Excel file generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate Excel file.");
        }
    }

    /*
     * Created by Anand Swaroop Redyy Pittu
     * On 24-02-2024
     */
    @GetMapping(value = "/download/excel")
    public ResponseEntity<byte[]> exportDownloadFile() throws IOException {
        Path path = Paths.get(EmployeeConstant.FILE_PATH);
        Path paths = Paths.get(EmployeeConstant.FILE_PATH_2);
        if (Files.exists(path)) {
            byte[] fileContent = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setContentDispositionFormData("attachment", "employee.xlsx");

            new Thread(() -> {
                try {
                    Thread.sleep(EmployeeConstant.SLEEP_TIME);
                    Files.deleteIfExists(paths);
                    System.out.println("File deleted successfully.");
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }).start();
            return ResponseEntity.ok().headers(headers).body(fileContent);

        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importEmployees(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload an Excel file!");
        }

        try {
            String importedEmployees = employeeService.importExcelData(file);
            return ResponseEntity.ok("Employees imported successfully!" + importedEmployees);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to import Excel data: " + e.getMessage());
        }
    }

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /*
     * Created by Anand Swaroop Redyy Pittu
     * On 24-02-2024
     */
    @PostMapping(value = "/export")
    public ResponseEntity<Object> exportData() {
        try {
            return ResponseEntity.status(200).body(employeeService.exportDataToExcel());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Employee>> getAllProfilePictures() {
        try {
            List<Employee> images = employeeService.getAllProfilePictures();
            Map<String, Object> response = new HashMap<>();
            response.put("msg", "Fetching list of profile pictures");
            return new ResponseEntity<>(images, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/images/{profile_picture}")
    public ResponseEntity<byte[]> getImages(@PathVariable("profile_picture") String profilePicture) {
        Optional<Employee> imageOptional = employeeService.getImageById(profilePicture);
        if (imageOptional.isPresent()) {
            Employee image = imageOptional.get();
            try {
                Path imagePath = Paths.get(UPLOAD_FOLDER, image.getProfilePicture());
                byte[] imageBytes = Files.readAllBytes(imagePath);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", image.getProfilePicture());
                return ResponseEntity.ok().headers(headers).body(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/image/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please select a file to upload", HttpStatus.BAD_REQUEST);
            }
            employeeService.uploadImage(file);
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*
     * Created by Kappala Varalakshmi
     * On 26-02-2024
     */

    @PostMapping("/save")
    public ResponseEntity<Response<Employee>> saveEmployee(@RequestBody Employee employee) {
        try {
            Response<Employee> response = employeeService.saveEmployee(employee);
            logger.info("Employee saved successfully: {}", response.getData());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving employee: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error saving employee: " + e.getMessage()));
        }
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Response<String>> deleteEmployee(@PathVariable String id) {
        try {
            Response<String> response = employeeService.deleteEmployee(id);
            logger.info("Employee deletion response: {}", response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting employee: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error deleting employee: " + e.getMessage()));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Response<List<Employee>>> getAllEmployees() {
        try {
            Response<List<Employee>> employeesResponse = employeeService.getAllEmployees();
            logger.info("Fetched all employees successfully");
            return ResponseEntity.ok(employeesResponse);
        } catch (Exception e) {
            logger.error("Error fetching employees: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error fetching employees: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Employee>> getEmployeeById(@PathVariable String id) {
        try {
            Response<Employee> response = employeeService.getEmployeeById(id);
            logger.info("Employee fetched successfully: {}", response.getData());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching employee: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error fetching employee: " + e.getMessage()));
        }
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Response<Employee>> updateEmployeeById(@PathVariable String id,
            @RequestBody Employee updatedEmployee) {
        try {
            Response<Employee> response = employeeService.updateEmployeeById(id, updatedEmployee);
            logger.info("Employee updated successfully: {}", response.getData());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating employee: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error updating employee: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-insert")
    public ResponseEntity<Response<List<Employee>>> bulkInsertEmployees(@RequestBody List<Employee> employees) {
        try {
            Response<List<Employee>> response = employeeService.saveAllEmployees(employees);
            logger.info("Bulk insert of employees successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error inserting employees: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>("Error inserting employees: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Response<String>> bulkDeleteEmployees(@RequestBody List<String> employeeIds) {
        try {
            Response<String> response = employeeService.deleteEmployeesByIds(employeeIds);
            logger.info("Bulk deletion of employees successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting employees: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new Response<>(0, "Error deleting employees: " + e.getMessage()));
        }
    }
/*applying sorting based on column names in table */
    @GetMapping("/{field}")
    private APIResponse<List<Employee>> getEmployeesWithSort(@PathVariable String field) {
        try {
            List<Employee> allEmployees = employeeService.findEmployeesWithSorting(field);

            APIResponse<List<Employee>> response = new APIResponse<>();
            response.setStatus("1");
            response.setMessage("Employees fetched with sorting successfully");
            response.setData(allEmployees);
            response.setRecordCount(allEmployees.size());

            logger.info("Employees fetched with sorting successfully");

            return response;
        } catch (Exception e) {
            logger.error("Error fetching employees with sorting", e);

            APIResponse<List<Employee>> errorResponse = new APIResponse<>();
            errorResponse.setStatus("0");
            errorResponse.setMessage("Error fetching employees with sorting");
            errorResponse.setError(e.getMessage());

            return errorResponse;
        }
    }

    /*
     * Created by Karima Shaik
     */
    @PostMapping("/search")
    public ResponseEntity<Object> searchEmployee(@RequestBody Map<String, String> requestBody) {
        try {
            String searchTerm = requestBody.get("searchTerm");
            List<Employee> employees = employeeService.searchEmployeesByFilter(searchTerm);

            Map<String, Object> response = Map.of("content", employees);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of("error", "An error occurred while processing the request.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}