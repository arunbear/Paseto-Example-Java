# Paseto Example Java

A runnable example demonstrating how to use the [paseto4j](https://github.com/nbaars/paseto4j) library for secure token generation and validation in a Spring Boot application.

## What is PASETO?

**PASETO (Platform-Agnostic Security Tokens)** is a modern token format that provides a secure alternative to JWT with several key advantages:

- **Cryptographic agility**: Built-in security best practices without requiring developer choices
- **Simplicity**: Fewer dangerous options mean fewer security pitfalls
- **Version-based updates**: Easy security upgrades across applications
- **Authenticated encryption**: All tokens are encrypted and authenticated by default

This example uses **PASETO Version 4**, which provides symmetric-key authenticated encryption using `XChaCha20-Poly1305`.

## Getting Started

### Prerequisites

- Java 16 or higher
- Maven 3.6 or higher

### Project Setup

Clone this repository and navigate to the project directory:

```bash
git clone https://github.com/arunbear/Paseto-Example-Java.git
cd Paseto-Example-Java
```

Build the project:

```bash
./mvnw clean install
```

## Understanding the Example

This project demonstrates a complete workflow for creating, encrypting, and decrypting PASETO tokens. Let's walk through each component.

### Step 1: Define Your Token Payload

The first step is to define what data you want to store in your token. In this example, we use the [`AppToken`](src/main/java/org/example/AppToken.java) record:

```java
@Builder
public record AppToken(String userId, String role, Instant expiresAt) {
    public AppToken {
        if (expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Cannot create an expired token");
        }
    }
}
```

**Key points:**
- Uses a Java `record` for a clean, immutable data structure
- Includes a compact constructor for validation (ensures tokens can't be created in an expired state)
- Contains user identification, role/permissions, and expiration time

### Step 2: Create a Token Service

The [`TokenService`](src/main/java/org/example/TokenService.java) handles encryption and decryption:

```java
@Service
public class TokenService {
    
    @Value("${app.token.secret:#{null}}")
    String secret = "";
    
    @Value("${app.token.footer:#{null}}")
    String footer = "";
    
    public Try<String> encrypt(AppToken token) {
        return Try.of(() -> {
            String payload = mapper().writeValueAsString(token);
            return Paseto.encrypt(key(), payload, footer);
        });
    }
    
    public Try<AppToken> decrypt(String token) {
        return Try.of(() -> {
            String payload = Paseto.decrypt(key(), token, footer);
            return mapper().readValue(payload, AppToken.class);
        });
    }
}
```

**Key features:**
- Configurable secret key and optional footer from environment variables
- Uses the `Try` monad (from Vavr) for elegant error handling
- Automatically converts tokens to/from JSON using Jackson
- Includes `JavaTimeModule` to handle Java 8+ time types

### Step 3: Configure Your Application

Create an `application.properties` file or set environment variables:

```properties
app.token.secret=your-secure-secret-key-here-minimum-32-characters
app.token.footer=optional-metadata-or-context
```

The secret key should be at least 32 characters long for security.

### Step 4: Use the Service

The [`PasetoExampleApplication`](src/main/java/org/example/PasetoExampleApplication.java) is a Spring Boot application that initializes everything:

```java
@SpringBootApplication
public class PasetoExampleApplication {
    static void main(String[] args) {
        SpringApplication.run(PasetoExampleApplication.class, args);
    }
}
```

## Practical Workflow

Here's a complete example of creating, encrypting, and decrypting tokens:

```java
// Inject the TokenService (Spring will handle this)
@Autowired
TokenService tokenService;

// 1. Create a token
AppToken appToken = AppToken.builder()
    .userId("user-12345")
    .role("ADMIN")
    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
    .build();

// 2. Encrypt the token
Try<String> encrypted = tokenService.encrypt(appToken);
if (encrypted.isSuccess()) {
    String pastetoToken = encrypted.get();
    System.out.println("Token: " + pastetoToken);
} else {
    System.out.println("Error: " + encrypted.getCause().getMessage());
}

// 3. Later, decrypt and validate the token
Try<AppToken> decrypted = tokenService.decrypt(pastetoToken);
if (decrypted.isSuccess()) {
    AppToken decoded = decrypted.get();
    System.out.println("User ID: " + decoded.userId());
    System.out.println("Role: " + decoded.role());
} else {
    System.out.println("Invalid token: " + decrypted.getCause().getMessage());
}
```

## Testing the Implementation

See the [`TokenServiceTest`](src/test/java/org/example/TokenServiceTest.java) file for comprehensive examples:

```java
@Test
void a_good_token_can_be_encrypted_and_decrypted() {
    // Create a valid token
    AppToken appToken = AppToken.builder()
        .userId("1234")
        .role("USER")
        .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
        .build();
    
    // Encrypt it
    Try<String> encrypted = tokenService.encrypt(appToken);
    then(encrypted.isSuccess()).isTrue();
    
    // Decrypt and verify
    Try<AppToken> decrypted = tokenService.decrypt(encrypted.get());
    then(decrypted.isSuccess()).isTrue();
    then(decrypted.get().userId()).isEqualTo("1234");
}
```

**Run the tests:**

```bash
./mvnw test
```

The tests demonstrate:
- ✅ Valid tokens can be encrypted and decrypted successfully
- ✅ Invalid or tampered tokens are rejected
- ✅ Token data is preserved through encryption/decryption

## Key Technologies Used

| Technology | Purpose |
|-----------|---------|
| **Spring Boot** | Web framework and dependency injection |
| **paseto4j** | PASETO implementation |
| **Lombok** | Reduces boilerplate with annotations |
| **Jackson** | JSON serialization with JSR310 support for Java Time |
| **Vavr** | Functional error handling with `Try` |
| **Bouncy Castle** | Cryptography provider |
| **Error Prone** | Static analysis to catch bugs |
| **NullAway** | Null pointer prevention |

## Running the Application

```bash
# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Run tests
./mvnw test
```

## Resources

- [PASETO Official Documentation](https://paseto.io/)
- [paseto4j GitHub Repository](https://github.com/nbaars/paseto4j)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PASETO RFC](https://github.com/paseto-standard/paseto-spec)
- [Spring Boot Quick Guide to Replace JWT with PASETO](https://nutbutterfly.medium.com/spring-boot-quick-guide-to-replace-jwt-with-paseto-774f43c8f2c4) - The article that inspired this project

## License

This project is licensed under the GPL License. See the LICENSE file for details.
