# PeerTayo Mobile - Simple & Clean

A simple Android app for PeerTayo using Kotlin + MVVM.

## Structure

```
app/src/main/java/com/example/peertayo_mobile/
├── PeerTayoApplication.kt      # Hilt app
├── MainActivity.kt              # Home screen
├── api/
│   ├── ApiService.kt           # Retrofit API endpoints  
│   ├── AuthRequest.kt          # Login/Register requests
│   └── AuthResponse.kt         # API responses
├── auth/
│   ├── LoginActivity.kt        # Login screen
│   ├── LoginViewModel.kt       # Login logic
│   ├── RegisterActivity.kt     # Register screen
│   └── RegisterViewModel.kt    # Register logic
├── di/
│   └── AppModule.kt            # Hilt DI setup
└── utils/
    ├── Constants.kt            # API URL
    └── TokenManager.kt         # Token storage
```

## Quick Start

1. **Start Backend**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **Update API URL** (if needed)
   - For emulator: Already set to `http://10.0.2.2:8080/`
   - For physical device: Edit `Constants.kt`, use your computer's IP

3. **Build & Run**
   - Open `mobile/` in Android Studio
   - Click Run

## Features

- ✅ Login
- ✅ Register
- ✅ JWT Token storage
- ✅ Simple validation
- ✅ Auto-login

## Testing

1. Register: John Doe, john@test.com, password123
2. Login with same credentials
3. Logout from main screen

That's it! Simple and working.
