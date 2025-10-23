package com.school.equipmentlending.repository;

import com.school.equipmentlending.model.BookingRequest;
import com.school.equipmentlending.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByRequester_Username(String username);
    List<BookingRequest> findByStatus(BookingStatus status);
    List<BookingRequest> findByEquipment_IdAndStatus(Long equipmentId, BookingStatus status);
}
