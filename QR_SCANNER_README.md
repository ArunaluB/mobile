# QR Code Scanning Feature - RechangeMe App

## 📱 Overview

The QR Scanner feature allows operators to scan booking QR codes, view detailed reservation information, and manage booking statuses through an intuitive and attractive Material Design UI.

## ✨ Features

### 1. **QR Code Scanning**
- Camera-based QR code scanning using ZXing library
- Automatic permission handling for camera access
- User-friendly scanning interface with beep feedback

### 2. **Booking Details Display**
- **Reservation ID**: Unique booking identifier
- **Station ID**: Charging station identifier
- **Status Badge**: Color-coded status indicator (Approved, In-Progress, Completed, Cancelled)
- **Time Slot Highlight**: Prominent display of booking time window
- **Date Display**: Formatted date in readable format
- **Start & End Times**: Detailed UTC timestamps converted to local time
- **Owner NIC**: Optional owner identification (if available)

### 3. **Status Management**
The app supports multiple booking statuses with visual indicators:

- 🟢 **Approved** - Booking is confirmed and ready
- 🟠 **In-Progress** - Charging session is active
- 🔵 **Completed** - Successfully completed session
- 🔴 **Cancelled** - Booking was cancelled
- ⚪ **Pending** - Awaiting confirmation

### 4. **Interactive Actions**

#### Next Actions (Dynamic based on API response)
- **Confirm Start** - Start the charging session
- **Confirm End** - End the charging session

#### Session Management
- **Complete** - Mark booking as completed with success animation
- **Cancel** - Cancel the current booking
- **Scan Another** - Reset to scan a new QR code

### 5. **History Tracking**
- All scanned bookings are tracked
- View completed and cancelled bookings in History tab
- Complete booking details preserved for reference

## 🔧 Technical Implementation

### API Integration

#### **Scan QR Code Endpoint**
```http
POST http://localhost:7989/api/Operator/scan
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsImtpZCI6InFyLXYxIn0.eyJyaWQiOiJSRVMtMDAxIiwic2lkIjoiU1ROLTA0NSIsInYiOiIxIiwiZXhwIjoxNzM5MzM0MDAwLCJuYmYiOjE3MzkzMzMxMDB9.sig"
}
```

**Response:**
```json
{
  "reservationId": "RES-001",
  "stationId": "STN-045",
  "status": "Approved",
  "startTimeUtc": "2025-10-12T12:30:00Z",
  "endTimeUtc": "2025-10-12T13:30:00Z",
  "ownerNic": "1999XXXXXXXX",
  "nextActions": ["confirm-start"]
}
```

#### **Confirm Action Endpoint**
```http
POST http://localhost:7989/api/Operator/scan/confirm
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsImtpZCI6InFyLXYxIn0...",
  "action": "confirm-start"
}
```

### Architecture

```
┌─────────────────────────────────────────┐
│          ScanFragment (UI)              │
│  - QR Scanner Integration               │
│  - Booking Details Display              │
│  - Action Buttons                       │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         ScanViewModel                   │
│  - LiveData Management                  │
│  - Loading States                       │
│  - Result Handling                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│       BookingRepository                 │
│  - API Call Abstraction                 │
│  - Error Handling                       │
│  - Result Wrapping                      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│         RetrofitClient                  │
│  - HTTP Client Configuration            │
│  - JSON Parsing                         │
│  - Logging Interceptor                  │
└─────────────────────────────────────────┘
```

## 🎨 UI Components

### Material Design Elements
- **MaterialCardView**: Elevated cards with rounded corners
- **MaterialButton**: Filled buttons with icons
- **BottomNavigationView**: Tab navigation
- **LottieAnimation**: Success checkmark animation
- **Status Badges**: Color-coded status indicators

### Color Scheme
```kotlin
Status Colors:
- Approved:    #4CAF50 (Green)
- In-Progress: #FF9800 (Orange)
- Completed:   #2196F3 (Blue)
- Cancelled:   #F44336 (Red)
- Pending:     #9E9E9E (Gray)
```

## 📦 Dependencies Added

```gradle
// QR Code Scanner
implementation("com.journeyapps:zxing-android-embedded:4.3.0")
implementation("com.google.zxing:core:3.5.2")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// JSON parsing
implementation("com.google.code.gson:gson:2.10.1")

// ViewModel and LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// Lottie Animation
implementation("com.airbnb.android:lottie:6.2.0")
```

## 🔐 Permissions Required

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

## 🚀 Usage Flow

1. **Launch App** → Navigate to Scan tab
2. **Tap "Scan QR Code"** → Camera permission is requested (first time)
3. **Scan QR Code** → Point camera at booking QR code
4. **View Details** → Booking information is displayed with status
5. **Take Action** → Confirm Start/End based on nextActions
6. **Manage Session** → Complete or Cancel the booking
7. **Scan Another** → Reset to scan next booking

## 📝 Example JWT Token Structure

```json
Header:
{
  "alg": "HS256",
  "kid": "qr-v1"
}

Payload:
{
  "rid": "RES-001",     // Reservation ID
  "sid": "STN-045",     // Station ID
  "v": "1",             // Version
  "exp": 1739334000,    // Expiration timestamp
  "nbf": 1739333100     // Not before timestamp
}
```

## 🔄 Status Workflow

```
Approved → [Confirm Start] → In-Progress → [Complete/Cancel] → Completed/Cancelled
           [Confirm End]
```

## 🎯 Future Enhancements

1. **Offline Mode**: Cache scanned bookings for offline viewing
2. **Push Notifications**: Alert operators about upcoming bookings
3. **Analytics**: Track scan success rates and session durations
4. **QR Code Generation**: Generate QR codes for new bookings
5. **Multi-language Support**: Internationalization
6. **Dark Mode**: Theme support
7. **Export History**: PDF/CSV export of scan history

## 🐛 Troubleshooting

### Camera not working
- Ensure camera permission is granted in app settings
- Check if device has a working camera

### Network errors
- Verify API endpoint URL in `RetrofitClient.kt`
- Check internet connectivity
- Ensure backend server is running on `localhost:7989`

### QR Code not scanning
- Ensure good lighting conditions
- Hold device steady while scanning
- Verify QR code is valid JWT format

## 📄 License

This feature is part of the RechangeMe application.

---

**Developed with ❤️ using Kotlin, Material Design, and modern Android development practices**
