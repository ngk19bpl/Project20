package com.Employee.EmployeeTask.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Employee.EmployeeTask.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

}

