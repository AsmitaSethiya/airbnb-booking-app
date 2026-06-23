# Airbnb Booking Application

## Overview

A full-stack backend application inspired by Airbnb, built using Java, Spring Boot, PostgreSQL, Spring Security, JWT Authentication, and Stripe Payment Integration.

The application allows users to search hotels, book rooms, add guests, make payments, and manage bookings. Hotel managers can manage hotels, rooms, and inventory.

---

## Features

### Authentication & Authorization

* User Registration (Signup)
* User Login
* JWT Access Token
* JWT Refresh Token
* Role-Based Authorization
* Spring Security Integration

### Hotel Management

* Create Hotel
* Update Hotel Details
* Manage Hotel Information

### Room Management

* Add Rooms
* Update Rooms
* Delete Rooms
* Room Availability Management

### Inventory Management

* Daily Inventory Tracking
* Room Availability Validation
* Inventory Locking
* Surge Pricing Support
* Open/Close Inventory

### Booking Management

* Search Available Hotels
* Create Booking
* Add Guests
* Booking Status Tracking
* Booking Confirmation

### Payment Integration

* Stripe Checkout Integration
* Secure Payment Processing
* Stripe Webhooks
* Automatic Booking Confirmation After Payment

### Security

* JWT Authentication
* Custom JWT Filter
* Stateless Session Management
* Role-Based Access Control

### API Documentation

* Swagger/OpenAPI Integration

---

## Tech Stack

### Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* PostgreSQL

### Authentication

* JWT (JSON Web Token)

### Payment Gateway

* Stripe

### Documentation

* Swagger/OpenAPI

### Build Tool

* Maven

---

## Architecture

Controller Layer

↓

Service Layer

↓

Repository Layer

↓

PostgreSQL Database

---

## Security Flow

1. User signs up.
2. Password is encrypted using BCrypt.
3. User logs in.
4. JWT Access Token and Refresh Token are generated.
5. Every secured API validates JWT using a custom JWT filter.
6. User roles determine API access.

---

## Booking Flow

1. User searches available hotels.
2. User selects room and booking dates.
3. Inventory is reserved.
4. Stripe Checkout Session is created.
5. User completes payment.
6. Stripe sends webhook event.
7. Booking status becomes CONFIRMED.
8. Inventory is updated.

---

## Key Concepts Implemented

* JWT Authentication
* Spring Security
* Role-Based Authorization
* REST APIs
* DTO Mapping
* Global Exception Handling
* Stripe Webhooks
* Inventory Management
* Concurrency Handling
* Pessimistic Locking
* Transaction Management
* Swagger Documentation

---

## API Documentation

Swagger UI:

http://localhost:8080/api/v1/swagger-ui/index.html

---

## Future Enhancements

* Refund Management
* Booking Cancellation
* Email Notifications
* AWS Deployment
* CI/CD Pipeline
* Microservices Architecture

---

## Author

Asmita Sethiya

Java Backend Developer | Spring Boot Developer
