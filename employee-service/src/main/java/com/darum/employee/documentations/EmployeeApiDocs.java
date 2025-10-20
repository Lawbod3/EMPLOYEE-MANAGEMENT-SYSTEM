package com.darum.employee.documentations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.darum.employee.dto.request.GetEmployeeRequest;


/**
 * 📘 Swagger documentation annotations for Employee-related APIs (WebFlux)
 */

public class EmployeeApiDocs {

    //🔹 GET EMPLOYEE BY CODE (Admin, SuperAdmin, Manager)
    @Operation(
            summary = "Get employee by employee code",
            description = """
                    Fetch a specific employee using their employee code.
                    <br>Access restricted to **Admin**, **SuperAdmin**, or **Manager** roles.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing employee code"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient privileges"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @RequestBody(
            required = true,
            description = "Request body containing the employee code to look up",
            content = @Content(
                    schema = @Schema(implementation = GetEmployeeRequest.class),
                    examples = @ExampleObject(
                            name = "Get Employee Example",
                            value = """
                            {
                              "employeeCode": "EMP-1023"
                            }
                            """
                    )
            )
    )
    public @interface GetSpecificEmployeeDoc {}



    // 🔹 GET MY DETAILS (Any authenticated employee)
    @Operation(
            summary = "Get logged-in employee details",
            description = """
                    Retrieves the profile details of the currently authenticated employee.
                    <br>Requires a valid JWT token. Accessible to all employee roles.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Employee details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = com.darum.shared.dto.response.ApiResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "404", description = "Employee record not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public @interface GetMyDetailsDoc {}


}
