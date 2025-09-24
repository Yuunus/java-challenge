package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeApiClient;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private final EmployeeApiClient employeeApiClient;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            log.info("Getting all employees");
            List<Employee> employees = employeeApiClient.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error getting all employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        try {
            log.info("Searching employees by name: {}", searchString);
            List<Employee> allEmployees = employeeApiClient.getAllEmployees();
            List<Employee> filteredEmployees = allEmployees.stream()
                    .filter(employee -> employee.getName() != null
                            && employee.getName().toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.toList());
            log.info("Found {} employees matching search: {}", filteredEmployees.size(), searchString);
            return ResponseEntity.ok(filteredEmployees);
        } catch (Exception e) {
            log.error("Error searching employees by name: {}", searchString, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        try {
            log.info("Getting employee by id: {}", id);
            Employee employee = employeeApiClient.getEmployeeById(id);
            if (employee != null) {
                return ResponseEntity.ok(employee);
            } else {
                log.warn("Employee not found with id: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting employee by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            log.info("Getting highest salary of all employees");
            List<Employee> employees = employeeApiClient.getAllEmployees();
            Integer highestSalary = employees.stream()
                    .filter(employee -> employee.getSalary() != null)
                    .mapToInt(Employee::getSalary)
                    .max()
                    .orElse(0);
            log.info("Highest salary found: {}", highestSalary);
            return ResponseEntity.ok(highestSalary);
        } catch (Exception e) {
            log.error("Error getting highest salary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try {
            log.info("Getting top 10 highest earning employee names");
            List<Employee> employees = employeeApiClient.getAllEmployees();
            List<String> topEarnerNames = employees.stream()
                    .filter(employee -> employee.getSalary() != null && employee.getName() != null)
                    .sorted(Comparator.comparing(Employee::getSalary, Comparator.reverseOrder()))
                    .limit(10)
                    .map(Employee::getName)
                    .collect(Collectors.toList());
            log.info("Found {} top earning employees", topEarnerNames.size());
            return ResponseEntity.ok(topEarnerNames);
        } catch (Exception e) {
            log.error("Error getting top 10 highest earning employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeInput employeeInput) {
        try {
            log.info("Creating new employee: {}", employeeInput.getName());
            Employee createdEmployee = employeeApiClient.createEmployee(employeeInput);
            return ResponseEntity.ok(createdEmployee);
        } catch (Exception e) {
            log.error("Error creating employee: {}", employeeInput.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        try {
            log.info("Deleting employee by id: {}", id);
            Employee employee = employeeApiClient.getEmployeeById(id);
            if (employee == null) {
                log.warn("Employee not found for deletion with id: {}", id);
                return ResponseEntity.notFound().build();
            }

            boolean deleted = employeeApiClient.deleteEmployee(employee.getName());
            if (deleted) {
                log.info("Successfully deleted employee: {}", employee.getName());
                return ResponseEntity.ok(employee.getName());
            } else {
                log.error("Failed to delete employee: {}", employee.getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete employee");
            }
        } catch (Exception e) {
            log.error("Error deleting employee by id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting employee");
        }
    }
}
