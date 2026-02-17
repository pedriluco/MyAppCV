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

    List<Appointment> findByTenantIdAndStartAtBetween(
            Long tenantId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByTenantIdAndStartAtBetweenOrderByStartAtAsc(
            Long tenantId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByTenantIdAndStartAtBetweenAndStatusInOrderByStartAtAsc(
            Long tenantId,
            LocalDateTime start,
            LocalDateTime end,
            List<AppointmentStatus> statuses
    );

    boolean existsByTenantIdAndStatusAndStartAtLessThanAndEndAtGreaterThan(
            Long tenantId,
            AppointmentStatus status,
            LocalDateTime endAt,
            LocalDateTime startAt
    );

    boolean existsByTenantIdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            Long tenantId,
            List<AppointmentStatus> statuses,
            LocalDateTime endAt,
            LocalDateTime startAt
    );
}
