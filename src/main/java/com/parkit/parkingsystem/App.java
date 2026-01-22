package com.parkit.parkingsystem;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.InteractiveShell;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
    private static final Logger logger = LogManager.getLogger("App");
    public static void main(String[] args) {
        InputReaderUtil inputReaderUtil = new InputReaderUtil();
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        TicketDAO ticketDAO = new TicketDAO();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //InteractiveShell shell = new InteractiveShell(inputReaderUtil, parkingService);
        InteractiveShell.loadInterface();  // <-- call on the instance
    }
}
