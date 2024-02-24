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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1")
public class EmployeeController {

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

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadFile() throws IOException {

        Path path = Paths.get(EmployeeConstant.FILE_PATH);

        byte[] fileContent = Files.readAllBytes(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setContentDispositionFormData("attachment", "employee.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
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

    @PostMapping(value = "/export")
    public ResponseEntity<Object> exportData() {
        try {
            return ResponseEntity.status(200).body(employeeService.exportDataToExcel());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/download/excel")
    public ResponseEntity<byte[]> exportDownloadFile() throws IOException {
        Path path = Paths.get(EmployeeConstant.FILE_PATH);

        if (Files.exists(path)) {
            byte[] fileContent = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setContentDispositionFormData("attachment", "employee.xlsx");

            new Thread(() -> {
                try {
                    Thread.sleep(EmployeeConstant.SLEEP_TIME);
                    Files.deleteIfExists(path);
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

}