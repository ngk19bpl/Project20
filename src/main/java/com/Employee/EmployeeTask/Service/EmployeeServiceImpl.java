package com.Employee.EmployeeTask.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.Employee.EmployeeTask.Config.Response;
import com.Employee.EmployeeTask.Constant.EmployeeConstant;
import com.Employee.EmployeeTask.Exception.EmployeeException;
import com.Employee.EmployeeTask.Repository.EmployeeRepository;
import com.Employee.EmployeeTask.entity.Employee;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;
import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Files;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Value("${upload.folder}")
    private String uploadFolder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void generateExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Employee Data");

            Row headerRow = sheet.createRow(0);
            List<String> columnNames = getColumnNames();

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columnNames.size(); i++) {
                createHeaderCell(headerRow, i, columnNames.get(i), headerCellStyle);

                sheet.setColumnWidth(i, 15 * 256);
            }

            try (FileOutputStream fileOut = new FileOutputStream(EmployeeConstant.FILE_PATH)) {
                workbook.write(fileOut);
            }
            System.out.println("Excel file generated successfully.");
        } catch (IOException e) {
            throw new EmployeeException("employee data export errror" + e);
        }
    }

    private List<String> getColumnNames() {
        Class<Employee> entityClass = Employee.class;
        Field[] fields = entityClass.getDeclaredFields();
        List<String> columnNames = new ArrayList<>();
        for (Field field : fields) {
            if (!field.getName().equals("id")) {
                columnNames.add(field.getName());
            }
        }
        return columnNames;
    }

    private static void createHeaderCell(Row headerRow, int columnIndex, String value, CellStyle style) {
        Cell headerCell = headerRow.createCell(columnIndex);
        headerCell.setCellValue(value);
        headerCell.setCellStyle(style);
    }

    @Override
    public String importExcelData(MultipartFile file) {
        List<Employee> importedEmployees = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    continue;
                }
                Employee employee = new Employee();
                employee.setFirstName(getStringValue(currentRow.getCell(0)));
                employee.setLastName(getStringValue(currentRow.getCell(1)));
                employee.setEmail(getStringValue(currentRow.getCell(2)));
                employee.setContact(getStringValue(currentRow.getCell(3)));
                Cell salaryCell = currentRow.getCell(4);
                if (salaryCell != null) {
                    if (salaryCell.getCellType() == CellType.NUMERIC) {
                        employee.setSalary(salaryCell.getNumericCellValue());
                    } else if (salaryCell.getCellType() == CellType.STRING) {
                        try {
                            employee.setSalary(Double.parseDouble(salaryCell.getStringCellValue()));
                        } catch (NumberFormatException e) {
                            employee.setSalary(0.0);
                        }
                    }
                }
                employee.setDepartment(getStringValue(currentRow.getCell(5)));
                employee.setAddress(getStringValue(currentRow.getCell(6)));
                employee.setGender(getStringValue(currentRow.getCell(7)));
                Date dob = currentRow.getCell(8).getDateCellValue();
                employee.setDob(dob);
                employee.setEmployeeStatus(getStringValue(currentRow.getCell(9)));
                importedEmployees.add(employee);
            }
        } catch (IOException e) {
            throw new EmployeeException("employee data export errror" + e);
        }

        employeeRepository.saveAll(importedEmployees);

        return "Data imported successfully";
    }

    private String getStringValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                return cell.getStringCellValue();
            }
        }
        return null;
    }

    /*
     * Created by Anand Swaroop Redyy Pittu
     * On 24-02-2024
     */
    public String exportDataToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("employee.xlsx");
            List<Employee> employees = employeeRepository.findAll();

            int rowNum = 0;
            Row headerRow = sheet.createRow(rowNum++);

            String[] headers = { "EmpID", "FirstName", "LastLame", "Address", "Email", "DOB", "Contact",
                    "Gender", "Department", "Salary", "EmpStatus" };
            int colNum = 0;
            for (String header : headers) {
                Cell cell = headerRow.createCell(colNum++);
                cell.setCellValue(header);

                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            for (Employee employee : employees) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(employee.getId());
                row.createCell(1).setCellValue(employee.getFirstName());
                row.createCell(2).setCellValue(employee.getLastName());
                row.createCell(3).setCellValue(employee.getAddress());
                row.createCell(4).setCellValue(employee.getEmail());
                row.createCell(5).setCellValue(employee.getDob());
                row.createCell(6).setCellValue(employee.getContact());
                row.createCell(7).setCellValue(employee.getGender());
                row.createCell(8).setCellValue(employee.getDepartment());
                row.createCell(9).setCellValue(employee.getSalary());
                row.createCell(10).setCellValue(employee.getEmployeeStatus());
            }
            try (FileOutputStream fileOut = new FileOutputStream(EmployeeConstant.FILE_PATH_2)) {
                workbook.write(fileOut);
            }

            return "Data exported to Excel successfully!";
        } catch (IOException e) {
            throw new EmployeeException("employee data export errror" + e);

        }
    };

    @Override
    public List<Employee> getAllProfilePictures() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> getImageById(String profilePicture) {
        return employeeRepository.findByProfilePicture(profilePicture);
    }

    @Override
    public void uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            byte[] imageData = file.getBytes();
            Path folderPath = Paths.get(uploadFolder);
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            Employee employee = new Employee();
            employeeRepository.save(employee);
            String id = employee.getId();

            String originalFilename = file.getOriginalFilename().toLowerCase();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            String fileName = id + fileExtension;
            employee.setProfilePicture(fileName);
            Path filePath = Paths.get(uploadFolder + File.separator + fileName);
            Files.write(filePath, imageData);
            employeeRepository.save(employee);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Created by Kappala Varalakshmi
     * On 26-02-2024
     */

    @Override
    public Response<Employee> saveEmployee(Employee employee) {
        try {
            Employee savedEmployee = employeeRepository.save(employee);
            logger.info("Employee saved successfully: {}", savedEmployee);
            // Send welcome email with attachment
            sendWelcomeEmail(savedEmployee);
            return new Response<>(savedEmployee, "Employee saved successfully");
        } catch (Exception e) {
            logger.error("Error saving employee: {}", e.getMessage());
            return new Response<>("Error saving employee: " + e.getMessage());
        }
    }

    /* created by Divya Gattu */
    /* 26/2/2024 */

    private void sendWelcomeEmail(Employee employee) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(employee.getEmail());
            helper.setSubject("Welcome to the Bluepal");
            String htmlContent = "<p>Dear " + employee.getFirstName() + ",</p>"
                    + "<p>Welcome to our company!</p>";
            helper.setText(htmlContent, true);
            ClassPathResource classPathResource = new ClassPathResource("office.jpg");
            helper.addAttachment(classPathResource.getFilename(), classPathResource);

            javaMailSender.send(mimeMessage);
            logger.info("Welcome email sent successfully to: {}", employee.getEmail());
        } catch (MessagingException e) {
            logger.error("Error sending welcome email: {}", e.getMessage());
        }
    }

    @Override
    public Response<String> deleteEmployee(String id) {
        try {
            Optional<Employee> optionalEmployee = employeeRepository.findById(id);
            if (optionalEmployee.isPresent()) {
                employeeRepository.deleteById(id);
                logger.info("Employee deleted successfully with ID: {}", id);
                return new Response<>(1, "Employee deleted successfully");
            } else {
                logger.error("Employee not found with ID: {}", id);
                return new Response<>("Employee not found. Unable to delete.");
            }
        } catch (Exception e) {
            logger.error("Error deleting employee with ID {}: {}", id, e.getMessage());
            return new Response<>("Error deleting employee: " + e.getMessage());
        }
    }

    @Override
    public Response<List<Employee>> getAllEmployees() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            logger.info("Fetched all employees successfully");
            return new Response<>(employees, "Employees fetched successfully");
        } catch (Exception e) {
            logger.error("Error fetching employees: {}", e.getMessage());
            return new Response<>("Error fetching employees: " + e.getMessage());
        }
    }

    @Override
    public Response<Employee> getEmployeeById(String id) {
        try {
            Optional<Employee> Employee = employeeRepository.findById(id);
            if (Employee.isPresent()) {
                Employee employee = Employee.get();
                logger.info("Employee fetched successfully with ID: {}", id);
                return new Response<>(employee, "Employee fetched successfully");
            } else {
                logger.error("Employee not found with ID: {}", id);
                return new Response<>("Employee not found");
            }
        } catch (Exception e) {
            logger.error("Error fetching employee with ID {}: {}", id, e.getMessage());
            return new Response<>("Error fetching employee: " + e.getMessage());
        }
    }

    @Override
    public Response<Employee> updateEmployeeById(String id, Employee updatedEmployee) {
        try {
            Optional<Employee> Employee = employeeRepository.findById(id);
            if (Employee.isPresent()) {
                Employee existingEmployee = Employee.get();

                Field[] fields = Employee.class.getDeclaredFields();
                for (Field field : fields) {
                    String fieldName = field.getName();

                    if (!fieldName.equals("id") && !fieldName.equals("contact") && !fieldName.equals("salary")) {
                        field.setAccessible(true);
                        Object value = field.get(updatedEmployee);

                        if (value != null) {
                            field.set(existingEmployee, value);
                        }
                    }
                }

                Employee savedEmployee = employeeRepository.save(existingEmployee);
                logger.info("Employee updated successfully with ID: {}", id);
                return new Response<>(savedEmployee, "Employee updated successfully");
            } else {
                logger.error("Employee not found with ID: {}", id);
                return new Response<>("Employee not found");
            }
        } catch (Exception e) {
            logger.error("Error updating employee with ID {}: {}", id, e.getMessage());
            return new Response<>("Error updating employee: " + e.getMessage());
        }
    }

    @Override
    public Response<List<Employee>> saveAllEmployees(List<Employee> employees) {
        try {
            List<Employee> savedEmployees = employeeRepository.saveAll(employees);
            logger.info("Employees inserted successfully");
            return new Response<>(savedEmployees, "Employees inserted successfully");
        } catch (Exception e) {
            logger.error("Error inserting employees: {}", e.getMessage());
            return new Response<>("Error inserting employees: " + e.getMessage());
        }
    }

    @Override
    public Response<String> deleteEmployeesByIds(List<String> employeeIds) {
        try {
            for (String id : employeeIds) {
                if (!employeeRepository.existsById(id)) {
                    logger.error("Employee not found with ID: {}", id);
                    return new Response<>("Employee with ID " + id + " not found. Unable to delete.");
                }
            }
            employeeIds.forEach(employeeRepository::deleteById);
            logger.info("Employees deleted successfully: {}", employeeIds);
            return new Response<>(1, "Employees Deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting employees: {}", e.getMessage());
            return new Response<>(0, "Error deleting employees: " + e.getMessage());
        }
    }

    /* Ram 26-02-2024 soting base don column names */
    @Override
    public List<Employee> findEmployeesWithSorting(String field) {

        try {
            List<Employee> employees = employeeRepository.findAll(Sort.by(Sort.Direction.ASC, field));
            logger.info("Successfully retrieved employees with sorting by field '{}'", field);
            return employees;
        } catch (Exception e) {
            logger.error("Error occurred while retrieving employees with sorting by field '{}'", field, e);
            return List.of();
        }
    }

    /*
     * Created by Karima Shaik
     */

    @Override
    public List<Employee> searchEmployeesByFilter(String searchTerm) {
        try {
            if (searchTerm != null && !searchTerm.isEmpty()) {
                return employeeRepository.searchEmployeesByFilter(searchTerm);
            } else {
                return employeeRepository.findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}