package com.myapp.backend.appointments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByTenantId(Long tenantId);

    // opcional (si quieres validar choques de horario)
    boolean existsByTenantIdAndDateAndTime(Long tenantId, LocalDate date, LocalTime time);
}
