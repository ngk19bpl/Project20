package com.Employee.EmployeeTask.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.Employee.EmployeeTask.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findByProfilePicture(String profilePicture);

    @Query("SELECT e FROM Employee e WHERE " +
    "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
    "(LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
    "(LOWER(e.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
    "(LOWER(e.gender) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) OR " +
    "(LOWER(e.department) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
List<Employee> searchEmployeesByFilter(@Param("searchTerm") String searchTerm);
}

