package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }


        /*Calendar inCalendar = Calendar.getInstance();
        inCalendar.setTime(ticket.getInTime());

        Calendar outCalendar = Calendar.getInstance();
        outCalendar.setTime(ticket.getOutTime());


        int inMinutes = inCalendar.get(Calendar.HOUR_OF_DAY) * 60 + inCalendar.get(Calendar.MINUTE);
        int outMinutes = outCalendar.get(Calendar.HOUR_OF_DAY) * 60 + outCalendar.get(Calendar.MINUTE);*/

        Date inTime = ticket.getInTime();
        Date outTime = ticket.getOutTime();

        Instant inInstant = inTime.toInstant();
        Instant outInstant = outTime.toInstant();

        Duration duration = Duration.between(inInstant, outInstant);
        long durationInMinutes = duration.toMinutes();
        double durationInHours = duration.toMinutes() / 60.0;
        double price = 0.0;
        /*
        //long durationInHours = duration.toHours();
        //Duration in mins
        //int durationInMinutes = outMinutes - inMinutes;
        //System.out.println("Duration in minutes: " + durationInMinutes);
        //Duration in hours
        */
        if (durationInMinutes <= 30) {
            ticket.setPrice(0.0); // First 30 minutes are free
            System.out.println("Fare is 0.0 (first 30 minutes free)");
            return;
        }
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                //ticket.setPrice(durationInHours * Fare.CAR_RATE_PER_HOUR);
                price = durationInHours * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                //ticket.setPrice(durationInHours * Fare.BIKE_RATE_PER_HOUR);
                price = durationInHours * Fare.BIKE_RATE_PER_HOUR;

                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
        System.out.println("Calculated fare: " + ticket.getPrice());

        if (ticket.isRecurrentUser()) {
            price = price * (1 - Fare.RECURRENT_USER_DISCOUNT);
        }
        ticket.setPrice(price);
        System.out.println("Calculated fare: " + ticket.getPrice());
    }
}
