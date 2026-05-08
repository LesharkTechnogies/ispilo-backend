# Jitsi Meet — Spring Boot Integration (No Background Push)

> Stack: Spring Boot · STOMP WebSocket · Jitsi (self-hosted) · PostgreSQL  
> Call model: **app must be open** to receive calls. No FCM, no OneSignal, no background wakeup.
Your AppID is:
vpaas-magic-cookie-1608953a47d743e090494e7f822ab50a
---
private key -----BEGIN PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCOp5/wxpSZIjKm
umb9Jl72IScBDBt5c6CvoABgxh9rdowPgoOnU+ZnalMd6fm5fgn3ErN+jGeam8uP
b6X1p96a/FqOuRWKkcyNxLGxmzz6uKZ/faQoyANkZ/u4ohydApbNLJ7owqMM0A8u
oZMmIIeSPSu4yA9mfkkA2oalC38gdakAOgE4+bHCtPPK6xYCMPS1vxoKRFKD6Wwq
CJjyL5PDbJioQwwDpxKTblAJ+8k3wEiF5PWrgauG4wxITahX3Wvq6z3iwjriFV+x
RSZwFEOFrPG96imY5zrUSfv1PcHOCLB8GoxbASgcEmLwdeG6t4Khj7wWrg0mGknb
aoEcfvCXAgMBAAECggEAZh0XIAMF25ffKfnEPvF4iDay9Mz13bonC5A8tQkc9I+n
MnL6SoyEIQK6/G6/k0flWjbIxY07nDBpRgNliLLhZY++Quu5v7cd9V0W1UuKGu6S
QDBvkcGarATC6ycf1ERGLhgCqBqrHEDSnU6LRn+Ci+u9oEeUvoknOLU0LHBOT6zR
5PQ38EjeS3akvI5QbBGzJ8WbZx4KBqo6+3LEikFZ4EFhc4PAxLBD6afrtiS5vPh5
mdNuo7C8Lr5ecCuSz/CYUI7o9PBjkRYJC0ABp1R0VRqoLehOVei+u92hJVYMF0Ax
W8Xo8kXc2z6U7lkg3KiBIloiKSIzJXlJGTUXcneaAQKBgQDrzktBHDq0H3QPOHKU
OqFetWSRPBDIHWRmCsZHof8lMuYCV8sB4YLz7Vp0VkGbKRrYSgqHKNW4SEA8lxDV
v6N9yOyd6gKEYcrPdzugkXSPVce1xRs0LVn+5CyuEi+/Qv2U/ZiPEkjj7AzLnwIO
yZ7WsbR1qe7aZRf+6Y1TZaupFQKBgQCa3yCJFLV7QbLVn9AZCQia7S81ygvOpzHk
vN7IEwnROAROx1wp3nfkiCbzSXc2QVzYj0C3M+tW9VLC3YmkKz3mytFCAKSv23r3
bEeec6H/ZfKFRIFyHKuBlo1BTDoYTHXuSdGXsfAQUWZrarJBtaw93TKQGUCdlRw7
ehTLAofF+wKBgQCyyoD+xkOwbSTXoYom3+mY4Np20F89DCQixsqeZ7MIZnZFkoGH
VJCxGOd7zCcEWX9UPZeL8adSid3C0xvdped1qRHXjiYIcAmRkqswTPle8MBzDxdY
UYaqICHMflebolSZUksJN+jyf/kXruGlirQXvze/+2/yQbKXAki40h79HQKBgGJF
LM7uzg1M7d4xjwkju0sbL3axIvbZDoa29aH8uqEAK3OVeryyhl8Dqbt8JWHO7cwY
chcNocpG1mIB0uHR8nBsx4VbBST1Xrx6/6Liequlk9LOWlj/KjCLSExjwwVCFx7B
xnW8Gbkw/Y0xYbTiQ03p+riG3YJgaE6xM1px25dbAoGBANOgibkeVJQm8+LvoW+f
Gp1OR4CV0s2Td5jYEVZW86TGtGlbC2Kj3YykUustxqnS/SzRQzdKrvCwxImjb7L+
Li3tkulHepAjPtJ9Qqg7N05fgBTY3E5ubjiSExkr0IA2lujeGfVhTbDCwMBg6I2k
ybP1qNVGM4lZ/Ii0W2P54k/R
-----END PRIVATE KEY-----

