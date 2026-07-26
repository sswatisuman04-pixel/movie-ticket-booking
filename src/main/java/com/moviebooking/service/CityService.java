package com.moviebooking.service;

import com.moviebooking.entity.City;
import com.moviebooking.exception.DuplicateResourceException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.CityRepository;
import com.moviebooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public City createCity(String name) {
        if (cityRepository.existsByName(name)) {
            throw new DuplicateResourceException("City with name '" + name + "' already exists");
        }
        City city = City.builder().name(name).build();
        log.info("Creating city: {}", name);
        return cityRepository.save(city);
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    @Transactional
    public City updateCity(Long id, String name) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        if (cityRepository.existsByName(name)) {
            throw new DuplicateResourceException("City with name '" + name + "' already exists");
        }
        city.setName(name);
        log.info("Updating city id={} to name={}", id, name);
        return cityRepository.save(city);
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
        if (!theaterRepository.findByCityId(id).isEmpty()) {
            throw new DuplicateResourceException("Cannot delete city with existing theaters");
        }
        log.info("Deleting city id={}", id);
        cityRepository.delete(city);
    }
}
