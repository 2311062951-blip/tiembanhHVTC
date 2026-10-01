package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.entity.Workshop;
import com.hvtc.bakery.entity.WorkshopRegistration;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.WorkshopRegistrationRepository;
import com.hvtc.bakery.repository.WorkshopRepository;
import com.hvtc.bakery.service.WorkshopService;
import com.hvtc.bakery.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopRegistrationRepository registrationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Workshop> findAllActiveWorkshops() {
        return workshopRepository.findByStatusTrueOrderByWorkshopDateAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Workshop> findAllWorkshops() {
        return workshopRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Workshop findBySlug(String slug) {
        return workshopRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Workshop: " + slug));
    }

    @Override
    @Transactional(readOnly = true)
    public Workshop findById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Workshop ID: " + id));
    }

    @Override
    @Transactional
    public WorkshopRegistration registerWorkshop(Long workshopId, User user, String name, String phone, String email, String note) {
        Workshop workshop = findById(workshopId);

        if (workshop.getRegisteredCount() >= workshop.getCapacity()) {
            throw new BadRequestException("Lớp học làm bánh này đã đủ số lượng học viên đăng ký!");
        }

        WorkshopRegistration reg = WorkshopRegistration.builder()
                .workshop(workshop)
                .user(user)
                .customerName(name.trim())
                .phone(phone.trim())
                .email(email.trim())
                .note(note)
                .build();

        workshop.setRegisteredCount(workshop.getRegisteredCount() + 1);
        workshopRepository.save(workshop);

        return registrationRepository.save(reg);
    }

    @Override
    @Transactional
    public Workshop saveWorkshop(Workshop workshop) {
        if (workshop.getSlug() == null || workshop.getSlug().isBlank()) {
            workshop.setSlug(SlugUtils.toSlug(workshop.getName()));
        }
        return workshopRepository.save(workshop);
    }

    @Override
    @Transactional
    public void deleteWorkshop(Long id) {
        Workshop workshop = findById(id);
        workshopRepository.delete(workshop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkshopRegistration> getRegistrationsForWorkshop(Long workshopId) {
        return registrationRepository.findByWorkshopIdOrderByRegisteredAtDesc(workshopId);
    }
}