-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjqef8MaUmSIyprpm/SZe
9iEnAQwbeXOgr6AAYMYfa3aMD4KDp1PmZ2pTHen5uX4J9xKzfoxnmpvLj2+l9afe
mvxajrkVipHMjcSxsZs8+rimf32kKMgDZGf7uKIcnQKWzSye6MKjDNAPLqGTJiCH
kj0ruMgPZn5JANqGpQt/IHWpADoBOPmxwrTzyusWAjD0tb8aCkRSg+lsKgiY8i+T
w2yYqEMMA6cSk25QCfvJN8BIheT1q4GrhuMMSE2oV91r6us94sI64hVfsUUmcBRD
hazxveopmOc61En79T3BzgiwfBqMWwEoHBJi8HXhureCoY+8Fq4NJhpJ22qBHH7w
lwIDAQAB
-----END PUBLIC KEY-----

integration code :
    <!DOCTYPE html>
    <html>
      <head>
        <script src='https://8x8.vc/vpaas-magic-cookie-1608953a47d743e090494e7f822ab50a/external_api.js' async></script>
        <style>html, body, #jaas-container { height: 100%; }</style>
        <script type="text/javascript">
          window.onload = () => {
            const api = new JitsiMeetExternalAPI("8x8.vc", {
              roomName: "vpaas-magic-cookie-1608953a47d743e090494e7f822ab50a/SampleAppPassiveCollisionsJumpDaily",
              parentNode: document.querySelector('#jaas-container'),
							// Make sure to include a JWT if you intend to record,
							// make outbound calls or use any other premium features!
							// jwt: "eyJraWQiOiJ2cGFhcy1tYWdpYy1jb29raWUtMTYwODk1M2E0N2Q3NDNlMDkwNDk0ZTdmODIyYWI1MGEvYWZhYjU2LVNBTVBMRV9BUFAiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJqaXRzaSIsImlzcyI6ImNoYXQiLCJpYXQiOjE3Nzc3MjY2MjIsImV4cCI6MTc3NzczMzgyMiwibmJmIjoxNzc3NzI2NjE3LCJzdWIiOiJ2cGFhcy1tYWdpYy1jb29raWUtMTYwODk1M2E0N2Q3NDNlMDkwNDk0ZTdmODIyYWI1MGEiLCJjb250ZXh0Ijp7ImZlYXR1cmVzIjp7ImxpdmVzdHJlYW1pbmciOmZhbHNlLCJmaWxlLXVwbG9hZCI6ZmFsc2UsIm91dGJvdW5kLWNhbGwiOmZhbHNlLCJzaXAtb3V0Ym91bmQtY2FsbCI6ZmFsc2UsInRyYW5zY3JpcHRpb24iOmZhbHNlLCJsaXN0LXZpc2l0b3JzIjpmYWxzZSwicmVjb3JkaW5nIjpmYWxzZSwiZmxpcCI6ZmFsc2V9LCJ1c2VyIjp7ImhpZGRlbi1mcm9tLXJlY29yZGVyIjpmYWxzZSwibW9kZXJhdG9yIjp0cnVlLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWQiOiJnb29nbGUtb2F1dGgyfDEwNzU5OTA2MzcwNjI0MTY0NzMxNCIsImF2YXRhciI6IiIsImVtYWlsIjoidGVzdC51c2VyQGNvbXBhbnkuY29tIn19LCJyb29tIjoiKiJ9.WihTG8UR_Z-yVvT_FynMPsEX43QDOMkSu5Py_SAYdd1iWvjl2uGJSXgFMTgPPoY8oH9pnqxqeFLByk4E_FbQ36pFJIxuUhQFyBUrf2ya206sPxJkPJO_C-oaubn-wq4x9c6SGKfVtG5rN_16puyj3XLRmiHLS5WCuGAS2Ocvs6Auudj5CXAsXxsWrOCS4PozWeVKbXTe45vOZzdcvscODiVX6nLMOiga9T6xbCptU3m-Pwd8ifUh-dhT3r2buDAJtvNw7Vg8_-ejMKteid5exq5020kEOqWq0552zLA3NOb-p5VgxHZAXBm2rRSrfvaf8q9sXamxqFu7dcP8my4jig"
            });
          }
        </script>
      </head>
      <body><div id="jaas-container" /></body>
    </html>
  
