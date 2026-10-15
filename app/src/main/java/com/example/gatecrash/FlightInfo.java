package com.example.gatecrash;

import com.google.android.gms.maps.model.LatLng;

public class FlightInfo {
    private String flightNumber;
    private String airlineName;
    private String airlineCode;
    private String originAirport;
    private String destinationAirport;
    private String route;
    private String departureTime;
    private String arrivalTime;
    private String terminal;
    private String gate;
    private String baggage;
    private String aircraft;
    private String status;
    private String date;
    private String placesVerifiedName;
    private String placesVerifiedAddress;
    private LatLng airportCoordinates;
    private boolean isPlacesVerified;
    private boolean isLiveTracking;
    private double liveAltitude;
    private double liveSpeed;
    private LatLng liveCoordinates;

    public FlightInfo(String flightNumber, String airlineName, String airlineCode,
                      String originAirport, String destinationAirport, String route,
                      String departureTime, String arrivalTime, String terminal,
                      String gate, String aircraft, String status, LatLng airportCoordinates) {
        this.flightNumber = flightNumber;
        this.airlineName = airlineName;
        this.airlineCode = airlineCode;
        this.originAirport = originAirport;
        this.destinationAirport = destinationAirport;
        this.route = route;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.terminal = terminal;
        this.gate = gate;
        this.baggage = "Carousel 2";
        this.aircraft = aircraft;
        this.status = status;
        this.airportCoordinates = airportCoordinates;
        this.date = "Today";
        this.placesVerifiedName = destinationAirport;
        this.placesVerifiedAddress = destinationAirport;
        this.isPlacesVerified = false;
        this.isLiveTracking = false;
        this.liveAltitude = 0.0;
        this.liveSpeed = 0.0;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getAirlineName() { return airlineName; }
    public String getAirlineCode() { return airlineCode; }
    public String getOriginAirport() { return originAirport; }
    public String getDestinationAirport() { return destinationAirport; }
    public String getRoute() { return route; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getTerminal() { return terminal; }
    public String getGate() { return gate; }
    public String getBaggage() { return baggage; }
    public void setBaggage(String baggage) { this.baggage = baggage; }
    public String getAircraft() { return aircraft; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPlacesVerifiedName() { return placesVerifiedName; }
    public void setPlacesVerifiedName(String placesVerifiedName) { this.placesVerifiedName = placesVerifiedName; }

    public String getPlacesVerifiedAddress() { return placesVerifiedAddress; }
    public void setPlacesVerifiedAddress(String placesVerifiedAddress) { this.placesVerifiedAddress = placesVerifiedAddress; }

    public LatLng getAirportCoordinates() { return airportCoordinates; }
    public void setAirportCoordinates(LatLng airportCoordinates) { this.airportCoordinates = airportCoordinates; }

    public boolean isPlacesVerified() { return isPlacesVerified; }
    public void setPlacesVerified(boolean placesVerified) { isPlacesVerified = placesVerified; }

    public boolean isLiveTracking() { return isLiveTracking; }
    public void setLiveTracking(boolean liveTracking) { isLiveTracking = liveTracking; }

    public double getLiveAltitude() { return liveAltitude; }
    public void setLiveAltitude(double liveAltitude) { this.liveAltitude = liveAltitude; }

    public double getLiveSpeed() { return liveSpeed; }
    public void setLiveSpeed(double liveSpeed) { this.liveSpeed = liveSpeed; }

    public LatLng getLiveCoordinates() { return liveCoordinates; }
    public void setLiveCoordinates(LatLng liveCoordinates) { this.liveCoordinates = liveCoordinates; }

    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setTerminal(String terminal) { this.terminal = terminal; }
    public void setGate(String gate) { this.gate = gate; }
    public void setDestinationAirport(String destinationAirport) { this.destinationAirport = destinationAirport; }
    public void setOriginAirport(String originAirport) { this.originAirport = originAirport; }
    public void setRoute(String route) { this.route = route; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }
    public void setAircraft(String aircraft) { this.aircraft = aircraft; }
}
