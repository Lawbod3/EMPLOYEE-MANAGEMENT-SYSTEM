package com.darum.employee.documentations;

import com.darum.employee.dto.response.EmployeeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.darum.employee.dto.request.CreateEmployeeRequest;
import com.darum.employee.dto.request.UpdateEmployeeStatusRequest;




/**
 * 📘 Swagger documentation annotations for Employee Admin APIs (WebFlux)
 */


public class AdminApiDocs {

    //  CREATE EMPLOYEE
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Employee created successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class)) // ✅ FIXED!
            ),
            @ApiResponse(responseCode = "400", description = "Invalid department or input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token"),
            @ApiResponse(responseCode = "403", description = "Access denied - only Admin/SuperAdmin"),
            @ApiResponse(responseCode = "409", description = "User already exists as an employee")
    })
    @RequestBody(
            required = true,
            description = "Employee creation data",
            content = @Content(
                    schema = @Schema(implementation = CreateEmployeeRequest.class),
                    examples = @ExampleObject(
                            name = "Create Employee Example",
                            value = """
                            {
                              "email": "employee@example.com",
                              "department": "HUMAN_RESOURCES"
                            }
                            """
                    )
            )
    )
    public @interface CreateEmployeeDoc {}

    //  GET ALL EMPLOYEES
    @Operation(
            summary = "Get all employees",
            description = "Retrieves a list of all employees in the system. Requires Admin or SuperAdmin access.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public @interface GetAllEmployeesDoc {}

    //  UPDATE EMPLOYEE STATUS
    @Operation(
            summary = "Update employee status",
            description = "Allows Admin or SuperAdmin to update the status of an employee (e.g., ACTIVE, INACTIVE, SUSPENDED).",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee status updated successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @RequestBody(
            required = true,
            description = "Status update request",
            content = @Content(
                    schema = @Schema(implementation = UpdateEmployeeStatusRequest.class),
                    examples = @ExampleObject(
                            name = "Update Status Example",
                            value = """
                            {
                              "email": "employee@example.com",
                              "status": "INACTIVE"
                            }
                            """
                    )
            )
    )
    public @interface UpdateEmployeeStatusDoc {}
}