## Project Structure

```
src/main/java/com/yourapp/
├── config/
│   ├── WebSocketConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── CallController.java
│   └── RoomController.java
├── service/
│   ├── CallService.java
│   ├── RoomService.java
│   ├── RoomTokenService.java
│   └── MeetingReminderJob.java
├── model/
│   ├── Room.java
│   ├── CallSession.java
│   └── User.java          ← assumed already exists
├── repository/
│   ├── RoomRepository.java
│   └── CallSessionRepository.java
├── dto/
│   ├── CallInvitePayload.java
│   ├── CallResponse.java
│   ├── CreateRoomRequest.java
│   └── JoinResponse.java
└── websocket/
    ├── WebSocketEventListener.java
    └── UserSessionRegistry.java
```

---

## 1. Dependencies — `pom.xml`

```xml
<dependencies>

    <!-- Spring Boot core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- WebSocket + STOMP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- Spring Security (JWT auth assumed) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JPA + PostgreSQL -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- JWT minting for Jitsi tokens -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Scheduling (meeting reminders) -->
    <!-- Built into Spring Boot — no extra dep needed -->

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

</dependencies>
```

---

## 2. Configuration — `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/yourapp_db
    username: youruser
    password: yourpassword
  jpa:
    hibernate:
      ddl-auto: update        # use 'validate' in production
    show-sql: false

jitsi:
  domain: meet.yourdomain.com   # your self-hosted Jitsi VPS domain
  app-id: your_jitsi_app_id     # from /etc/prosody/conf.d/your-domain.cfg.lua
  secret: your_jitsi_secret     # same file, keep this private

server:
  port: 8080
```

---

## 3. WebSocket Config — `WebSocketConfig.java`

```java
package com.yourapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix for messages FROM server TO client (subscriptions)
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages FROM client TO server (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        // Required for /user/queue/** (user-specific destinations)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS fallback for older clients
    }
}
```

---

## 4. User Session Registry — `UserSessionRegistry.java`

Tracks which users currently have an active WebSocket connection.
Used to decide whether to try sending a STOMP message or show "user unavailable".

```java
package com.yourapp.websocket;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionRegistry {

    // Thread-safe set of currently connected user IDs
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public void userConnected(String userId) {
        onlineUsers.add(userId);
    }

    public void userDisconnected(String userId) {
        onlineUsers.remove(userId);
    }

    public boolean isUserOnline(String userId) {
        return onlineUsers.contains(userId);
    }

    public Set<String> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
}
```

---

## 5. WebSocket Event Listener — `WebSocketEventListener.java`

Fires when users connect/disconnect. Updates `UserSessionRegistry`.

```java
package com.yourapp.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserSessionRegistry sessionRegistry;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserId(accessor);
        if (userId != null) {
            sessionRegistry.userConnected(userId);
            log.info("User connected via WS: {}", userId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = getUserId(accessor);
        if (userId != null) {
            sessionRegistry.userDisconnected(userId);
            log.info("User disconnected from WS: {}", userId);
        }
    }

    private String getUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() != null) {
            return accessor.getUser().getName(); // Principal name = userId from JWT
        }
        return null;
    }
}
```

---

## 6. Jitsi Token Service — `RoomTokenService.java`

Mints short-lived JWTs that allow a user to join a specific Jitsi room.
**Never expose `jitsi.secret` to the Flutter app.**

```java
package com.yourapp.service;

