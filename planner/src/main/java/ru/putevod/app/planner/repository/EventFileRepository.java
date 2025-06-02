package ru.putevod.app.planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.EventFile;
import ru.putevod.app.planner.model.File;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventFileRepository extends JpaRepository<EventFile, Long> {

    List<EventFile> findByEvent(Event event);

    List<EventFile> findByFile(File file);

    Optional<EventFile> findByEventAndFile(Event event, File file);

    void deleteByEventAndFile(Event event, File file);

    boolean existsByEventAndFile(Event event, File file);
} 