package com.reliaquest.api.service;

import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeApiClient {

    private final RestTemplate restTemplate;

    @Value("${mock.employee.api.url:http://localhost:8112/api/v1/employee}")
    private String baseUrl;

    public List<Employee> getAllEmployees() {
        try {
            log.info("Fetching all employees from mock API");
            ResponseEntity<ApiResponse<List<Employee>>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info(
                        "Successfully fetched {} employees",
                        response.getBody().getData().size());
                return response.getBody().getData();
            }
            log.warn("Unexpected response from mock API: {}", response);
            throw new RuntimeException("Failed to fetch employees from mock API");

        } catch (RestClientException e) {
            log.error("Error fetching employees from mock API", e);
            throw new RuntimeException("Failed to fetch employees", e);
        }
    }

    public Employee getEmployeeById(String id) {
        try {
            log.info("Fetching employee with id: {}", id);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl + "/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully fetched employee with id: {}", id);
                return response.getBody().getData();
            }
            log.warn("Employee not found with id: {}", id);
            return null;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee not found with id: {}", id);
            return null;
        } catch (RestClientException e) {
            log.error("Error fetching employee with id: {}", id, e);
            throw new RuntimeException("Failed to fetch employee", e);
        }
    }

    public Employee createEmployee(EmployeeInput employeeInput) {
        try {
            log.info("Creating new employee: {}", employeeInput.getName());
            HttpEntity<EmployeeInput> entity = new HttpEntity<>(employeeInput);
            ResponseEntity<ApiResponse<Employee>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ApiResponse<Employee>>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info(
                        "Successfully created employee: {}",
                        response.getBody().getData().getName());
                return response.getBody().getData();
            }
            log.error("Failed to create employee: {}", response);
            throw new RuntimeException("Failed to create employee");

        } catch (RestClientException e) {
            log.error("Error creating employee", e);
            throw new RuntimeException("Failed to create employee", e);
        }
    }

    public boolean deleteEmployee(String employeeName) {
        try {
            log.info("Deleting employee: {}", employeeName);
            HttpEntity<DeleteEmployeeRequest> entity = new HttpEntity<>(new DeleteEmployeeRequest(employeeName));
            ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
                    baseUrl, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                boolean deleted = Boolean.TRUE.equals(response.getBody().getData());
                log.info("Employee deletion result for {}: {}", employeeName, deleted);
                return deleted;
            }
            log.warn("Unexpected response when deleting employee: {}", response);
            return false;

        } catch (RestClientException e) {
            log.error("Error deleting employee: {}", employeeName, e);
            throw new RuntimeException("Failed to delete employee", e);
        }
    }

    private static class DeleteEmployeeRequest {
        private String name;

        public DeleteEmployeeRequest(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