import com.yourapp.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomTokenService {

    @Value("${jitsi.app-id}") private String appId;
    @Value("${jitsi.secret}")  private String secret;
    @Value("${jitsi.domain}")  private String domain;

    /**
     * Mint a Jitsi JWT for a specific user + room.
     *
     * @param user        the user who will join (null = anonymous/guest)
     * @param roomName    the Jitsi room name
     * @param isModerator true = admin/host, false = regular participant
     */
    public String mintToken(User user, String roomName, boolean isModerator) {

        Map<String, Object> userClaims = new HashMap<>();
        if (user != null) {
            userClaims.put("id",     user.getId().toString());
            userClaims.put("name",   user.getDisplayName());
            userClaims.put("email",  user.getEmail());
            userClaims.put("avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
        }

        Map<String, Object> featureFlags = Map.of(
            "livestreaming", false,
            "recording",     false,
            "outbound-call", false
        );

        Map<String, Object> context = new HashMap<>();
        context.put("user",     userClaims);
        context.put("features", featureFlags);

        return Jwts.builder()
                .header().add("kid", appId + "/default-key").and()
                .issuer(appId)
                .subject("*")
                .audience().add(domain).and()
                .claim("room",      roomName)
                .claim("context",   context)
                .claim("moderator", isModerator)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000)) // 1 hour
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }
}
```

---

## 7. Room Model — `Room.java`

```java
package com.yourapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;               // Jitsi room name (UUID-based, unguessable)

    @Column(nullable = false)
    private String title;              // Human-readable title ("Team Standup")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;             // PUBLIC | PRIVATE | SCHEDULED

    @Column(name = "created_by", nullable = false)
    private String createdById;        // FK → users.id

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt; // null for instant rooms

    @Column(name = "reminder_sent")
    @Builder.Default
    private boolean reminderSent = false;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;    // null = still active

    public enum RoomType {
        PUBLIC,     // Admin-created, visible to all users
        PRIVATE,    // 1-to-1 or group, invite only
        SCHEDULED   // Public, has a future scheduledAt time
    }
}
```

---

## 8. Call Session Model — `CallSession.java`

Tracks active 1-to-1 or group calls. Used to detect duplicate calls and show call history.

```java
package com.yourapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;           // Jitsi room name

    @Column(name = "caller_id", nullable = false)
    private String callerId;           // who initiated

    @Column(name = "target_id", nullable = false)
    private String targetId;           // who was called

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CallStatus status = CallStatus.RINGING;

    @Column(name = "started_at")
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;  // null until accepted

    @Column(name = "ended_at")
    private LocalDateTime endedAt;     // null while active

    public enum CallStatus {
        RINGING,   // Invite sent, waiting for answer
        ACTIVE,    // Both parties joined
        REJECTED,  // Target declined
        MISSED,    // Timed out (30s) without answer
        ENDED      // Call finished normally
    }
}
```

---

## 9. Repositories

```java
// RoomRepository.java
package com.yourapp.repository;

import com.yourapp.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    Optional<Room> findByName(String name);

    // All public rooms (for users to browse)
    List<Room> findByTypeOrderByCreatedAtDesc(Room.RoomType type);

    // Upcoming scheduled rooms not yet reminded
    List<Room> findByTypeAndScheduledAtBetweenAndReminderSentFalse(
            Room.RoomType type,
            LocalDateTime from,
            LocalDateTime to
    );

    // Active rooms (not yet closed)
    List<Room> findByClosedAtIsNullOrderByCreatedAtDesc();
}
```

```java
// CallSessionRepository.java
package com.yourapp.repository;

import com.yourapp.model.CallSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, String> {

    Optional<CallSession> findByRoomName(String roomName);

    // Call history for a user (as caller or target)
    List<CallSession> findByCallerIdOrTargetIdOrderByStartedAtDesc(
            String callerId, String targetId
    );

    // Check if there's already an active call between two users
    Optional<CallSession> findByCallerIdAndTargetIdAndStatus(
            String callerId, String targetId, CallSession.CallStatus status
    );
}
```

---

## 10. DTOs

```java
// CreateRoomRequest.java
package com.yourapp.dto;

