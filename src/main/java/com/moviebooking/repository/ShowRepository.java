package com.moviebooking.repository;

import com.moviebooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByScreenIdAndDate(Long screenId, LocalDate date);

    List<Show> findByScreenId(Long screenId);

    @Query("SELECT s FROM Show s JOIN s.screen sc JOIN sc.theater t WHERE t.city.id = :cityId")
    List<Show> findByCityId(@Param("cityId") Long cityId);

    @Query("SELECT s FROM Show s WHERE LOWER(s.movieName) LIKE LOWER(CONCAT('%', :movieName, '%'))")
    List<Show> searchByMovieName(@Param("movieName") String movieName);
}
