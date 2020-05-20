package com.space.controller;

import com.space.exception.BadRequestException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/rest/ships")
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public List<Ship> getShipsList(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String planet,
                                  @RequestParam(required = false) ShipType shipType,
                                  @RequestParam(required = false) Long after,
                                  @RequestParam(required = false) Long before,
                                  @RequestParam(required = false) Boolean isUsed,
                                  @RequestParam(required = false) Double minSpeed,
                                  @RequestParam(required = false) Double maxSpeed,
                                  @RequestParam(required = false) Integer minCrewSize,
                                  @RequestParam(required = false) Integer maxCrewSize,
                                  @RequestParam(required = false) Double minRating,
                                  @RequestParam(required = false) Double maxRating,
                                  @RequestParam(required = false) ShipOrder order,
                                  @RequestParam(required = false) Integer pageNumber,
                                  @RequestParam(required = false) Integer pageSize){
        List<Ship> shipsList = shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
        return shipService.pageShips(shipsList, order, pageNumber, pageSize);

    }

    @GetMapping(value = "/count")
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Integer getShipsCount(@RequestParam(required = false) String name,
                                 @RequestParam(required = false) String planet,
                                 @RequestParam(required = false) ShipType shipType,
                                 @RequestParam(required = false) Long after,
                                 @RequestParam(required = false) Long before,
                                 @RequestParam(required = false) Boolean isUsed,
                                 @RequestParam(required = false) Double minSpeed,
                                 @RequestParam(required = false) Double maxSpeed,
                                 @RequestParam(required = false) Integer minCrewSize,
                                 @RequestParam(required = false) Integer maxCrewSize,
                                 @RequestParam(required = false) Double minRating,
                                 @RequestParam(required = false) Double maxRating){
        return shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating).size();
    }

    @GetMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<Ship> getShip(@PathVariable Long id){
        if (!isIdValid(id)) throw new BadRequestException();
        return new ResponseEntity<>(shipService.getShip(id), HttpStatus.OK);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        Ship createShip = shipService.createShip(ship);
        if (createShip == null) throw new BadRequestException();
        return new ResponseEntity<>(createShip, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<Ship> updateShip(@RequestBody Ship ship, @PathVariable Long id) {
        if (!isIdValid(id)) throw new BadRequestException();
        return new ResponseEntity<>(shipService.updateShip(ship, id), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteShip(@PathVariable Long id){
        if (!isIdValid(id)) throw new BadRequestException();
        shipService.deleteShip(id);
    }

    private boolean isIdValid(Long id){
        return id!=null && id>0;
    }
}
