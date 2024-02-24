package com.Employee.EmployeeTask.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.Employee.EmployeeTask.Constant.EmployeeConstant;
import com.Employee.EmployeeTask.Service.EmployeeService;
import com.Employee.EmployeeTask.entity.Employee;

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

    private final String UPLOAD_FOLDER = "Project20/src/main/java/com/Employee/uploads/";

    @Autowired
    private EmployeeService employeeService;

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
    public ResponseEntity<byte[]> getImages(@PathVariable("profile_picture") String profilePicture) 
    {
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
}