package com.hvtc.bakery.repository;

import com.hvtc.bakery.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    Optional<Workshop> findBySlug(String slug);
    List<Workshop> findByStatusTrueOrderByWorkshopDateAsc();
}