import com.yourapp.model.Room;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateRoomRequest {
    private String title;
    private Room.RoomType type;          // PUBLIC | PRIVATE | SCHEDULED
    private LocalDateTime scheduledAt;   // required if type = SCHEDULED
}
```

```java
// JoinResponse.java
package com.yourapp.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class JoinResponse {
    private String roomName;
    private String token;       // Jitsi JWT — Flutter passes this to SDK
    private String jitsiUrl;    // full URL: https://meet.yourdomain.com/roomName
}
```

```java
// CallResponse.java
package com.yourapp.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class CallResponse {
    private String roomName;
    private String token;       // caller's Jitsi JWT
    private boolean targetOnline; // false = target app is closed
}
```

```java
// CallInvitePayload.java  — sent via STOMP to the target user
package com.yourapp.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallInvitePayload {
    private String roomName;
    private String callerId;
    private String callerName;
    private String callerAvatar;
    private String token;         // target's Jitsi JWT
}
```

---

## 11. Room Service — `RoomService.java`

```java
package com.yourapp.service;

import com.yourapp.dto.*;
import com.yourapp.model.*;
import com.yourapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository    roomRepository;
    private final RoomTokenService  tokenService;

    @Value("${jitsi.domain}") private String jitsiDomain;

    // Admin creates a public or scheduled room
    public JoinResponse createRoom(CreateRoomRequest req, User creator) {
        String roomName = UUID.randomUUID().toString(); // unguessable

        Room room = Room.builder()
                .name(roomName)
                .title(req.getTitle())
                .type(req.getType())
                .createdById(creator.getId())
                .scheduledAt(req.getScheduledAt())
                .build();

        roomRepository.save(room);

        String token = tokenService.mintToken(creator, roomName, true); // admin = moderator
        return new JoinResponse(roomName, token, "https://" + jitsiDomain + "/" + roomName);
    }

    // Any user joins an existing public/scheduled room
    public JoinResponse joinRoom(String roomName, User user) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomName));

        String token = tokenService.mintToken(user, roomName, false);
        return new JoinResponse(roomName, token, "https://" + jitsiDomain + "/" + roomName);
    }

    // List all public rooms (active + upcoming scheduled)
    public List<Room> getPublicRooms() {
        return roomRepository.findByClosedAtIsNullOrderByCreatedAtDesc();
    }

    // Close/end a room (admin only)
    public void closeRoom(String roomName) {
        Room room = roomRepository.findByName(roomName)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setClosedAt(java.time.LocalDateTime.now());
        roomRepository.save(room);
    }
}
```

---

## 12. Call Service — `CallService.java`

Core logic: creates a private room, sends STOMP invite to target, handles reject/cancel.

```java
package com.yourapp.service;

