package com.hvtc.bakery.repository;

import com.hvtc.bakery.entity.WorkshopRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkshopRegistrationRepository extends JpaRepository<WorkshopRegistration, Long> {
    List<WorkshopRegistration> findByWorkshopIdOrderByRegisteredAtDesc(Long workshopId);
    List<WorkshopRegistration> findByUserIdOrderByRegisteredAtDesc(Long userId);
    long countByWorkshopId(Long workshopId);
}
