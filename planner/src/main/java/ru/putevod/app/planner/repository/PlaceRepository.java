package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
} 