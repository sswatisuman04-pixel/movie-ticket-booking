package com.moviebooking.service;

import com.moviebooking.entity.City;
import com.moviebooking.entity.Theater;
import com.moviebooking.exception.DuplicateResourceException;
import com.moviebooking.exception.ResourceNotFoundException;
import com.moviebooking.repository.CityRepository;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final CityRepository cityRepository;
    private final ScreenRepository screenRepository;

    @Transactional
    public Theater createTheater(Long cityId, String name, String address) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));
        if (theaterRepository.existsByNameAndCityId(name, cityId)) {
            throw new DuplicateResourceException("Theater with name '" + name + "' already exists in this city");
        }
        Theater theater = Theater.builder()
                .name(name)
                .address(address)
                .city(city)
                .build();
        log.info("Creating theater: {} in city id={}", name, cityId);
        return theaterRepository.save(theater);
    }

    public List<Theater> getTheatersByCity(Long cityId) {
        return theaterRepository.findByCityId(cityId);
    }

    @Transactional
    public Theater updateTheater(Long id, String name, String address) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        if (name != null && !name.equals(theater.getName())) {
            if (theaterRepository.existsByNameAndCityId(name, theater.getCity().getId())) {
                throw new DuplicateResourceException("Theater with name '" + name + "' already exists in this city");
            }
            theater.setName(name);
        }
        if (address != null) {
            theater.setAddress(address);
        }
        log.info("Updating theater id={}", id);
        return theaterRepository.save(theater);
    }

    @Transactional
    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
        if (!screenRepository.findByTheaterId(id).isEmpty()) {
            throw new DuplicateResourceException("Cannot delete theater with existing screens");
        }
        log.info("Deleting theater id={}", id);
        theaterRepository.delete(theater);
    }
}