import com.yourapp.dto.*;
import com.yourapp.model.*;
import com.yourapp.repository.*;
import com.yourapp.websocket.UserSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomTokenService      tokenService;
    private final UserSessionRegistry   sessionRegistry;
    private final CallSessionRepository callSessionRepository;
    private final UserRepository        userRepository;

    @Value("${jitsi.domain}") private String jitsiDomain;

    /**
     * User A calls User B.
     * Returns room info to caller immediately.
     * Sends STOMP message to target if they are online.
     */
    public CallResponse initiateCall(String callerId, String targetUserId) {
        User caller = userRepository.findById(callerId).orElseThrow();
        User target = userRepository.findById(targetUserId).orElseThrow();

        // Prevent duplicate active calls
        callSessionRepository
            .findByCallerIdAndTargetIdAndStatus(callerId, targetUserId, CallSession.CallStatus.RINGING)
            .ifPresent(s -> { throw new RuntimeException("Call already in progress"); });

        String roomName    = "call-" + UUID.randomUUID();
        String callerToken = tokenService.mintToken(caller, roomName, false);
        String targetToken = tokenService.mintToken(target, roomName, false);

        // Persist the call session
        CallSession session = CallSession.builder()
                .roomName(roomName)
                .callerId(callerId)
                .targetId(targetUserId)
                .status(CallSession.CallStatus.RINGING)
                .build();
        callSessionRepository.save(session);

        boolean targetOnline = sessionRegistry.isUserOnline(targetUserId);

        if (targetOnline) {
            // Send call invite via STOMP — target's Flutter app shows ringing screen
            CallInvitePayload payload = CallInvitePayload.builder()
                    .roomName(roomName)
                    .callerId(callerId)
                    .callerName(caller.getDisplayName())
                    .callerAvatar(caller.getAvatarUrl())
                    .token(targetToken)
                    .build();

            messagingTemplate.convertAndSendToUser(
                targetUserId,
                "/queue/call-invite",
                payload
            );
            log.info("Call invite sent via STOMP: {} → {}", callerId, targetUserId);
        } else {
            // Target is offline — mark as missed immediately
            session.setStatus(CallSession.CallStatus.MISSED);
            callSessionRepository.save(session);
            log.info("Call missed — target offline: {}", targetUserId);
        }

        return new CallResponse(roomName, callerToken, targetOnline);
    }

    /**
     * Target user accepted the call (optional — just joining Jitsi is enough,
     * but this lets you show "connected" UI to the caller).
     */
    public void acceptCall(String roomName, String targetId) {
        callSessionRepository.findByRoomName(roomName).ifPresent(session -> {
            session.setStatus(CallSession.CallStatus.ACTIVE);
            session.setAnsweredAt(java.time.LocalDateTime.now());
            callSessionRepository.save(session);

            // Notify caller that call was accepted
            messagingTemplate.convertAndSendToUser(
                session.getCallerId(),
                "/queue/call-accepted",
                Map.of("room_name", roomName)
            );
        });
    }

    /**
     * Target user declined the call.
     */
    public void rejectCall(String roomName, String targetId) {
        callSessionRepository.findByRoomName(roomName).ifPresent(session -> {
            session.setStatus(CallSession.CallStatus.REJECTED);
            session.setEndedAt(java.time.LocalDateTime.now());
            callSessionRepository.save(session);

            // Notify caller that call was rejected
            messagingTemplate.convertAndSendToUser(
                session.getCallerId(),
                "/queue/call-rejected",
                Map.of("room_name", roomName)
            );
        });
    }

    /**
     * Caller hung up before target answered.
     */
    public void cancelCall(String roomName, String callerId) {
        callSessionRepository.findByRoomName(roomName).ifPresent(session -> {
            session.setStatus(CallSession.CallStatus.MISSED);
            session.setEndedAt(java.time.LocalDateTime.now());
            callSessionRepository.save(session);

            // Notify target to dismiss the ringing screen
            messagingTemplate.convertAndSendToUser(
                session.getTargetId(),
                "/queue/call-cancelled",
                Map.of("room_name", roomName)
            );
        });
    }

    /**
     * Call ended normally (both parties finished).
     */
    public void endCall(String roomName) {
        callSessionRepository.findByRoomName(roomName).ifPresent(session -> {
            session.setStatus(CallSession.CallStatus.ENDED);
            session.setEndedAt(java.time.LocalDateTime.now());
            callSessionRepository.save(session);
        });
    }
}
```

---

## 13. Room Controller — `RoomController.java`

```java
package com.yourapp.controller;

import com.yourapp.dto.*;
import com.yourapp.model.*;
import com.yourapp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService    roomService;
    private final UserRepository userRepository;

    // Admin creates a public or scheduled room
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JoinResponse> createRoom(
            @RequestBody CreateRoomRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        User creator = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(roomService.createRoom(req, creator));
    }

    // Any authenticated user joins a room
    @PostMapping("/{roomName}/join")
    public ResponseEntity<JoinResponse> joinRoom(
            @PathVariable String roomName,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(roomService.joinRoom(roomName, user));
    }

    // List all public/active rooms
    @GetMapping("/public")
    public ResponseEntity<List<Room>> getPublicRooms() {
        return ResponseEntity.ok(roomService.getPublicRooms());
    }

    // Admin closes a room
    @DeleteMapping("/{roomName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> closeRoom(@PathVariable String roomName) {
        roomService.closeRoom(roomName);
        return ResponseEntity.ok().build();
    }
}
```

---

## 14. Call Controller — `CallController.java`

```java
package com.yourapp.controller;

