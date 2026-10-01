package com.hvtc.bakery.service;

import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.entity.Workshop;
import com.hvtc.bakery.entity.WorkshopRegistration;

import java.util.List;

public interface WorkshopService {
    List<Workshop> findAllActiveWorkshops();
    List<Workshop> findAllWorkshops();
    Workshop findBySlug(String slug);
    Workshop findById(Long id);
    WorkshopRegistration registerWorkshop(Long workshopId, User user, String name, String phone, String email, String note);
    Workshop saveWorkshop(Workshop workshop);
    void deleteWorkshop(Long id);
    List<WorkshopRegistration> getRegistrationsForWorkshop(Long workshopId);
}
