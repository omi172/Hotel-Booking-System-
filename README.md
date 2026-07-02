#Hotel Booking System — Detailed Project Explanation
#1. Introduction
The Hotel Booking System is a full-stack web application built using Spring Boot, MongoDB, and Java that enables users to search for hotels, view available rooms, and make bookings with payment processing. The system implements secure authentication using JWT tokens and role-based access control (RBAC) to differentiate between regular Users and Administrators.

This project replicates the core functionality of real-world hotel booking platforms like Booking.com, OYO, and MakeMyTrip at a smaller scale, designed for learning and portfolio demonstration.


#2. Problem Statement
In the hospitality industry, there is a need for a centralized platform where:

Users can search hotels by location, check room availability for specific dates, and make bookings
Administrators can manage hotel records (add new hotels, update information, remove listings)
The system must prevent double-bookings (two users booking the same room for overlapping dates)
Payment processing must be the final confirmation step before a booking is finalized
All operations must be secure with proper authentication and authorization

#3. Objectives
Build a RESTful API backend using Spring Boot and MongoDB
Implement secure user authentication with JWT and BCrypt password hashing
Create role-based access control (Admin/User)
Enable admins to perform full CRUD operations on hotel records
Develop a booking engine with intelligent date conflict detection
Build a payment module with transaction tracking and retry mechanism
Implement hotel search with room availability filtering
Write comprehensive unit tests using JUnit 5 and Mockito
Create a Thymeleaf-based frontend for user interaction

