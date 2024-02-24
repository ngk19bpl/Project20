package com.Employee.EmployeeTask.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

        String filePath = "D:/kafka/import_export/import-export/src/main/resources/templates/employee.xlsx";
        Path path = Paths.get(filePath);

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

}