package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ParkingSpotDAOTest {

    @Mock
    private DataBaseConfig dataBaseConfig;  // Mock the DataBaseConfig
    @Mock
    private Connection connection;          // Mock the SQL connection
    @Mock
    private PreparedStatement preparedStatement; // Mock the prepared statement
    @Mock
    private ResultSet resultSet;           // Mock the ResultSet

    private ParkingSpotDAO parkingSpotDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);

        // Create an instance of ParkingSpotDAO with the mocks
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseConfig; // Manually inject the mock dataBaseConfig
    }

    // --- Tests for updateParking(ParkingSpot) ---

    @Test
    public void updateParkingTest_success() {
        try {
            // GIVEN
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1); // Simulate success (1 row updated)

            // WHEN
            boolean result = parkingSpotDAO.updateParking(parkingSpot);

            // THEN
            assertTrue(result, "updateParking should return true when one row is updated");
            verify(preparedStatement, times(1)).executeUpdate(); // Verify executeUpdate() was called once
        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void updateParkingTest_failure() {
        try {
            // GIVEN
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(0); // Simulate failure (0 rows updated)

            // WHEN
            boolean result = parkingSpotDAO.updateParking(parkingSpot);

            // THEN
            assertFalse(result, "updateParking should return false when no row is updated");
            verify(preparedStatement, times(1)).executeUpdate(); // Verify executeUpdate() was called once
        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void updateParkingTest_exception() {
        try {
            // GIVEN
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.UPDATE_PARKING_SPOT)).thenThrow(new SQLException("Database error"));

            // WHEN
            boolean result = parkingSpotDAO.updateParking(parkingSpot);

            // THEN
            assertFalse(result, "updateParking should return false when an exception occurs");
        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    // --- Tests for getNextAvailableSlot(ParkingType) ---

    @Test
    public void getNextAvailableSlotTest_success() throws SQLException, ClassNotFoundException {
        // GIVEN
        ParkingType parkingType = ParkingType.CAR;
        int expectedSlotId = 1;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedSlotId); // Simulate that the ID of the available parking spot is 1

        // WHEN
        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // THEN
        assertEquals(expectedSlotId, result, "The next available parking slot ID should be 1.");
        verify(preparedStatement, times(1)).executeQuery();  // Verify that executeQuery() was called once
    }

    @Test
    public void getNextAvailableSlotTest_noAvailableSpot() throws SQLException, ClassNotFoundException {
        // GIVEN
        ParkingType parkingType = ParkingType.CAR;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);  // Simulate no available spot

        // WHEN
        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // THEN
        assertEquals(-1, result, "The method should return -1 when no available parking spot is found.");
        verify(preparedStatement, times(1)).executeQuery();  // Verify that executeQuery() was called once
    }

    @Test
    public void getNextAvailableSlotTest_databaseError() throws SQLException, ClassNotFoundException {
        // GIVEN
        ParkingType parkingType = ParkingType.CAR;

        when(dataBaseConfig.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenThrow(new SQLException("Database error"));  // Simulate SQL exception

        // WHEN
        int result = parkingSpotDAO.getNextAvailableSlot(parkingType);

        // THEN
        assertEquals(-1, result, "The method should return -1 if a database error occurs.");
    }
}