import com.yourapp.dto.CallResponse;
import com.yourapp.service.CallService;
import com.yourapp.repository.UserRepository;
import com.yourapp.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallController {

    private final CallService    callService;
    private final UserRepository userRepository;

    // Initiate a call to another user
    @PostMapping("/invite")
    public ResponseEntity<CallResponse> inviteUser(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User caller = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        String targetUserId = body.get("target_user_id");
        return ResponseEntity.ok(callService.initiateCall(caller.getId(), targetUserId));
    }

    // Target accepts the call
    @PostMapping("/accept")
    public ResponseEntity<Void> acceptCall(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User target = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        callService.acceptCall(body.get("room_name"), target.getId());
        return ResponseEntity.ok().build();
    }

    // Target rejects the call
    @PostMapping("/reject")
    public ResponseEntity<Void> rejectCall(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User target = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        callService.rejectCall(body.get("room_name"), target.getId());
        return ResponseEntity.ok().build();
    }

    // Caller cancels before target answers
    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelCall(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        User caller = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        callService.cancelCall(body.get("room_name"), caller.getId());
        return ResponseEntity.ok().build();
    }

    // Either party ends the call
    @PostMapping("/end")
    public ResponseEntity<Void> endCall(@RequestBody Map<String, String> body) {
        callService.endCall(body.get("room_name"));
        return ResponseEntity.ok().build();
    }
}
```

---

## 15. Meeting Reminder Job — `MeetingReminderJob.java`

Runs every minute. Sends STOMP broadcast to all online users 5 minutes before a scheduled meeting.

```java
package com.yourapp.service;

import com.yourapp.model.Room;
import com.yourapp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingReminderJob {

    private final RoomRepository       roomRepository;
    private final RoomTokenService     tokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60_000) // every 60 seconds
    public void sendReminders() {
        LocalDateTime now          = LocalDateTime.now();
        LocalDateTime fiveMinAhead = now.plusMinutes(5);
        LocalDateTime sixMinAhead  = now.plusMinutes(6);

        List<Room> upcoming = roomRepository
            .findByTypeAndScheduledAtBetweenAndReminderSentFalse(
                Room.RoomType.SCHEDULED, fiveMinAhead, sixMinAhead
            );

        for (Room room : upcoming) {
            // Mint a guest token (no specific user — Flutter will swap it after join)
            String token = tokenService.mintToken(null, room.getName(), false);

            // Broadcast to all subscribed clients on /topic/meeting-reminder
            messagingTemplate.convertAndSend(
                "/topic/meeting-reminder",
                Map.of(
                    "room_name",   room.getName(),
                    "title",       room.getTitle(),
                    "token",       token,
                    "starts_at",   room.getScheduledAt().toString()
                )
            );

            room.setReminderSent(true);
            roomRepository.save(room);
            log.info("Reminder sent for room: {}", room.getTitle());
        }
    }
}
```

Enable scheduling in your main class:

```java
@SpringBootApplication
@EnableScheduling   // ← add this
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 16. STOMP Destinations Reference

| Direction | Destination | Purpose |
|---|---|---|
| Server → User | `/user/{id}/queue/call-invite` | Incoming call notification |
| Server → User | `/user/{id}/queue/call-accepted` | Target accepted your call |
| Server → User | `/user/{id}/queue/call-rejected` | Target declined your call |
| Server → User | `/user/{id}/queue/call-cancelled` | Caller cancelled before you answered |
| Server → All  | `/topic/meeting-reminder` | 5-min reminder for scheduled rooms |

