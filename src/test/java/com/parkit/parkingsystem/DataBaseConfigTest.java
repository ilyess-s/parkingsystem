package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.DriverManager;
import org.mockito.MockedStatic;                // Resolves 'MockedStatic'
import static org.mockito.Mockito.mockStatic;    // Resolves 'mockStatic'
import static org.mockito.ArgumentMatchers.anyString; // Used for DriverManager.getConnection
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataBaseConfigTest {

    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    public void setUp() {
        dataBaseConfig = new DataBaseConfig();
    }

    @Test
    public void closeConnectionTest() throws SQLException {
        // WHEN
        dataBaseConfig.closeConnection(connection);

        // THEN: Verify the close method was called on the mock
        verify(connection, times(1)).close();
    }

    @Test
    public void closePreparedStatementTest() throws SQLException {
        // WHEN
        dataBaseConfig.closePreparedStatement(preparedStatement);

        // THEN
        verify(preparedStatement, times(1)).close();
    }

    @Test
    public void closeResultSetTest() throws SQLException {
        // WHEN
        dataBaseConfig.closeResultSet(resultSet);

        // THEN
        verify(resultSet, times(1)).close();
    }

    @Test
    public void closeConnection_WithException_ShouldNotThrow() throws SQLException {
        // GIVEN: Connection throws error on close
        doThrow(new SQLException("Closing error")).when(connection).close();

        // WHEN & THEN: Should log the error internally but not crash the test
        dataBaseConfig.closeConnection(connection);
        verify(connection, times(1)).close();
    }

    @Test
    public void closeNullObjects_ShouldNotThrow() {
        // GIVEN: Passing null to the methods
        // WHEN & THEN: Methods should handle nulls gracefully (no NullPointerException)
        dataBaseConfig.closeConnection(null);
        dataBaseConfig.closePreparedStatement(null);
        dataBaseConfig.closeResultSet(null);
    }
    @Test
    public void getConnectionTest() throws Exception {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {

            // Use the variable name 'mockedDriverManager' to call .when()
            mockedDriverManager.when(() -> DriverManager.getConnection(
                    anyString(),
                    anyString(),
                    anyString()
            )).thenReturn(connection);

            // WHEN
            Connection result = dataBaseConfig.getConnection();

            // THEN
            assertNotNull(result);
            assertEquals(connection, result);
        }
    }
}