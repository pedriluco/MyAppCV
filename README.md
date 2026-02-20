MyApp — Multi-Tenant Appointment Platform

MyApp is a full-stack multi-tenant appointment management platform designed to simulate real-world SaaS architecture patterns rather than a basic CRUD application. It supports role-based authentication (USER, OWNER, ADMIN), tenant-level isolation, business approval workflows, service configuration, business hours management, and a moderated appointment lifecycle.
The system was built with a strong focus on architectural clarity and separation of concerns. The backend uses Spring Boot with JWT-based stateless authentication and a dedicated authorization layer to enforce tenant access rules. The Android client is built with Jetpack Compose and follows an MVVM architecture using StateFlow and Retrofit for reactive, state-driven UI management.
A simplified and more basic version of this system was implemented in a real-world dental clinic to manage appointments and operational flows, validating the architectural decisions in a production-like environment.

  Backend:
Java
Spring Boot
Spring Security (JWT)
JPA / Hibernate
PostgreSQL

  Android Client:
Kotlin
Jetpack Compose
MVVM
Retrofit
Coroutines
StateFlow

Core Functionality:

The platform implements a complete business lifecycle. Business owners can create branches (tenants) that require administrative approval before becoming active. Once approved, owners can configure services, define business hours, and moderate appointment requests. Users can browse active businesses and request appointments.
Appointments follow a controlled lifecycle (REQUESTED → APPROVED → REJECTED) and the agenda system includes dynamic filtering by status and time, including automatic hiding of past appointments. The UI adapts dynamically to user roles and tenant state to prevent invalid actions and unnecessary backend errors.

Architecture Highlights:
Stateless JWT authentication with role claims
Service-layer tenant authorization validation
Multi-tenant isolation enforced at the backend
UI-level role restrictions synchronized with backend logic
Centralized Snackbar system for consistent feedback
Business hours upsert logic with default week generation
Reactive state management with ViewModel + StateFlow

Features Delivered in v1:

JWT authentication
Role-based access control
Multi-tenant architecture
Admin approval workflow
Service management
Business hours configuration
Appointment moderation
Agenda filtering (status + past appointments)
Clean loading and error states

Planned Improvements (v1.1):

The next iteration will include:
Users can view their own booked appointments
Appointment cancellation and rescheduling
Improved owner dashboard experience
Enhanced feedback states and UX refinements

About the author

I Built as part of a continuous full-stack development journey focused on:
Applied Mathematics & Computing (My major)
Architecture design
Clean state management
Secure backend patterns
