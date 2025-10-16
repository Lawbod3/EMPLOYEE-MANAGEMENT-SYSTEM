package com.darum.auth.documentations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.darum.auth.dto.request.AuthRequest;
import com.darum.auth.dto.request.RegisterRequest;
import com.darum.auth.dto.response.AuthResponse;
import com.darum.shared.dto.response.UserResponse;
import com.darum.shared.dto.request.AddRoleRequest;

public class AuthApiDocs {

    //  REGISTER
    @Operation(
            summary = "Register a new user",
            description = "Registers a new user with email, password, first name, and last name, then logs them in."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "User already exists")
    })
    @RequestBody(
            required = true,
            description = "User registration data",
            content = @Content(
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "Example registration",
                            value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "SecurePass123",
                          "firstName": "John",
                          "lastName": "Doe"
                        }
                        """
                    )
            )
    )
    public @interface RegisterDoc {}

    //  LOGIN
    @Operation(
            summary = "Login a user",
            description = "Authenticates a user and returns a JWT token for subsequent requests."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @RequestBody(
            required = true,
            description = "User login credentials",
            content = @Content(
                    schema = @Schema(implementation = AuthRequest.class),
                    examples = @ExampleObject(
                            name = "Example login",
                            value = """
                        {
                          "email": "john.doe@example.com",
                          "password": "SecurePass123"
                        }
                        """
                    )
            )
    )
    public @interface LoginDoc {}

    //  GET PROFILE
    @Operation(
            summary = "Get authenticated user profile",
            description = "Returns the currently logged-in user's profile details.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public @interface GetProfileDoc {}

    //  GET USER BY EMAIL
    @Operation(
            summary = "Get user by email",
            description = "Retrieves a user’s profile using their email address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public @interface GetUserByEmailDoc {}

    //  ADD ROLE
    @Operation(
            summary = "Add a role to a user",
            description = "SuperAdmin only — adds a new role to a user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role added successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (SuperAdmin only)")
    })
    @RequestBody(
            required = true,
            description = "Role to add",
            content = @Content(
                    schema = @Schema(implementation = AddRoleRequest.class),
                    examples = @ExampleObject(
                            name = "Add role example",
                            value = """
                        {
                          "role": "ADMIN"
                        }
                        """
                    )
            )
    )
    public @interface AddRoleDoc {}

    //  REMOVE ROLE
    @Operation(
            summary = "Remove a role from a user",
            description = "SuperAdmin only — removes an existing role from a user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role removed successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (SuperAdmin only)")
    })
    @RequestBody(
            required = true,
            description = "Role to remove",
            content = @Content(
                    schema = @Schema(implementation = AddRoleRequest.class),
                    examples = @ExampleObject(
                            name = "Remove role example",
                            value = """
                        {
                          "role": "DISPATCHER"
                        }
                        """
                    )
            )
    )
    public @interface RemoveRoleDoc {}
}
