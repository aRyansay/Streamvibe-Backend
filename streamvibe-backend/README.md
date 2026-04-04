# StreamVibe — Java Spring Boot Backend

## Project Structure
```
streamvibe-backend/
├── pom.xml
├── src/main/java/com/streamvibe/
│   ├── StreamVibeApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── VideoController.java
│   │   ├── ReelController.java
│   │   ├── FriendController.java
│   │   └── ShareController.java
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── VideoDto.java
│   │   ├── ReelDto.java
│   │   └── ShareDto.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Video.java
│   │   ├── Reel.java
│   │   ├── Friendship.java
│   │   └── Share.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── VideoRepository.java
│   │   ├── ReelRepository.java
│   │   ├── FriendshipRepository.java
│   │   └── ShareRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── VideoService.java
│   │   ├── ReelService.java
│   │   ├── FriendService.java
│   │   └── ShareService.java
│   └── security/
│       ├── JwtUtil.java
│       └── JwtFilter.java
└── src/main/resources/
    ├── application.properties
    └── schema.sql
```

## Running
1. Install Java 17+, Maven 3.8+, MySQL 8.0+
2. Create DB: `CREATE DATABASE streamvibe;`
3. Update `application.properties` with your DB credentials
4. `mvn spring-boot:run`

## API Endpoints
- POST /api/auth/register
- POST /api/auth/login
- GET  /api/videos            (public)
- POST /api/videos            (auth)
- GET  /api/videos/{id}       (public, increments view)
- DELETE /api/videos/{id}     (auth, owner only)
- GET  /api/reels              (public)
- POST /api/reels              (auth)
- GET  /api/friends            (auth)
- POST /api/friends/request    (auth)
- PUT  /api/friends/{id}/accept(auth)
- DELETE /api/friends/{id}     (auth)
- POST /api/shares             (auth)
- GET  /api/shares/inbox       (auth)
- DELETE /api/shares/{id}      (auth, soft-delete)
