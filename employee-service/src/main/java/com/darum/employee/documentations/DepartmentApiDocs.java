package com.darum.employee.documentations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.darum.employee.dto.request.UpdateDepartmentRequest;


/**
 * 📘 Swagger documentation annotations for Department Management APIs (WebFlux)
 */


public class DepartmentApiDocs {

    // 🔹 GET ALL DEPARTMENTS
    @Operation(
            summary = "Get all departments",
            description = "Retrieves all available departments. Requires a valid JWT token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Departments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public @interface GetAllDepartmentsDoc {}


    // 🔹 UPDATE EMPLOYEE DEPARTMENT
    @Operation(
            summary = "Update employee department",
            description = "Allows Admin or SuperAdmin to update the department of an existing employee.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee department updated successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or department not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @RequestBody(
            required = true,
            description = "Department update request",
            content = @Content(
                    schema = @Schema(implementation = UpdateDepartmentRequest.class),
                    examples = @ExampleObject(
                            name = "Update Department Example",
                            value = """
                            {
                              "email": "employee@example.com",
                              "department": "IT"
                            }
                            """
                    )
            )
    )
    public @interface UpdateEmployeeDepartmentDoc {}

}
