# Auction Marketplace Platform

A Spring Boot application for real-time online auctions with secure user authentication, role-based authorization, and live bidding updates through WebSockets (STOMP).

## Features

### Authentication & Authorization
- JWT-based login and access control
- BCrypt password hashing
- Role-based authorization using Spring Security 6  
  - USER: buyers/sellers  
  - ADMIN: privileged operations

### Product Management
- Users add products with category and details
- Keyword search and pagination supported

### Auction System
- Sellers create auctions for their products
- Auction lifecycle:
  - UPCOMING → OPEN → CLOSED
- Only owner or admin can open/close/delete auctions

### Real-Time Bidding (WebSockets + STOMP)
- Live price updates broadcast to subscribers
- Highest bidder tracking
- Secure WebSocket connections through authentication/authorization

### Bid Tracking
- Users view their bid history
- Auction pages show active bids sorted by price

## Tech Stack

- Spring Boot 3
- Spring Security 6 + JWT
- STOMP WebSockets for live updates
- JPA / Hibernate
- JUnit + Mockito for service testing

## Modules Overview

| Module | Responsibility |
|--------|----------------|
| User Service | Auth + registration + roles |
| Product Service | CRUD + search/filter |
| Auction Service | Auction lifecycle + ownership checks |
| Bid Service | Placing bids + real-time updates |
| WebSocket Layer | STOMP messaging broadcast |

## Highlights
- Secure access enforcement everywhere (HTTP + WebSocket)
- Transaction-safe bidding logic ensures data integrity
- Lightweight DTO > Entity mapping structure

---
