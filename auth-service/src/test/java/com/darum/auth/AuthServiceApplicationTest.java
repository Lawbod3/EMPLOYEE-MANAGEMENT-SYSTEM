package com.darum.auth;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
 class AuthServiceApplicationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws Exception {
        // Try to get a connection
        try (Connection connection = dataSource.getConnection()) {
            // If we get here, connection is successful
            assertNotNull(connection);

            // Test if connection is valid
            assertTrue(connection.isValid(2), "Database connection should be valid");

            // Try to execute a simple query
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            }

            System.out.println("✅ Database connection test PASSED!");
        }
    }
}