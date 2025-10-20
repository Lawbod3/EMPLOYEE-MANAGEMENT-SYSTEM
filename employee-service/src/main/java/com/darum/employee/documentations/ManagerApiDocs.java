package com.darum.employee.documentations;


import com.darum.employee.dto.request.DemoteManagerRequest;
import com.darum.employee.dto.request.PromoteToManagerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * 📘 Swagger documentation annotations for Manager-related APIs
 * Used in ManagerController endpoints.
 */

public class ManagerApiDocs {

    //  🔹 Promote Employee to Manager
    @Operation(
            summary = "Promote an employee to manager",
            description = "Allows Admin or SuperAdmin to promote an existing employee to manager role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee successfully promoted to manager",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or department"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "409", description = "Employee already has a manager role")
    })
    @RequestBody(
            required = true,
            description = "Promotion request payload",
            content = @Content(
                    schema = @Schema(implementation = PromoteToManagerRequest.class),
                    examples = @ExampleObject(
                            name = "Promote to Manager Example",
                            value = """
                            {
                              "email": "employee@example.com",
                              "department": "ENGINEERING"
                            }
                            """
                    )
            )
    )
    public @interface PromoteToManagerDoc {}

    //  🔹 Demote Manager
    @Operation(
            summary = "Demote a manager to employee",
            description = "Allows Admin or SuperAdmin to demote a manager back to employee role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manager successfully demoted",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Manager not found")
    })
    @RequestBody(
            required = true,
            description = "Demotion request payload",
            content = @Content(
                    schema = @Schema(implementation = DemoteManagerRequest.class),
                    examples = @ExampleObject(
                            name = "Demote Manager Example",
                            value = """
                            {
                              "email": "manager@example.com"
                            }
                            """
                    )
            )
    )
    public @interface DemoteManagerDoc {}

    //  🔹 Get Employees in Manager’s Department
    @Operation(
            summary = "Get all employees in manager’s department",
            description = "Retrieves all employees belonging to the department of the logged-in Manager/Admin/SuperAdmin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of employees retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "No employees found in department")
    })
    public @interface GetEmployeesInDepartmentDoc {}
}
