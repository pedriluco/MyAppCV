package com.myapp.backend.config;

import com.myapp.backend.auth.entity.GlobalRole;
import com.myapp.backend.auth.entity.User;
import com.myapp.backend.auth.repository.UserRepository;

import com.myapp.backend.tenant.entity.Tenant;
import com.myapp.backend.tenant.repository.TenantRepository;

import com.myapp.backend.services.entity.BusinessService;
import com.myapp.backend.services.repository.BusinessServiceRepository;

import com.myapp.backend.hours.entity.BusinessHours;
import com.myapp.backend.hours.repository.BusinessHoursRepository;

import com.myapp.backend.appointments.entity.Appointment;
import com.myapp.backend.appointments.entity.AppointmentStatus;
import com.myapp.backend.appointments.repository.AppointmentRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
public class DemoDataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepo;
    private final UserRepository userRepo;
    private final BusinessServiceRepository serviceRepo;
    private final BusinessHoursRepository hoursRepo;
    private final AppointmentRepository appointmentRepo;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(
            TenantRepository tenantRepo,
            UserRepository userRepo,
            BusinessServiceRepository serviceRepo,
            BusinessHoursRepository hoursRepo,
            AppointmentRepository appointmentRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
        this.serviceRepo = serviceRepo;
        this.hoursRepo = hoursRepo;
        this.appointmentRepo = appointmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (tenantRepo.count() > 0) return;

        // -------------------------
        // TENANT
        // -------------------------
        Tenant tenant = new Tenant("Barber Demo");
        tenantRepo.save(tenant);

        // -------------------------
        // USERS
        // -------------------------
        User owner = new User();
        owner.setEmail("owner@demo.com");
        owner.setPassword(passwordEncoder.encode("123456"));
        owner.setGlobalRole(GlobalRole.OWNER);
        userRepo.save(owner);

        User user = new User();
        user.setEmail("user@demo.com");
        user.setPassword(passwordEncoder.encode("123456"));
        user.setGlobalRole(GlobalRole.USER);
        userRepo.save(user);

        // -------------------------
        // SERVICES
        // -------------------------
        BusinessService corte = new BusinessService();
        corte.setTenantId(tenant.getId());
        corte.setName("Corte");
        corte.setDurationMinutes(30);
        serviceRepo.save(corte);

        BusinessService barba = new BusinessService();
        barba.setTenantId(tenant.getId());
        barba.setName("Barba");
        barba.setDurationMinutes(15);
        serviceRepo.save(barba);

        // -------------------------
        // BUSINESS HOURS (L-V)
        // -------------------------
        for (DayOfWeek day : List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        )) {
            BusinessHours h = new BusinessHours();
            h.setTenantId(tenant.getId());
            h.setDayOfWeek(day.getValue()); // 1=Lunes
            h.setOpenTime("10:00");
            h.setCloseTime("19:00");
            h.setClosed(false);
            hoursRepo.save(h);
        }

        // -------------------------
        // APPOINTMENTS
        // -------------------------
        LocalDate base = LocalDate.now().plusDays(1);

        createAppointment(tenant, corte, "Juan Pérez",
                base.atTime(11, 0), 30, AppointmentStatus.REQUESTED);

        createAppointment(tenant, barba, "Ana López",
                base.atTime(12, 0), 15, AppointmentStatus.REQUESTED);

        createAppointment(tenant, corte, "Carlos Ruiz",
                base.atTime(13, 0), 30, AppointmentStatus.APPROVED);

        createAppointment(tenant, barba, "María Gómez",
                base.atTime(14, 0), 15, AppointmentStatus.APPROVED);

        createAppointment(tenant, corte, "Pedro Demo",
                base.atTime(15, 0), 30, AppointmentStatus.REJECTED);

        System.out.println("✔ DEMO DATA CARGADA");
    }

    private void createAppointment(
            Tenant tenant,
            BusinessService service,
            String clientName,
            LocalDateTime start,
            int durationMinutes,
            AppointmentStatus status
    ) {
        Appointment a = new Appointment();
        a.setTenantId(tenant.getId());
        a.setServiceId(service.getId());
        a.setClientName(clientName);
        a.setStartAt(start);
        a.setEndAt(start.plusMinutes(durationMinutes));
        a.setStatus(status);
        appointmentRepo.save(a);
    }
}
