package com.woltaxi.ride.controller;

import com.woltaxi.ride.model.Ride;
import com.woltaxi.ride.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rides")
public class RideController {

    @Autowired
    private RideService rideService;

    @PostMapping("/request")
    public ResponseEntity<Ride> requestRide(@RequestBody RideRequest request) {
        Ride ride = rideService.createRide(
            request.getPassengerId(),
            request.getPickupLatitude(),
            request.getPickupLongitude(),
            request.getDestinationLatitude(),
            request.getDestinationLongitude()
        );
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<Ride> acceptRide(@PathVariable Long rideId, @RequestBody AcceptRequest request) {
        Ride ride = rideService.acceptRide(rideId, request.getDriverId());
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(@PathVariable Long rideId) {
        Ride ride = rideService.completeRide(rideId);
        return ResponseEntity.ok(ride);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Ride>> getPassengerRides(@PathVariable Long passengerId) {
        List<Ride> rides = rideService.getPassengerRides(passengerId);
        return ResponseEntity.ok(rides);
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Ride>> getDriverRides(@PathVariable Long driverId) {
        List<Ride> rides = rideService.getDriverRides(driverId);
        return ResponseEntity.ok(rides);
    }
}

class RideRequest {
    private Long passengerId;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;
    
    // Getters and setters
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }
    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }
    public Double getDestinationLatitude() { return destinationLatitude; }
    public void setDestinationLatitude(Double destinationLatitude) { this.destinationLatitude = destinationLatitude; }
    public Double getDestinationLongitude() { return destinationLongitude; }
    public void setDestinationLongitude(Double destinationLongitude) { this.destinationLongitude = destinationLongitude; }
}

class AcceptRequest {
    private Long driverId;
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
}
