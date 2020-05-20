package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipService {

    private final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Ship createShip(Ship ship) {
        if (ship.getName() == null ||
                ship.getName().isEmpty() ||
                ship.getName().length() > 50 ||
                ship.getPlanet() == null ||
                ship.getPlanet().isEmpty() ||
                ship.getPlanet().length() > 50 ||
                ship.getShipType() == null ||
                ship.getProdDate() == null ||
                getYear(ship.getProdDate()) < 2800 ||
                getYear(ship.getProdDate()) > 3019 ||
                ship.getSpeed() == null ||
                ship.getSpeed() < 0.01d ||
                ship.getSpeed() > 0.99d ||
                ship.getCrewSize() == null ||
                ship.getCrewSize() < 1 ||
                ship.getCrewSize() > 9999) throw new BadRequestException();
        if (ship.isUsed() == null) ship.setUsed(false);
        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        ship.setRating(createRating(ship));
        return shipRepository.save(ship);
    }

    public List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after, Long before,
                                   Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                   Integer maxCrewSize, Double minRating, Double maxRating) {
        List<Ship> shipsList = shipRepository.findAll();
        if (name!=null) shipsList = shipsList.stream().filter(ship -> ship.getName().contains(name)).collect(Collectors.toList());
        if (planet!=null) shipsList = shipsList.stream().filter(ship -> ship.getPlanet().contains(planet)).collect(Collectors.toList());
        if (shipType!=null) shipsList = shipsList.stream().filter(ship -> ship.getShipType()==shipType).collect(Collectors.toList());
        if (after!=null) shipsList = shipsList.stream().filter(ship -> ship.getProdDate().getTime()>=after).collect(Collectors.toList());
        if (before!=null) shipsList = shipsList.stream().filter(ship -> ship.getProdDate().getTime()<=before).collect(Collectors.toList());
        if (isUsed!=null) shipsList = shipsList.stream().filter(ship -> ship.isUsed()==isUsed).collect(Collectors.toList());
        if (minSpeed!=null) shipsList = shipsList.stream().filter(ship -> ship.getSpeed()>=minSpeed).collect(Collectors.toList());
        if (maxSpeed!=null) shipsList = shipsList.stream().filter(ship -> ship.getSpeed()<=maxSpeed).collect(Collectors.toList());
        if (minCrewSize!=null) shipsList = shipsList.stream().filter(ship -> ship.getCrewSize()>=minCrewSize).collect(Collectors.toList());
        if (maxCrewSize!=null) shipsList = shipsList.stream().filter(ship -> ship.getCrewSize()<=maxCrewSize).collect(Collectors.toList());
        if (minRating!=null) shipsList = shipsList.stream().filter(ship -> ship.getRating()>=minRating).collect(Collectors.toList());
        if (maxRating!=null) shipsList = shipsList.stream().filter(ship -> ship.getRating()<=maxRating).collect(Collectors.toList());
        return shipsList;
    }

    public List<Ship> pageShips(List<Ship> shipsList, ShipOrder order,
                                Integer pageNumber, Integer pageSize){
        if (order==null) order = ShipOrder.ID;
        shipsList.sort(getComparator(order));
        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;
        return shipsList.stream()
                .sorted(getComparator(order))
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    @Transactional
    public Ship updateShip(Ship newShip, Long id) {
        Ship shipUpdate = getShip(id);
        if (newShip == null || shipUpdate == null) throw new BadRequestException();
        if (newShip.getName() != null) {
            if (newShip.getName().length() > 50 || newShip.getName().isEmpty()) throw new BadRequestException();
            shipUpdate.setName(newShip.getName());
        }
        if (newShip.getPlanet() != null) {
            if (newShip.getPlanet().length() > 50 || newShip.getPlanet().isEmpty()) throw new BadRequestException();
            shipUpdate.setPlanet(newShip.getPlanet());
        }
        if (newShip.getProdDate() != null) {
            if (getYear(newShip.getProdDate()) < 2800 || getYear(newShip.getProdDate()) > 3019) throw new BadRequestException();
            shipUpdate.setProdDate(newShip.getProdDate());
        }
        if (newShip.getSpeed() != null) {
            if (newShip.getSpeed() < 0.01d || newShip.getSpeed() > 0.99d) throw new BadRequestException();
            shipUpdate.setSpeed(newShip.getSpeed());
        }
        if (newShip.getCrewSize() != null) {
            if (newShip.getCrewSize() < 1 || newShip.getCrewSize() > 9999) throw new BadRequestException();
            shipUpdate.setCrewSize(newShip.getCrewSize());
        }
        if (newShip.getShipType() != null) shipUpdate.setShipType(newShip.getShipType());
        if (newShip.isUsed() != null) shipUpdate.setUsed(newShip.isUsed());
        shipUpdate.setRating(createRating(shipUpdate));
        return shipRepository.save(shipUpdate);
    }

    public void deleteShip(Long id){
        if (!shipRepository.existsById(id)) throw new NotFoundException();
        shipRepository.deleteById(id);
    }

    public Ship getShip(Long id) {
        if (!shipRepository.existsById(id)) throw new NotFoundException();
        return shipRepository.findById(id).orElse(null);
    }

    private double createRating(Ship ship){
        double v = ship.getSpeed();
        double k = ship.isUsed() ? 0.5d : 1.0d;
        double rating = (80*v*k) / (double) (3020-getYear(ship.getProdDate()));
        return (double) Math.round(rating*100)/100;
    }

    private Comparator<Ship> getComparator(ShipOrder order){
        switch (order){
            case ID:
                return Comparator.comparing(Ship::getId);
            case SPEED:
                return Comparator.comparing(Ship::getSpeed);
            case DATE:
                return Comparator.comparing(Ship::getProdDate);
            case RATING:
                return Comparator.comparing(Ship::getRating);
            default:
                throw new IllegalStateException("Unexpected value: " + order);
        }
    }

    private int getYear(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
}