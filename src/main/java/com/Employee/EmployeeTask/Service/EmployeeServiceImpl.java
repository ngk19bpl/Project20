package com.Employee.EmployeeTask.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Employee.EmployeeTask.Repository.EmployeeRepository;
import com.Employee.EmployeeTask.entity.Employee;

import java.io.IOException;
import java.util.List;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.lang.reflect.Field;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

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

            try (FileOutputStream fileOut = new FileOutputStream(
                    "D:/kafka/import_export/import-export/src/main/resources/templates/employee.xlsx")) {
                workbook.write(fileOut);
            }
            System.out.println("Excel file generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return "Failed to import data: " + e.getMessage();
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
}