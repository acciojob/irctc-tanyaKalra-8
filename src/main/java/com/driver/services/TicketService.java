package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        List<Ticket> tickets = train.getBookedTickets();
        String route = train.getRoute();
        String[] routeA = route.split(".");

        int count = 0;
        for (Ticket ticket: tickets){
            count+= ticket.getPassengersList().size();
        }

        int availableSeats  = train.getNoOfSeats()-count;
        if (availableSeats<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        boolean isStationPresentOnRoute = false;
        for (String station: routeA){
            if (bookTicketEntryDto.getFromStation().name().equalsIgnoreCase(station)){
                isStationPresentOnRoute = true;
                break;
            }
        }
        boolean isArivalPresentOnRoute = false;

        for (String station: routeA){
            if (bookTicketEntryDto.getToStation().name().equalsIgnoreCase(station)){
                isArivalPresentOnRoute = true;
                break;
            }
        }

        if (!isArivalPresentOnRoute || !isStationPresentOnRoute){
            throw new Exception("Invalid stations");
        }

        int from = Arrays.asList(routeA).indexOf(bookTicketEntryDto.getFromStation().name());
        int to = Arrays.asList(routeA).indexOf(bookTicketEntryDto.getToStation().name());
        int distance = to-from;

        List<Passenger> passengerList = new ArrayList<>();
        for (int passengerId: bookTicketEntryDto.getPassengerIds()){
            Passenger passenger = passengerRepository.findById(passengerId).get();
            passengerList.add(passenger);
        }

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(300*distance);
        ticket.setTrain(train);
        ticket.setPassengersList(passengerList);

        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        passenger.getBookedTickets().add(ticket);
        passengerRepository.save(passenger);

        Ticket ticket1 = ticketRepository.save(ticket);
        train.getBookedTickets().add(ticket1);
        trainRepository.save(train);

       return ticket1.getTicketId();

    }
}
