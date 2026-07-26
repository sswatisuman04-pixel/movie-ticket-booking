package com.moviebooking.repository;

import com.moviebooking.entity.ShowSeat;
import com.moviebooking.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    List<ShowSeat> findByShowId(Long showId);

    List<ShowSeat> findByShowIdAndStatus(Long showId, ShowSeatStatus status);

    List<ShowSeat> findByHeldByIdAndShowIdAndStatus(Long userId, Long showId, ShowSeatStatus status);

    @Query("SELECT ss FROM ShowSeat ss WHERE ss.status = 'HELD' AND ss.holdExpiresAt < :now")
    List<ShowSeat> findExpiredHolds(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.heldBy = null, ss.holdExpiresAt = null " +
            "WHERE ss.status = 'HELD' AND ss.holdExpiresAt < :now")
    int releaseExpiredHolds(@Param("now") LocalDateTime now);
}
