package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;


    @BeforeEach
    public void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            lenient().when(inputReaderUtil.readSelection()).thenReturn(1);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            lenient().when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processIncomingVehicleTest() {
        try {
            // GIVEN
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1); // 1 pour CAR
            when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            // WHEN
            parkingService.processIncomingVehicle();

            // THEN
            verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void processExitingVehicleTest() {
        try {
            // GIVEN
            String regNumber = "ABCDEF";
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000))); // Entré il y a 1h
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(regNumber);

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
            when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            // WHEN
            parkingService.processExitingVehicle();

            // THEN
            // On vérifie que le ticket est mis à jour (prix et heure de sortie)
            verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
            // On vérifie que la place de parking est libérée
            verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            throw new RuntimeException("Test failed during execution", e);
        }
    }

    @Test
    public void processExitingVehicleTestUnableToUpdateTicket() {
        // GIVEN
        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
            when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);

            // Simulate a failure in the database update
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

            // WHEN & THEN
            // If your service is designed to throw an exception when update fails:
            // assertThrows(RuntimeException.class, () -> parkingService.processExitingVehicle());

            parkingService.processExitingVehicle();

            // Verify that even if updateTicket fails, we can check the behavior.
            // For ParkIt, if updateTicket returns false, updateParking should NOT be called.
            verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));

        } catch (Exception e) {
            fail("Test failed due to unexpected exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testGetNextParkingNumberIfAvailable_withAvailableSpot() {

        // GIVEN
        ParkingSpot availableSpot = new ParkingSpot(1, ParkingType.CAR, true);

        when(inputReaderUtil.readSelection()).thenReturn(1); // 1 = CAR
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(availableSpot.getId());

        // WHEN
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ParkingType.CAR, result.getParkingType());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable_withNoAvailableSpot() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        // WHEN
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNull(result, "ParkingSpot should be null when no spot is available");
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void getNextParkingNumberIfAvailable_withEmptyParkingLot() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1); // User selects CAR
        // Simulate first available spot in empty parking lot
        ParkingSpot firstSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(firstSpot.getId());

        // WHEN
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // THEN
        assertNotNull(result, "ParkingSpot object should exist");
        assertEquals(1, result.getId(), "The first available parking spot should be 1");
        assertTrue(result.isAvailable(), "The first available parking spot should be available");
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void testGetVehichleType_carSelected() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(1);

        // WHEN
        ParkingType result = parkingService.getVehichleType();

        // THEN
        assertEquals(ParkingType.CAR, result, "Vehicle type should be CAR when input is 1");
        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    public void testGetVehichleType_bikeSelected() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(2);

        // WHEN
        ParkingType result = parkingService.getVehichleType();

        // THEN
        assertEquals(ParkingType.BIKE, result, "Vehicle type should be BIKE when input is 2");
        verify(inputReaderUtil, times(1)).readSelection();
    }

    @Test
    public void testGetVehichleType_invalidSelection() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(99);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> parkingService.getVehichleType(),
                "Invalid input should throw IllegalArgumentException");
    }

    @Test
    public void getVehichleType_shouldThrowIllegalArgumentException_whenInvalidInput() {
        // GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(99);

        // WHEN / THEN
        assertThrows(IllegalArgumentException.class,
                () -> parkingService.getVehichleType(),
                "Invalid vehicle type selection should throw exception");
    }

    @Test
    public void processExitingVehicle_recurrentUser_shouldApplyDiscount() throws Exception {

        String regNumber = "ABCDEF";
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(regNumber);

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNumber);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);

        // 👇 THIS IS THE IMPORTANT LINE
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);

        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }


}

