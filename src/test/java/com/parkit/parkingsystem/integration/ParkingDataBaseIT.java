package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

class ParkingDatabaseIT {

    private ParkingService parkingService;
    private DataBaseConfig dataBaseConfig = new DataBaseConfig();

    private static TicketDAO ticketDAO;
    private static  ParkingSpotDAO parkingSpotDAO;

    @Mock
    private InputReaderUtil inputReaderUtil;

    @BeforeAll
    static void setupClass() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        ticketDAO = new TicketDAO();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        clearDBEntries();
    }

    @Test
    void testParkingACar() throws Exception {
        parkingService = new ParkingService(
                inputReaderUtil,
                parkingSpotDAO,
                ticketDAO);

        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // WHEN
        parkingService.processIncomingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        assertEquals(1, ticketDAO.getNbTicket("ABCDEF"));
    }

    @Test
    void testParkingLotExit() throws Exception {
        parkingService = new ParkingService(
                inputReaderUtil,
                parkingSpotDAO,
                ticketDAO);

        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        // WHEN
        parkingService.processExitingVehicle();

        //THEN
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        assertEquals(0, ticketDAO.getNbTicket("ABCDEF"));

    }

    @Test
    void isReccurentUser() throws Exception {


    }

    private void clearDBEntries() throws Exception {
        try (Connection con = dataBaseConfig.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "TRUNCATE TABLE ticket");
            ps.execute();
        }
    }
}
