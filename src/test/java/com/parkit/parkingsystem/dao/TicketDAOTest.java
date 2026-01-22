package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketDAOTest {

    @Mock
    private DataBaseConfig dataBaseConfig;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private TicketDAO ticketDAO;

    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.initMocks(this);
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseConfig;
    }

    // --- Tests for getTicket(String) ---

    @Test
    public void getTicketTest_success() {
        try {
            // GIVEN
            String vehicleRegNumber = "ABC123";

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);

            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1); // parking number
            when(resultSet.getInt(2)).thenReturn(10); // ticket ID
            when(resultSet.getDouble(3)).thenReturn(30.0);
            when(resultSet.getTimestamp(4)).thenReturn(Timestamp.valueOf("2023-01-01 10:00:00"));
            when(resultSet.getTimestamp(5)).thenReturn(null);
            when(resultSet.getString(6)).thenReturn(ParkingType.CAR.name());

            // WHEN
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

            // THEN
            assertNotNull(ticket, "Ticket should not be null");
            assertEquals(10, ticket.getId());
            assertEquals(vehicleRegNumber, ticket.getVehicleRegNumber());
            assertEquals(30.0, ticket.getPrice());
            assertEquals(ParkingType.CAR, ticket.getParkingSpot().getParkingType());
            assertEquals(1, ticket.getParkingSpot().getId());

            verify(preparedStatement, times(1)).executeQuery();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void getTicketTest_noTicketFound() {
        try {
            // GIVEN
            String vehicleRegNumber = "UNKNOWN";

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.GET_TICKET)).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            // WHEN
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

            // THEN
            assertNull(ticket, "Ticket should be null when no record is found");
            verify(preparedStatement, times(1)).executeQuery();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void getTicketTest_databaseError() {
        try {
            // GIVEN
            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.GET_TICKET))
                    .thenThrow(new SQLException("Database error"));

            // WHEN
            Ticket ticket = ticketDAO.getTicket("ABC123");

            // THEN
            assertNull(ticket, "Ticket should be null when a database error occurs");

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void getNbTicketTest_success() {
        try {
            // GIVEN
            String vehicleRegNumber = "ABC123";
            int expectedCount = 3;

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(
                    "SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?"
            )).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(expectedCount);

            // WHEN
            int result = ticketDAO.getNbTicket(vehicleRegNumber);

            // THEN
            assertEquals(expectedCount, result, "The number of tickets should match the database count");
            verify(preparedStatement, times(1)).executeQuery();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void getNbTicketTest_noTicketFound() {
        try {
            // GIVEN
            String vehicleRegNumber = "UNKNOWN";

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(
                    "SELECT COUNT(*) FROM ticket WHERE VEHICLE_REG_NUMBER = ?"
            )).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(false);

            // WHEN
            int result = ticketDAO.getNbTicket(vehicleRegNumber);

            // THEN
            assertEquals(0, result, "The method should return 0 when no tickets are found");
            verify(preparedStatement, times(1)).executeQuery();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void getNbTicketTest_databaseError() {
        try {
            // GIVEN
            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString()))
                    .thenThrow(new SQLException("Database error"));

            // WHEN
            int result = ticketDAO.getNbTicket("ABC123");

            // THEN
            assertEquals(0, result, "The method should return 0 when a database error occurs");

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void updateTicketTest_success() {
        try {
            // GIVEN
            Ticket ticket = new Ticket();
            ticket.setId(10);
            ticket.setPrice(15.0);
            ticket.setOutTime(new java.util.Date());

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.UPDATE_TICKET)).thenReturn(preparedStatement);

            // WHEN
            boolean result = ticketDAO.updateTicket(ticket);

            // THEN
            assertTrue(result, "updateTicket should return true when update succeeds");
            verify(preparedStatement, times(1)).execute();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void updateTicketTest_failure_exception() {
        try {
            // GIVEN
            Ticket ticket = new Ticket();
            ticket.setId(10);
            ticket.setPrice(15.0);
            ticket.setOutTime(new java.util.Date());

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.UPDATE_TICKET))
                    .thenThrow(new SQLException("Database error"));

            // WHEN
            boolean result = ticketDAO.updateTicket(ticket);

            // THEN
            assertFalse(result, "updateTicket should return false when an exception occurs");

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    // --- Tests for saveTicket(Ticket) ---

    @Test
    public void saveTicketTest_success_butReturnsFalse() {
        try {
            // GIVEN
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber("ABC123");
            ticket.setPrice(10.0);
            ticket.setInTime(new java.util.Date());
            ticket.setOutTime(null);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket.setParkingSpot(parkingSpot);

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.SAVE_TICKET)).thenReturn(preparedStatement);
            when(preparedStatement.execute()).thenReturn(true);

            // WHEN
            boolean result = ticketDAO.saveTicket(ticket);

            // THEN
            assertFalse(result,
                    "saveTicket should return false because the finally block overrides the return value");
            verify(preparedStatement, times(1)).execute();

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void saveTicketTest_exception() {
        try {
            // GIVEN
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber("ABC123");
            ticket.setPrice(10.0);
            ticket.setInTime(new java.util.Date());

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            ticket.setParkingSpot(parkingSpot);

            when(dataBaseConfig.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(DBConstants.SAVE_TICKET))
                    .thenThrow(new SQLException("Database error"));

            // WHEN
            boolean result = ticketDAO.saveTicket(ticket);

            // THEN
            assertFalse(result, "saveTicket should return false when an exception occurs");

        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }




}
