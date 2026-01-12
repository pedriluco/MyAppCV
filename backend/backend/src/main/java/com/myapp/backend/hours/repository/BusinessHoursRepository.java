package com.myapp.backend.hours.repository;

import com.myapp.backend.hours.entity.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {

    List<BusinessHours> findByTenantId(Long tenantId);

    Optional<BusinessHours> findByTenantIdAndDayOfWeek(Long tenantId, Integer dayOfWeek);
}
