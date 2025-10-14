# Login API Implementation Guide

## Overview
This document describes the complete implementation of the login system with API integration, local storage, and beautiful profile display.

## Features Implemented

### 1. API Integration
- **Endpoint**: `https://localhost:7170/api/Auth/login`
- **Method**: POST
- **Request Body**:
```json
{
  "username": "Arunalu",
  "password": "arunalu@123"
}
```

### 2. Response Handling
The API returns comprehensive user data including:
- Authentication token (JWT)
- User role (e.g., StationOperator)
- User ID
- Username
- Complete user profile data
- Station information with slots

### 3. Local Storage (SharedPreferences)
All login response data is saved locally using SharedPreferences:
- Token for API authentication
- User profile information
- Station details
- Login status flag

**Location**: `UserPreferences.kt`
- `saveLoginResponse()` - Saves the entire login response
- `getLoginResponse()` - Retrieves saved data
- `getToken()` - Gets authentication token
- `isLoggedIn()` - Checks login status
- `clearUserData()` - Clears all saved data on logout

### 4. Beautiful Material UI Profile

#### Design Features:
1. **Gradient Header Card**
   - Purple gradient background
   - Circular avatar with first letter of username
   - Username display
   - Role badge

2. **User Information Card**
   - Email
   - Phone number
   - User ID (shortened for display)

3. **Station Information Card**
   - Station name
   - Station type (DC/AC)
   - GPS coordinates
   - Total number of slots

4. **Logout Button**
   - Material Design button
   - Confirmation dialog
   - Clears all saved data

#### Animations:
- Smooth fade-in animations for all cards
- Staggered animation delays for visual appeal

### 5. Auto-Login on App Start
The app checks if a user is already logged in:
- If logged in → Navigate to MainActivity
- If not logged in → Show LoginActivity

## File Structure

```
app/src/main/java/edu/sliit/myapplication/
├── models/
│   ├── LoginRequest.kt          # Login request data model
│   └── LoginResponse.kt         # Login response data models
├── api/
│   ├── AuthApi.kt               # Retrofit API interface
│   └── RetrofitClient.kt        # Retrofit configuration with SSL
├── utils/
│   └── UserPreferences.kt       # SharedPreferences manager
├── LoginActivity.kt             # Login screen with API call
├── ProfileFragment.kt           # Profile display with user data
└── SplashActivity.kt            # Auto-login check
```

## Network Configuration

### SSL/HTTPS Support
The app is configured to work with localhost HTTPS:
- Self-signed certificate support
- Trust all certificates (development only)
- Base URL: `https://10.0.2.2:7170/` (Android emulator)

**Note**: For physical devices, use your computer's local IP address instead of `10.0.2.2`.

### Permissions Required
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Usage Flow

### Login Flow:
1. User enters credentials
2. App sends POST request to login API
3. On success:
   - Save complete response to SharedPreferences
   - Show success message
   - Navigate to MainActivity
4. On failure:
   - Show error message
   - Keep user on login screen

### Profile Display:
1. Fragment loads
2. Retrieve saved login data from SharedPreferences
3. Display all user information in beautiful cards
4. Show station details
5. Enable logout functionality

### Logout Flow:
1. User clicks logout button
2. Confirmation dialog appears
3. On confirmation:
   - Clear all SharedPreferences data
   - Navigate to LoginActivity
   - Clear activity stack

## Testing

### Test Credentials:
- **Username**: Arunalu
- **Password**: arunalu@123

### Expected Response:
- Token (JWT)
- Role: StationOperator
- Station: Malabe
- 4 active slots

## API Configuration

### Change API URL:
Edit `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://YOUR_IP:7170/"
```

### For Physical Device:
Replace `10.0.2.2` with your computer's local IP address:
```kotlin
private const val BASE_URL = "https://192.168.1.XXX:7170/"
```

## Dependencies Used
- Retrofit 2.9.0 - HTTP client
- Gson - JSON serialization
- OkHttp - Logging interceptor
- Material Components - UI design
- Kotlin Coroutines - Async operations

## Security Notes

⚠️ **Development Mode**:
- Current SSL configuration trusts all certificates
- Suitable for development/testing only
- **DO NOT** use in production

For production:
- Use proper SSL certificates
- Remove trust-all certificate code
- Implement proper certificate pinning

## UI Customization

### Colors:
The profile uses a beautiful purple gradient theme:
- Start: `#667eea`
- Middle: `#764ba2`
- End: `#f093fb`

### Modify in:
- `drawable/gradient_header_background.xml`
- `drawable/circle_background.xml`

## Troubleshooting

### Connection Issues:
1. Check if backend API is running
2. Verify URL is correct for emulator/device
3. Check network permissions
4. Enable HTTPS in network_security_config.xml

### Data Not Displaying:
1. Check if login was successful
2. Verify SharedPreferences contains data
3. Check LogCat for errors

### SSL Errors:
1. Ensure network_security_config.xml is referenced in manifest
2. Check trust anchors configuration
3. Verify OkHttp client SSL configuration

## Future Enhancements
- Token refresh mechanism
- Encrypted local storage
- Biometric authentication
- Profile edit functionality
- Avatar image upload
- Session timeout handling
