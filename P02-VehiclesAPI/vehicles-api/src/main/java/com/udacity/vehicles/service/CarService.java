package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final WebClient maps;
    private final WebClient pricing;
    private final ModelMapper mapper;

    public CarService(CarRepository repository, WebClient maps, WebClient pricing, ModelMapper mapper) {
        this.repository = repository;
        this.maps = maps;
        this.pricing = pricing;
        this.mapper = mapper;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) throws CarNotFoundException {

        Optional<Car> carOptional = repository.findById(id);
        if (carOptional.isEmpty()) {
            throw new CarNotFoundException();
        }
        Car car = carOptional.get();

        Price price = pricing
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("services/price/")
                        .queryParam("vehicleId", car.getId())
                        .build()
                )
                .retrieve().bodyToMono(Price.class).block();

        car.setPrice(String.format("%s %s", price.getCurrency(), price.getPrice()));

        Address address = maps
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps/")
                        .queryParam("lat", car.getLocation().getLat())
                        .queryParam("lon", car.getLocation().getLon())
                        .build()
                )
                .retrieve().bodyToMono(Address.class).block();
        mapper.map(Objects.requireNonNull(address), car.getLocation());

        car.getLocation().setAddress(address.getAddress());
        car.getLocation().setCity(address.getCity());
        car.getLocation().setAddress(address.getAddress());
        car.getLocation().setZip(address.getZip());
        car.getLocation().setState(address.getState());

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) throws CarNotFoundException {
        Optional<Car> carOptional = repository.findById(id);
        if (carOptional.isEmpty()) {
            throw new CarNotFoundException();
        } else {
            repository.deleteById(id);
        }
    }
}