#4. System Architecture
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                              │
│          (Thymeleaf Frontend / Postman / React)                   │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP Requests (JSON)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                               │
│  ┌─────────────┐  ┌──────────────────┐  ┌───────────────────┐  │
│  │ CORS Filter │→ │ JWT Auth Filter   │→ │ Authorization      │  │
│  │             │  │ (Extract Token,   │  │ (@PreAuthorize)    │  │
│  │             │  │  Validate, Set    │  │                    │  │
│  │             │  │  SecurityContext)  │  │                    │  │
│  └─────────────┘  └──────────────────┘  └───────────────────┘  │
└──────────────────────────┬──────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                              │
│  ┌──────────────┐ ┌───────────────┐ ┌───────────────────────┐  │
│  │AuthController│ │HotelController│ │BookingController       │  │
│  │/api/auth/*   │ │/api/hotels/*  │ │/api/bookings/*         │  │
│  └──────┬───────┘ └───────┬───────┘ └───────────┬───────────┘  │
│  ┌──────────────┐ ┌───────────────┐ ┌───────────────────────┐  │
│  │RoomController│ │SearchController│ │PaymentController      │  │
│  │/api/rooms/*  │ │/api/search/*  │ │/api/payments/*         │  │
│  └──────┬───────┘ └───────┬───────┘ └───────────┬───────────┘  │
└─────────┼─────────────────┼─────────────────────┼───────────────┘
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                                │
│  ┌──────────────┐ ┌───────────────┐ ┌───────────────────────┐  │
│  │ AuthService  │ │ HotelService  │ │  BookingService        │  │
│  │(Register,    │ │(CRUD Hotels)  │ │(Book, Cancel,          │  │
│  │ Login, JWT)  │ │               │ │ Conflict Detection)    │  │
│  └──────┬───────┘ └───────┬───────┘ └───────────┬───────────┘  │
│  ┌──────────────┐ ┌───────────────┐ ┌───────────────────────┐  │
│  │ RoomService  │ │ SearchService │ │  PaymentService        │  │
│  │(CRUD Rooms)  │ │(Search+Filter)│ │(Pay, Retry, Track)     │  │
│  └──────┬───────┘ └───────┬───────┘ └───────────┬───────────┘  │
└─────────┼─────────────────┼─────────────────────┼───────────────┘
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                               │
│  ┌──────────────┐ ┌───────────────┐ ┌───────────────────────┐  │
│  │UserRepository│ │HotelRepository│ │BookingRepository       │  │
│  │              │ │RoomRepository │ │PaymentRepository       │  │
│  └──────┬───────┘ └───────┬───────┘ └───────────┬───────────┘  │
└─────────┼─────────────────┼─────────────────────┼───────────────┘
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATABASE LAYER                               │
│                         MongoDB                                   │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐   │
│  │ users  │ │ hotels │ │ rooms  │ │ bookings │ │ payments │   │
│  └────────┘ └────────┘ └────────┘ └──────────┘ └──────────┘   │
└─────────────────────────────────────────────────────────────────┘

#5. Modules in Detail
Module 1: User Module (Authentication & Authorization)
Purpose: Manages user registration, login, and access control.

How it works:

User registers with email, password, and name
Password is hashed using BCrypt (one-way encryption) before storing in MongoDB
On login, credentials are verified and a JWT token is generated
JWT token contains: user email (subject), role (claim), issued time, expiration time
Every subsequent API request must include this token in the Authorization: Bearer <token> header
The JwtAuthenticationFilter validates the token on every request
If valid → SecurityContext is populated → request proceeds
If invalid/expired → 401 Unauthorized is returned
Key Components:

User.java — Entity: id, email, password (hashed), name, role (ROLE_USER/ROLE_ADMIN)
AuthController.java — Endpoints: /api/auth/register, /api/auth/login
AuthService.java — Business logic: validate, hash password, generate token
JwtUtils.java — Token operations: generate, validate, extract claims
JwtAuthenticationFilter.java — Intercepts every request, validates JWT
SecurityConfig.java — Configures filter chain, CORS, session management
Security Flow:

REGISTRATION:
User → POST /api/auth/register → Validate Input → Hash Password → Save to MongoDB → Return Success

LOGIN:
User → POST /api/auth/login → Find User by Email → Verify Password with BCrypt
     → Generate JWT Token (email + role + expiry) → Return Token

API CALL:
User → GET /api/hotels (with Bearer Token) → JwtFilter extracts token
     → Validate signature & expiry → Load UserDetails → Set SecurityContext
     → @PreAuthorize checks role → Controller executes → Return Response
Example JWT Token Payload:

{
  "sub": "user@example.com",
  "role": "ROLE_USER",
  "iat": 1719645000,
  "exp": 1719731400
}
Module 2: Hotel Module (Admin Management)
Purpose: Allows administrators to create, update, view, and delete hotel records. Users can only view.

How it works:

Admin logs in and receives JWT token with ROLE_ADMIN
Admin can add new hotels with: name, city, address, description, rating
Admin can update any hotel's information
Admin can delete hotels (hard delete from database)
Regular users can view all hotels and search by city
All write operations are protected with @PreAuthorize("hasRole('ADMIN')")
Key Components:

Hotel.java — Entity: id, name, city, address, description, rating, createdAt, updatedAt
HotelController.java — REST endpoints with role-based protection
HotelService.java — Business logic for CRUD operations
HotelRepository.java — MongoDB queries (findByCity, findByRating)
Access Control Matrix:

┌────────────────┬───────┬──────┐
│ Operation      │ ADMIN │ USER │
├────────────────┼───────┼──────┤
│ Create Hotel   │  ✅   │  ❌  │
│ Update Hotel   │  ✅   │  ❌  │
│ Delete Hotel   │  ✅   │  ❌  │
│ View Hotels    │  ✅   │  ✅  │
│ Search by City │  ✅   │  ✅  │
└────────────────┴───────┴──────┘
Example Hotel Document in MongoDB:

{
  "_id": "ObjectId('64a1b2c3d4e5f6a7b8c9d0e1')",
  "name": "Taj Palace",
  "city": "Mumbai",
  "address": "Colaba, Mumbai, Maharashtra 400005",
  "description": "5-star luxury hotel with sea view",
  "rating": 4.8,
  "createdAt": "2026-07-01T10:00:00",
  "updatedAt": "2026-07-01T10:00:00"
}
Module 3: Room Module (Room Management)
Purpose: Manages rooms within each hotel — each room belongs to a specific hotel.

How it works:

Admin adds rooms to a hotel with: room number, type (SINGLE/DOUBLE/SUITE), price per night, capacity
Each room is linked to a hotel via hotelId
Room availability is determined dynamically based on existing bookings
Users can view rooms for a specific hotel
Key Components:

Room.java — Entity: id, hotelId, roomNumber, type, pricePerNight, capacity, available
RoomController.java — CRUD endpoints
RoomService.java — Business logic
RoomRepository.java — Queries: findByHotelId, findAvailableRooms
Room Types:

SINGLE  → 1 guest,  ₹2,000/night
DOUBLE  → 2 guests, ₹3,500/night
DELUXE  → 2 guests, ₹5,000/night
SUITE   → 4 guests, ₹8,000/night
Example Room Document:

{
  "_id": "ObjectId('room101')",
  "hotelId": "ObjectId('hotel123')",
  "roomNumber": "101",
  "type": "DELUXE",
  "pricePerNight": 5000.0,
  "capacity": 2,
  "available": true,
  "createdAt": "2026-07-01T10:30:00"
}
Module 4: Search Module (Hotel & Room Discovery)
Purpose: Enables users to search for hotels based on location and view rooms available for a selected date range.

How it works:

User provides: city/location + check-in date + check-out date
System finds all hotels in that city
For each hotel, finds all rooms
For each room, checks if any CONFIRMED booking overlaps with requested dates
Filters out rooms that are already booked (conflict detection)
Returns only hotels that have at least one available room
Response includes hotel details + list of available rooms with prices
The Challenge — No JOINs in MongoDB:

In SQL: SELECT rooms JOIN hotels JOIN bookings WHERE...
In MongoDB: Must query 3 collections separately and filter in service layer
Search Algorithm:

Input: city = "Mumbai", checkIn = July 15, checkOut = July 18

Step 1: Find hotels → hotelRepository.findByCityIgnoreCase("Mumbai")
        Result: [Taj Palace, Oberoi, Marriott]

Step 2: For each hotel, find rooms → roomRepository.findByHotelId(hotelId)
        Taj Palace rooms: [Room101, Room102, Room103]

Step 3: For each room, check conflicts:
        bookingRepository.findConflictingBookings(roomId, checkIn, checkOut)

        Room101: Existing booking July 14-17 → CONFLICT → ❌ Exclude
        Room102: No bookings → ✅ Available
        Room103: Existing booking July 20-25 → No conflict → ✅ Available

Step 4: Return Taj Palace with [Room102, Room103] as available

Output: Hotels with available rooms for the requested dates
Module 5: Booking Module (Reservation Management)
Purpose: Lets users book available rooms for specific check-in and check-out dates with conflict detection.

How it works:

User selects a hotel and room, provides check-in and check-out dates
System validates: check-out must be after check-in
System checks for date conflicts — ensures no existing CONFIRMED booking overlaps
If no conflict → booking is created with status CONFIRMED
User can view their booking history
User can cancel their own bookings (status changes to CANCELLED)
Admin can view ALL bookings across all users
Date Conflict Detection Algorithm:

A new booking CONFLICTS with an existing booking if:
  existing.checkIn < new.checkOut  AND  existing.checkOut > new.checkIn

This single condition catches ALL 4 types of overlaps:
┌─────────────────────────────────────────────────────────┐
│ Existing:        July 15 ──────── July 18               │
│                                                         │
│ Case 1: July 13 ────── July 16        → CONFLICT       │
│ Case 2:           July 17 ────── July 20 → CONFLICT    │
│ Case 3:           July 16 ── July 17   → CONFLICT      │
│ Case 4: July 14 ──────────────── July 20 → CONFLICT   │
│ Case 5: July 10 ── July 14            → NO CONFLICT    │
│ Case 6:                    July 19 ── July 22 → OK     │
└─────────────────────────────────────────────────────────┘
MongoDB Query for Conflict Detection:

@Query("{'roomId': ?0, 'status': 'CONFIRMED', 'checkInDate': {$lt: ?2}, 'checkOutDate': {$gt: ?1}}")
List<Booking> findConflictingBookings(String roomId, LocalDate checkIn, LocalDate checkOut);
Booking Statuses:

CONFIRMED → Active booking (room is reserved)
CANCELLED → User cancelled (room becomes available again)
COMPLETED → Stay is over (historical record)
Booking Flow:

User selects room → Validate dates → Check conflicts → No conflict?
  → YES: Create booking (CONFIRMED) → Proceed to payment
  → NO:  Return error "Room already booked for selected dates"
Example Booking Document:

{
  "_id": "ObjectId('booking123')",
  "userId": "user@example.com",
  "hotelId": "ObjectId('hotel123')",
  "roomId": "ObjectId('room101')",
  "checkInDate": "2026-07-15",
  "checkOutDate": "2026-07-18",
  "status": "CONFIRMED",
  "createdAt": "2026-07-01T14:30:00",
  "updatedAt": "2026-07-01T14:30:00"
}
Module 6: Payment Module (Transaction Processing)
Purpose: Simulates payment processing as the final step before booking confirmation.

How it works:

After a successful booking, user initiates payment
System validates: booking exists, belongs to user, not already paid, not cancelled
Payment is processed (simulated with 90% success rate)
If SUCCESS → transaction ID generated, payment recorded
If FAILED → user can retry the payment
Idempotency check prevents duplicate payments for same booking
Admin can view all payment records
Payment Flow:

User books room → Booking CONFIRMED → User clicks "Pay"
  → Validate booking ownership
  → Check if already paid (idempotency)
  → Check if booking is cancelled
  → Process payment (simulate)
  → SUCCESS: Record payment, generate transaction ID
  → FAILED: Allow retry

Retry Flow:
  → User clicks "Retry" on failed payment
  → Validate: payment exists, status is FAILED, user owns it
  → Re-process payment
  → Update status and transaction ID
Payment Statuses:

SUCCESS → Payment completed, booking is finalized
FAILED  → Payment failed, user can retry
Idempotency Protection:

Before processing any payment:
1. Check: Does a SUCCESS payment already exist for this bookingId?
2. If YES → Reject with "Payment already completed. TXN: TXN-ABC123"
3. If NO → Proceed with payment processing
Example Payment Document:

{
  "_id": "ObjectId('payment123')",
  "bookingId": "ObjectId('booking123')",
  "userId": "user@example.com",
  "amount": 15000.0,
  "paymentMethod": "UPI",
  "paymentStatus": "SUCCESS",
  "transactionId": "TXN-A3F8B2C1",
  "createdAt": "2026-07-01T14:35:00",
  "updatedAt": "2026-07-01T14:35:00"
}
#6. Complete User Journey
┌─────────────────────────────────────────────────────────────────┐
│                    COMPLETE BOOKING FLOW                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. REGISTER                                                     │
│     POST /api/auth/register                                      │
│     {email, password, name}                                      │
│     → Account created, password hashed with BCrypt               │
│                                                                  │
│  2. LOGIN                                                        │
│     POST /api/auth/login                                         │
│     {email, password}                                            │
│     → JWT token returned (valid 24 hours)                        │
│                                                                  │
│  3. SEARCH HOTELS                                                │
│     GET /api/search?city=Mumbai&checkIn=2026-07-15&checkOut=18   │
│     → Returns hotels with available rooms for those dates        │
│                                                                  │
│  4. VIEW ROOM DETAILS                                            │
│     GET /api/rooms/{roomId}                                      │
│     → Room type, price, capacity, amenities                      │
│                                                                  │
│  5. BOOK ROOM                                                    │
│     POST /api/bookings                                           │
│     {hotelId, roomId, checkInDate, checkOutDate}                 │
│     → Conflict check → Booking CONFIRMED                        │
│                                                                  │
│  6. MAKE PAYMENT                                                 │
│     POST /api/payments                                           │
│     {bookingId, amount, paymentMethod: "UPI"}                    │
│     → Payment SUCCESS → Transaction ID generated                 │
│                                                                  │
│  7. VIEW MY BOOKINGS                                             │
│     GET /api/bookings/my                                         │
│     → List of all user's bookings with status                    │
│                                                                  │
│  8. CANCEL BOOKING (if needed)                                   │
│     PUT /api/bookings/{id}/cancel                                │
│     → Status changed to CANCELLED, room becomes available        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
#7. Database Design (MongoDB Collections)
Users Collection
{
  "_id": "ObjectId",
  "email": "user@example.com",
  "password": "$2a$10$xYz...hashedPassword",
  "name": "Amit Kumar",
  "role": "ROLE_USER",
  "createdAt": "2026-07-01T10:00:00"
}
Hotels Collection
{
  "_id": "ObjectId",
  "name": "Taj Palace",
  "city": "Mumbai",
  "address": "Colaba, Mumbai 400005",
  "description": "5-star luxury hotel with sea view",
  "rating": 4.8,
  "createdAt": "2026-07-01T10:00:00",
  "updatedAt": "2026-07-01T10:00:00"
}
Rooms Collection
{
  "_id": "ObjectId",
  "hotelId": "ObjectId (ref: hotels)",
  "roomNumber": "101",
  "type": "DELUXE",
  "pricePerNight": 5000.0,
  "capacity": 2,
  "available": true,
  "createdAt": "2026-07-01T10:30:00"
}
Bookings Collection
{
  "_id": "ObjectId",
  "userId": "user@example.com",
  "hotelId": "ObjectId (ref: hotels)",
  "roomId": "ObjectId (ref: rooms)",
  "checkInDate": "2026-07-15",
  "checkOutDate": "2026-07-18",
  "status": "CONFIRMED",
  "createdAt": "2026-07-01T14:30:00",
  "updatedAt": "2026-07-01T14:30:00"
}
Payments Collection
{
  "_id": "ObjectId",
  "bookingId": "ObjectId (ref: bookings)",
  "userId": "user@example.com",
  "amount": 15000.0,
  "paymentMethod": "UPI",
  "paymentStatus": "SUCCESS",
  "transactionId": "TXN-A3F8B2C1",
  "createdAt": "2026-07-01T14:35:00",
  "updatedAt": "2026-07-01T14:35:00"
}
Collection Relationships
users ──────┐
            │ (userId)
hotels ─────┼──── rooms (hotelId)
            │
bookings ───┤ (userId, hotelId, roomId)
            │
payments ───┘ (bookingId, userId)
8. API Endpoints (Complete)
Authentication APIs
POST	/api/auth/register	Public	Register new user
POST	/api/auth/login	Public	Login and get JWT token
Hotel APIs
POST	/api/hotels/add	ADMIN	Create new hotel
PUT	/api/hotels/update/{id}	ADMIN	Update hotel info
DELETE	/api/hotels/delete/{id}	ADMIN	Delete hotel
GET	/api/hotels/all	ALL	View all hotels
GET	/api/hotels/{id}	ALL	View hotel by ID
GET	/api/hotels/search?city=	ALL	Search by city
Room APIs
POST	/api/rooms/add	ADMIN	Add room to hotel
PUT	/api/rooms/update/{id}	ADMIN	Update room
DELETE	/api/rooms/delete/{id}	ADMIN	Delete room
GET	/api/rooms/hotel/{hotelId}	ALL	Get rooms by hotel
GET	/api/rooms/{id}	ALL	Get room details
Search APIs
GET	/api/search?city=&checkIn=&checkOut=	ALL	Search available hotels/rooms
Booking APIs
POST	/api/bookings	USER	Create booking
GET	/api/bookings/my	USER	Get my bookings
PUT	/api/bookings/{id}/cancel	USER	Cancel my booking
GET	/api/bookings	ADMIN	Get all bookings
Payment APIs
POST	/api/payments	USER	Make payment
GET	/api/payments/my	USER	Get my payments
GET	/api/payments/{id}	USER	Get payment details
PUT	/api/payments/{id}/retry	USER	Retry failed payment
GET	/api/payments	ADMIN	Get all payments
GET	/api/payments/booking/{bookingId}	USER	Get payments by booking
#9. Security Implementation
Password Security
Registration:
  Raw password: "MyPassword123"
  → BCrypt hash: "$2a$10$xYzAbC123...60characterHash"
  → Stored in MongoDB (original password NEVER stored)

Login:
  User enters: "MyPassword123"
  → BCrypt.matches("MyPassword123", storedHash) → true → Login success
  → BCrypt.matches("WrongPass", storedHash) → false → 401 Unauthorized
JWT Token Structure
Header:    {"alg": "HS256", "typ": "JWT"}
Payload:   {"sub": "user@example.com", "role": "ROLE_USER", "iat": 1719645000, "exp": 1719731400}
Signature: HMACSHA256(header + "." + payload, secretKey)

Final Token: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0.signature
Security Filter Chain Order
Request → CORS Filter → JWT Authentication Filter → Authorization Filter → Controller
   ↓           ↓                ↓                        ↓
(Add CORS  (Extract &      (Check role           (Execute
 headers)   validate JWT)   @PreAuthorize)        business logic)
Role-Based Access Control
ROLE_ADMIN:
  ✅ Create/Update/Delete hotels
  ✅ Create/Update/Delete rooms
  ✅ View all bookings
  ✅ View all payments
  ✅ Everything a USER can do

ROLE_USER:
  ✅ View hotels and rooms
  ✅ Search available hotels
  ✅ Create bookings
  ✅ Cancel own bookings
  ✅ Make payments
  ✅ View own booking/payment history
  ❌ Cannot manage hotels/rooms
  ❌ Cannot view other users' data
10. Design Patterns Used
#




Pattern




Where Used




Purpose




1	MVC	Entire architecture	Separation of concerns (Controller-Service-Repository)
2	Repository	All Repository interfaces	Abstract database operations from business logic
3	DTO	Request/Response classes	Decouple API contract from database schema
4	Builder	@Builder on Response classes	Clean, readable object construction
5	Singleton	Spring Beans (@Service, @Component)	One instance per service in application
6	Chain of Responsibility	Security Filter Chain	Sequential request processing through filters
7	Strategy	Role-based access (@PreAuthorize)	Different behavior based on user role
8	Template Method	OncePerRequestFilter	Skeleton algorithm with customizable steps
9	Dependency Injection	@RequiredArgsConstructor	Loose coupling between layers
10	Factory	ResponseEntity, JWT generation	Object creation without exposing logic
11	Proxy	@PreAuthorize (AOP)	Intercept method calls for authorization
12	Facade	SearchService	Hide complexity of multi-collection queries
13	Observer	Booking status → Payment status	State changes trigger related updates
11. Technology Stack
Language	Java 17+	Core programming language
Framework	Spring Boot 3.x	Application framework with auto-configuration
Database	MongoDB	NoSQL document database for flexible schema
Security	Spring Security	Authentication and authorization framework
Token	JWT (JJWT library)	Stateless session management
Encryption	BCrypt	One-way password hashing
ORM	Spring Data MongoDB	Database abstraction layer
Validation	Jakarta Bean Validation	Input validation with annotations
Frontend	Thymeleaf	Server-side template engine
Build Tool	Maven	Dependency management and build
Testing	JUnit 5 + Mockito + MockMvc	Unit and integration testing
API Testing	Postman	Manual API testing
Version Control	Git & GitHub	Source code management
IDE	IntelliJ IDEA	Development environment
12. Why MongoDB? (Interview Question)
Flexible Schema	Hotel data varies (some have pools, gyms, etc.) — no rigid table structure needed
Document Model	Each hotel document can embed different attributes naturally
Horizontal Scaling	Read-heavy search operations scale well with MongoDB
JSON-Native	REST APIs return JSON; MongoDB stores JSON — no conversion needed
Fast Reads	Search queries are fast with proper indexing
No Complex JOINs	Booking system has clear document boundaries (hotels, rooms, bookings)
Developer Productivity	Spring Data MongoDB makes CRUD operations simple
13. Testing Strategy
Test Pyramid
         ┌─────────────┐
         │   E2E Tests  │  ← Few (full workflow tests)
         │  (Postman)   │
         ├─────────────┤
         │ Integration  │  ← Some (Repository + DB tests)
         │   Tests      │
         ├─────────────┤
         │  Unit Tests  │  ← Many (Service + Controller tests)
         │ (JUnit 5 +  │
         │  Mockito)    │
         └─────────────┘
Test Coverage
Controller Tests: @WebMvcTest + MockMvc
  → Test HTTP status codes (200, 201, 400, 401, 403)
  → Test request/response JSON structure
  → Test role-based access (admin vs user)

Service Tests: @ExtendWith(MockitoExtension.class)
  → Test business logic (conflict detection, validation)
  → Test edge cases (duplicate payment, cancelled booking)
  → Mock repository calls

Repository Tests: @DataMongoTest
  → Test custom queries (findConflictingBookings)
  → Test with embedded MongoDB
Total Tests: 58+
AuthControllerTest     → 6 tests
HotelControllerTest    → 10 tests
RoomControllerTest     → 8 tests
BookingControllerTest  → 6 tests
PaymentControllerTest  → 8 tests
HotelServiceTest       → 6 tests
BookingServiceTest     → 8 tests
PaymentServiceTest     → 6 tests
14. Challenges Faced & Solutions
#
1	MongoSocketReadTimeoutException	Default timeout too short for Atlas	Configured connectTimeoutMS=30000, socketTimeoutMS=60000
2	JWT DecodingException: Illegal base64 character '-'	Secret key had special characters	Used plain alphanumeric key, 32+ characters
3	@PreAuthorize returning 403 for admin	hasRole('ROLE_ADMIN') adds double prefix	Changed to hasRole('ADMIN')
4	CORS blocking frontend requests	No CORS configuration in SecurityConfig	Added CorsConfigurationSource bean
5	Controller tests all returning 401	JWT filter overrides @WithMockUser	Created TestSecurityConfig excluding JWT filter
6	Double-booking same room	No conflict detection	Implemented overlap query in BookingRepository
7	Duplicate payments	No idempotency check	Check existing SUCCESS payment before processing
8	MethodArgumentNotValidException	Missing @Valid or invalid request body	Added proper validation annotations + error handler
15. Key Features Summary
JWT Authentication — Stateless, scalable auth with 24-hour token expiry
BCrypt Password Hashing — One-way encryption, impossible to reverse
Role-Based Access Control — Admin vs User with @PreAuthorize
Hotel CRUD (Admin) — Full management of hotel records
Room Management — Rooms linked to hotels with type and pricing
Smart Search — Search by city with real-time availability filtering
Date Conflict Detection — Prevents double-booking with overlap algorithm
Booking Lifecycle — CONFIRMED → CANCELLED/COMPLETED status tracking
Payment Processing — Simulated payments with SUCCESS/FAILED status
Payment Idempotency — Prevents duplicate charges
Retry Mechanism — Failed payments can be retried
Input Validation — All requests validated with meaningful errors
Global Exception Handling — Consistent error response format
Soft/Hard Delete — Appropriate deletion strategy per module
Comprehensive Testing — 58+ tests with 90%+ coverage
Thymeleaf Frontend — Server-rendered UI for user interaction
16. Project Structure
src/
├── main/
│   ├── java/com/example/HotelBookingSystem/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtUtils.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── CorsConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── HotelController.java
│   │   │   ├── RoomController.java
│   │   │   ├── BookingController.java
│   │   │   ├── PaymentController.java
│   │   │   └── SearchController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── HotelService.java
│   │   │   ├── RoomService.java
│   │   │   ├── BookingService.java
│   │   │   ├── PaymentService.java
│   │   │   └── SearchService.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── HotelRepository.java
│   │   │   ├── RoomRepository.java
│   │   │   ├── BookingRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── Hotel.java
│   │   │   ├── Room.java
│   │   │   ├── Booking.java
│   │   │   ├── Payment.java
│   │   │   ├── Role.java (enum)
│   │   │   ├── RoomType.java (enum)
│   │   │   ├── BookingStatus.java (enum)
│   │   │   └── PaymentStatus.java (enum)
│   │   ├── DTO/
│   │   │   ├── AuthRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   ├── HotelRequest.java
│   │   │   ├── HotelResponse.java
│   │   │   ├── RoomRequest.java
│   │   │   ├── RoomResponse.java
│   │   │   ├── BookingRequest.java
│   │   │   ├── BookingResponse.java
│   │   │   ├── PaymentRequest.java
│   │   │   ├── PaymentResponse.java
│   │   │   └── SearchResponse.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.properties
│       └── templates/
│           ├── index.html
│           ├── login.html
│           ├── register.html
│           ├── hotels.html
│           ├── rooms.html
│           ├── booking.html
│           └── payment.html
├── test/
│   └── java/com/example/HotelBookingSystem/
│       ├── config/
│       │   └── TestSecurityConfig.java
│       ├── controller/
│       │   ├── AuthControllerTest.java
│       │   ├── HotelControllerTest.java
│       │   ├── BookingControllerTest.java
│       │   └── PaymentControllerTest.java
│       └── service/
│           ├── HotelServiceTest.java
│           ├── BookingServiceTest.java
│           └── PaymentServiceTest.java
└── pom.xml
17. Future Enhancements
Email Notifications — Send booking confirmation emails
Real Payment Gateway — Integrate Razorpay/Stripe
Image Upload — Hotel and room photos using AWS S3
Review & Rating System — Users rate hotels after stay
Pagination & Sorting — Handle large datasets efficiently
Redis Caching — Cache frequently searched hotels
Docker Deployment — Containerize with Docker Compose
Swagger/OpenAPI — Auto-generate API documentation
WebSocket Notifications — Real-time booking updates
Microservices Migration — Split into independent services
Admin Dashboard — Analytics on bookings and revenue
Multi-language Support — i18n for global users
18. How to Run the Project
# Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB (local or Atlas)

# Clone the repository
git clone https://github.com/yourusername/hotel-booking-system.git
cd hotel-booking-system

# Configure MongoDB in application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/hotel_booking_db
jwt.secret=YourSuperSecretKeyAtLeast32Characters
jwt.expiration=86400000

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Access the application
http://localhost:8080
19. Conclusion
The Hotel Booking System demonstrates proficiency in building production-ready backend applications using modern Java technologies. It showcases skills in:

RESTful API Design — Clean, resource-oriented endpoints
NoSQL Database Modeling — Efficient document structure with MongoDB
Security Implementation — JWT + BCrypt + RBAC
Algorithm Design — Date conflict detection for preventing double-bookings
Payment Processing — Idempotency, retry mechanism, status tracking
Clean Architecture — Layered design with 13+ design patterns
