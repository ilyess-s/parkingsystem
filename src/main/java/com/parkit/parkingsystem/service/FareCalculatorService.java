package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {

        if ((ticket.getOutTime() == null)
                || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException(
                    "Out time provided is incorrect:" + ticket.getOutTime());
        }

        Date inTime = ticket.getInTime();
        Date outTime = ticket.getOutTime();

        Instant inInstant = inTime.toInstant();
        Instant outInstant = outTime.toInstant();

        Duration duration = Duration.between(inInstant, outInstant);
        long durationInMinutes = duration.toMinutes();
        double durationInHours = durationInMinutes / 60.0;

        if (durationInMinutes <= 30) {
            ticket.setPrice(0.0);
            return;
        }

        double price;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR:
                price = durationInHours * Fare.CAR_RATE_PER_HOUR;
                break;
            case BIKE:
                price = durationInHours * Fare.BIKE_RATE_PER_HOUR;
                break;
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
        if (discount) {
            price = price * 0.95;
        }

        ticket.setPrice(price);
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}
