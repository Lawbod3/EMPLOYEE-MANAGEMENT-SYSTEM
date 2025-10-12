package com.darum.employee.service;

import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.dto.response.EmployeeResponse;
import com.darum.employee.model.Employee;
import com.darum.employee.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final WebClient authWebClient;

    public Mono<EmployeeResponse> createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        return null;
    }
}
