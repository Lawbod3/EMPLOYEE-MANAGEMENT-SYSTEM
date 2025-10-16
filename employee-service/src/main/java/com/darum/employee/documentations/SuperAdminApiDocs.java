package com.darum.employee.documentations;
import com.darum.employee.dto.request.PromoteToAdminRequest;
import com.darum.employee.dto.request.RemoveAdminRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * 📘 Swagger documentation annotations for Super Admin operations
 * Used in SuperAdminController.
 */

public class SuperAdminApiDocs {

    // 🔹 Promote Employee to Admin
    @Operation(
            summary = "Promote an employee to admin",
            description = "Allows SuperAdmin to promote an existing employee to Admin role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee successfully promoted to admin",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or employee already has admin role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Access denied - only SuperAdmin can perform this action"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(
            required = true,
            description = "Promotion request payload for SuperAdmin to promote a user to Admin role",
            content = @Content(
                    schema = @Schema(implementation = PromoteToAdminRequest.class),
                    examples = @ExampleObject(
                            name = "Promote to Admin Example",
                            value = """
                            {
                              "email": "user@example.com",
                              "department": "IT"
                            }
                            """
                    )
            )
    )
    public @interface PromoteToAdminDoc {}



    // 🔹 Remove Admin Role from User
    @Operation(
            summary = "Remove admin role from a user",
            description = "Allows SuperAdmin to remove the ADMIN role from an existing user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin role successfully removed from user",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email or user does not have ADMIN role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid token"),
            @ApiResponse(responseCode = "403", description = "Access denied - only SuperAdmin allowed"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @RequestBody(
            required = true,
            description = "Remove admin role request payload",
            content = @Content(
                    schema = @Schema(implementation = RemoveAdminRequest.class),
                    examples = @ExampleObject(
                            name = "Remove Admin Role Example",
                            value = """
                            {
                              "email": "admin@example.com"
                            }
                            """
                    )
            )
    )
    public @interface RemoveAdminRoleDoc {}
}
