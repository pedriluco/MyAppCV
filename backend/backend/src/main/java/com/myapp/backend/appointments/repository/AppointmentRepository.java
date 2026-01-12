package com.myapp.backend.appointments.repository;

import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByTenantId(Long tenantId);

    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

    // ✅ no-overlap real: buscar APPROVED que se traslape
    boolean existsByTenantIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long tenantId,
            AppointmentStatus status,
            LocalDateTime endAt,
            LocalDateTime startAt
    );
}
