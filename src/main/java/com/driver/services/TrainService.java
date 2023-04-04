package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        List<Station> list = trainEntryDto.getStationRoute();
        StringBuilder route = new StringBuilder();
        for (Station station: list){
            if (route.length() == 0){
                route.append(station.toString());
            }
            else {
                route.append(", ").append(station.toString());
            }
        }

        train.setRoute(route.toString());

        trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        List<Ticket> tickets = train.getBookedTickets();
        String[] trainRoute = train.getRoute().split(",");
        HashMap<String, Integer> map = new HashMap<>();
        for (int i =0 ;i<trainRoute.length;i++){
            map.put(trainRoute[i],i);
        }
        if (!map.containsKey(seatAvailabilityEntryDto.getFromStation().toString())
                || !map.containsKey(seatAvailabilityEntryDto.getToStation().toString())){
            return 0;
        }

        int noOfPassengers = 0;
        for (Ticket ticket: tickets){
            noOfPassengers+=ticket.getPassengersList().size();
        }

        int availableSeats = train.getNoOfSeats()-noOfPassengers;
        for (Ticket ticket: tickets){
            String from = ticket.getFromStation().toString();
            String to = ticket.getToStation().toString();
            if (map.get(seatAvailabilityEntryDto.getToStation().toString()) <= map.get(from)){
                availableSeats+=ticket.getPassengersList().size();
            }
            else if(map.get(seatAvailabilityEntryDto.getFromStation().toString()) >= map.get(to)) {
                availableSeats+=ticket.getPassengersList().size();
            }
        }

       return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        String route = train.getRoute();
        String[] routeA = route.split(",");

        for (String routes: routeA){
            if (!station.toString().equalsIgnoreCase(routes)){
                throw new Exception("Train is not passing from this station");
            }
        }
        List<Ticket> tickets = train.getBookedTickets();
        int noOfPeopleBoardingAtAStation = 0;
        for (Ticket ticket : tickets){
            if (ticket.getFromStation().equals(station)){
                noOfPeopleBoardingAtAStation += ticket.getPassengersList().size();
            }
        }

        return noOfPeopleBoardingAtAStation;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train=trainRepository.findById(trainId).get();
        List<Ticket> tickets = train.getBookedTickets();
        if(tickets.size()==0){
            return 0;
        }
        List<Passenger> passengerList;
        int oldestPerson = 0;
        for(Ticket ticket:tickets){
            passengerList = ticket.getPassengersList();
            for(Passenger passenger:passengerList){
                if(passenger.getAge()>oldestPerson){
                    oldestPerson=passenger.getAge();
                }
            }
        }
        return oldestPerson;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trainList = trainRepository.findAll();
        List<Integer> trainsBetweenGivenTime = new ArrayList<>();
        for(Train train:trainList){
            String[] trainRoute = train.getRoute().split(",");
            for(int i=0;i<trainRoute.length;i++){
                if(trainRoute[i].equals(station.toString())){
                    int time=train.getDepartureTime().getHour()+i;
                    if(time>=startTime.getHour()&&time<=endTime.getHour()){
                        trainsBetweenGivenTime.add(train.getTrainId());
                    }
                    break;
                }
            }
        }
        return trainsBetweenGivenTime;

    }
}
