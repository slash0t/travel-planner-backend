package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.File;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripFile;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripFileRepository extends JpaRepository<TripFile, Long> {
    
    List<TripFile> findByTrip(Trip trip);
    
    List<TripFile> findByFile(File file);
    
    Optional<TripFile> findByTripAndFile(Trip trip, File file);
    
    void deleteByTripAndFile(Trip trip, File file);
    
    boolean existsByTripAndFile(Trip trip, File file);
} 