Flutter subscribes with:
```dart
stompClient.subscribe(destination: '/user/queue/call-invite', callback: ...);
stompClient.subscribe(destination: '/topic/meeting-reminder', callback: ...);
```
Spring routes `/user/queue/**` automatically to the correct user session.

---

## 17. Database Schema (PostgreSQL)

```sql
-- ─── Users (assumed to exist already) ──────────────────────────────────────
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    avatar_url   TEXT,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',   -- USER | ADMIN
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ─── Rooms ──────────────────────────────────────────────────────────────────
CREATE TABLE rooms (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL UNIQUE,   -- Jitsi room name (UUID slug)
    title        VARCHAR(255) NOT NULL,           -- human-readable
    type         VARCHAR(20)  NOT NULL,           -- PUBLIC | PRIVATE | SCHEDULED
    created_by   UUID         NOT NULL REFERENCES users(id),
    scheduled_at TIMESTAMP,                       -- NULL for instant rooms
    reminder_sent BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    closed_at    TIMESTAMP                        -- NULL = still active
);

CREATE INDEX idx_rooms_type       ON rooms(type);
CREATE INDEX idx_rooms_closed_at  ON rooms(closed_at);
CREATE INDEX idx_rooms_scheduled  ON rooms(scheduled_at) WHERE closed_at IS NULL;

-- ─── Call Sessions ──────────────────────────────────────────────────────────
CREATE TABLE call_sessions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_name   VARCHAR(255) NOT NULL UNIQUE,   -- Jitsi room name
    caller_id   UUID NOT NULL REFERENCES users(id),
    target_id   UUID NOT NULL REFERENCES users(id),
    status      VARCHAR(20)  NOT NULL DEFAULT 'RINGING',
                -- RINGING | ACTIVE | REJECTED | MISSED | ENDED
    started_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    answered_at TIMESTAMP,     -- NULL until accepted
    ended_at    TIMESTAMP      -- NULL while active
);

CREATE INDEX idx_calls_caller   ON call_sessions(caller_id);
CREATE INDEX idx_calls_target   ON call_sessions(target_id);
CREATE INDEX idx_calls_status   ON call_sessions(status);
```

---

## What to Store in the DB — Summary

| Table | Why it's needed |
|---|---|
| `rooms` | List public rooms in the app, track scheduled meetings, send reminders, let admin close/delete rooms |
| `call_sessions` | Call history (missed/rejected/answered), prevent duplicate calls, duration tracking |
| `users` | Already exists — needed for JWT principal lookup and display name/avatar in call invite |

### What you do NOT need to store

| Thing | Why not |
|---|---|
| Jitsi JWT tokens | Short-lived, minted on demand, never persisted |
| WebSocket session IDs | Held in-memory by `UserSessionRegistry` — reset on restart is fine |
| Message history | Jitsi handles all media; you're not storing chat |
| Online/offline state | In-memory registry is sufficient; DB would be stale within seconds |

---

## Jitsi Server Setup (VPS — one time)

```bash
# 1. Install Jitsi on Ubuntu 22.04
apt update && apt install -y gnupg2 nginx-full
curl https://download.jitsi.org/jitsi-key.gpg.key | gpg --dearmor > /usr/share/keyrings/jitsi.gpg
echo "deb [signed-by=/usr/share/keyrings/jitsi.gpg] https://download.jitsi.org stable/" \
    > /etc/apt/sources.list.d/jitsi.list
apt update && apt install -y jitsi-meet

# 2. During install — enter your domain: meet.yourdomain.com
# 3. Choose "Let's Encrypt" for SSL certificate

# 4. Enable JWT authentication
# Edit /etc/prosody/conf.d/meet.yourdomain.com.cfg.lua
# Set:  authentication = "token"
#       app_id = "your_jitsi_app_id"
#       app_secret = "your_jitsi_secret"
#       allow_empty_token = false

# 5. Edit /etc/jitsi/meet/meet.yourdomain.com-config.js
# Set:  tokenAuthUrl not needed (server-side JWT only)

# 6. Restart services
systemctl restart prosody jicofo jitsi-videobridge2
```