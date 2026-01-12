package com.myapp.backend.appointments;

import com.myapp.backend.appointments.dto.CreateAppointmentRequest;
import com.myapp.backend.appointments.dto.AppointmentResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository repository;

    public AppointmentController(AppointmentRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AppointmentResponse> getByTenant(@RequestParam Long tenantId) {
        return repository.findByTenantId(tenantId)
                .stream()
                .map(a -> new AppointmentResponse(
                        a.getId(),
                        a.getTenantId(),
                        a.getServiceId(),
                        a.getClientName(),
                        a.getDate().toString(),   // LocalDate -> String
                        a.getTime().toString()    // LocalTime -> String
                ))
                .toList();
    }

    @PostMapping
    public AppointmentResponse create(@RequestBody CreateAppointmentRequest req) {
        LocalDate date = LocalDate.parse(req.getDate()); // "2026-01-15"
        LocalTime time = LocalTime.parse(req.getTime()); // "16:00"

        Appointment a = new Appointment();
        a.setTenantId(req.getTenantId());
        a.setServiceId(req.getServiceId());
        a.setClientName(req.getClientName());
        a.setDate(date);
        a.setTime(time);

        Appointment saved = repository.save(a);

        return new AppointmentResponse(
                saved.getId(),
                saved.getTenantId(),
                saved.getServiceId(),
                saved.getClientName(),
                saved.getDate().toString(),
                saved.getTime().toString()
        );
    }
}
