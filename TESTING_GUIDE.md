# 🧪 Testing Guide for Home Page & Quick Booking

## 📋 Prerequisites

1. **Backend Server**: Must be running on `http://localhost:7989/`
2. **Endpoint**: `POST /booking/create`
3. **Android Device/Emulator**: With the app installed

## 🚀 Testing Steps

### Phase 1: UI & Animation Testing (No Backend Required)

#### Test 1: Home Page Load Animations
```
✅ Expected: All cards fade in sequentially
1. Open app
2. Navigate to Home tab
3. Observe:
   - Header appears first (green card)
   - Quick Booking appears second (orange card)
   - Stats cards appear third (blue & purple)
   - Recent Activity appears fourth
   - Feature icons appear last

Duration: ~1.4 seconds total
```

#### Test 2: Pulse Animation
```
✅ Expected: Quick Booking card continuously pulses
1. Stay on Home tab
2. Watch the orange Quick Booking card
3. Observe: Card scales up and down (1.0 → 1.05 → 1.0)
4. Repeats infinitely at 1-second intervals
```

#### Test 3: Quick Booking Dialog Opening
```
✅ Expected: Dialog opens with time slots
1. Tap "Quick Booking" orange card
2. Dialog appears with:
   - Title "⚡ Quick Booking"
   - Close button (✕)
   - List of 8 time slots
   - Cancel & Refresh buttons
```

#### Test 4: Slot Visual States
```
✅ Expected: Different visual states for slots
In the dialog, verify:

Active & Available (Full brightness):
- Station Alpha (SLT-001) ✅
- Station Beta (SLT-002) ✅
- Station Delta (SLT-004) ✅
- Station Zeta (SLT-006) ✅
- Station Eta (SLT-007) ✅

Active but Unavailable (Dimmed):
- Station Gamma (SLT-003) ⚠️
- Station Theta (SLT-008) ⚠️

Inactive (Dimmed, gray badge):
- Station Epsilon (SLT-005) ❌
```

#### Test 5: Slot Interaction
```
✅ Expected: Only active & available slots respond to taps
1. Tap on dimmed slots → Nothing happens
2. Tap on active/available slot → Confirmation dialog appears
3. Dialog shows: "Book [Station Name] from [Time] to [Time]?"
```

#### Test 6: Refresh Button
```
✅ Expected: Slots reload
1. In Quick Booking dialog
2. Tap "🔄 Refresh" button
3. Toast message: "🔄 Refreshing slots..."
4. Slots list refreshes (same demo data)
```

---

### Phase 2: API Integration Testing (Backend Required)

#### Backend Setup Requirements:

Your backend must handle this request:

```http
POST http://localhost:7989/booking/create
Content-Type: application/json

{
  "slotId": "SLT-001"
}
```

Expected response formats:

**Success Response:**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "bookingId": "BKG-12345",
  "slotDetails": {
    "slotId": "SLT-001",
    "stationName": "Station Alpha",
    "startTime": "08:00 AM",
    "endTime": "09:00 AM",
    "isActive": true
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Slot already booked",
  "bookingId": null,
  "slotDetails": null
}
```

#### Test 7: Successful Booking
```
✅ Expected: Booking created and confirmed
1. Start backend server on localhost:7989
2. Open app, go to Home tab
3. Tap Quick Booking
4. Select "Station Alpha (SLT-001)"
5. Confirm booking
6. Expected behavior:
   - Loading indicator appears briefly
   - Success toast: "✅ Booking created successfully! ID: BKG-12345"
   - Dialog closes automatically
```

#### Test 8: API Error Handling
```
✅ Expected: Error message displayed
1. Stop backend server OR
2. Make backend return error response
3. Attempt to create booking
4. Expected: Error toast with message
   Example: "❌ Booking failed: Connection failed"
```

#### Test 9: Network Timeout
```
✅ Expected: Timeout handled gracefully
1. Simulate slow network
2. Attempt booking
3. Wait 30 seconds (timeout period)
4. Expected: Error message displayed
```

---

## 🔍 Logcat Testing

Monitor logs during testing:

```bash
# View all app logs
adb logcat | grep "HomeFragment\|HomeViewModel\|QuickBookingRepository"

# View API calls
adb logcat | grep "OkHttp"

# View errors only
adb logcat *:E
```

Expected log messages:

**On Home Page Load:**
```
D/HomeFragment: Setting up animations
D/HomeViewModel: Loading demo time slots
D/HomeFragment: Animations started
```

**On Quick Booking Dialog Open:**
```
D/HomeFragment: Showing quick booking dialog
D/TimeSlotsAdapter: Binding 8 slots
```

**On Booking Creation:**
```
D/HomeViewModel: Creating booking for slot: SLT-001
D/QuickBookingRepository: Calling API: booking/create
D/OkHttp: --> POST http://localhost:7989/booking/create
D/OkHttp: {"slotId":"SLT-001"}
D/OkHttp: <-- 200 OK (123ms)
D/HomeViewModel: Booking successful: BKG-12345
```

---

## 📱 Manual Test Cases

### Test Case 1: Full Booking Flow
```
Test ID: TC-001
Title: Complete booking process
Priority: High

Steps:
1. Open app
2. Navigate to Home tab
3. Observe animations
4. Tap Quick Booking
5. Select available slot (SLT-001)
6. Confirm booking
7. Verify success message
8. Check booking ID appears

Expected: Booking created successfully
Status: [ ] Pass [ ] Fail
Notes: _________________________________
```

### Test Case 2: Inactive Slot Handling
```
Test ID: TC-002
Title: Verify inactive slots cannot be booked
Priority: Medium

Steps:
1. Open Quick Booking dialog
2. Find "Station Epsilon" (inactive, gray badge)
3. Attempt to tap it

Expected: No action, slot is disabled
Status: [ ] Pass [ ] Fail
Notes: _________________________________
```

### Test Case 3: Unavailable Slot Handling
```
Test ID: TC-003
Title: Verify unavailable slots show warning
Priority: Medium

Steps:
1. Open Quick Booking dialog
2. Find "Station Gamma" (dimmed, green badge)
3. Tap on it

Expected: Toast message "⚠️ This slot is not available"
Status: [ ] Pass [ ] Fail
Notes: _________________________________
```

### Test Case 4: Dialog Dismissal
```
Test ID: TC-004
Title: Test all dialog close methods
Priority: Low

Steps:
1. Open Quick Booking dialog
2. Test close button (✕)
3. Reopen dialog
4. Test Cancel button
5. Reopen dialog
6. Tap outside dialog

Expected: Dialog closes in all cases
Status: [ ] Pass [ ] Fail
Notes: _________________________________
```

---

## 🐛 Common Issues & Solutions

### Issue 1: Animations Not Visible
**Symptom**: Cards appear instantly without animation
**Solution**: 
- Check device animation settings
- Enable "Animator duration scale" in Developer Options
- Set to 1x or higher

### Issue 2: API Connection Failed
**Symptom**: "❌ Booking failed: Failed to connect"
**Solutions**:
- For **Emulator**: Use `10.0.2.2` instead of `localhost`
- For **Physical Device**: Use computer's IP address
- Ensure backend server is running
- Check firewall settings

### Issue 3: Dialog Not Showing
**Symptom**: Nothing happens when tapping Quick Booking
**Solutions**:
- Check logcat for errors
- Verify fragment is properly attached
- Rebuild and reinstall app

### Issue 4: Slots Not Loading
**Symptom**: Empty dialog or no slots visible
**Solutions**:
- Check `HomeViewModel.loadDemoTimeSlots()`
- Verify RecyclerView adapter is set
- Check logcat for exceptions

---

## 🎯 Performance Benchmarks

Expected performance metrics:

| Metric | Target | Acceptable |
|--------|--------|------------|
| Page load animation | 1.4s | < 2s |
| Dialog open time | < 300ms | < 500ms |
| API call response | < 1s | < 3s |
| Slot tap response | < 100ms | < 200ms |

---

## ✅ Testing Checklist

Copy and use this checklist:

```
UI Testing:
[ ] Home page loads with animations
[ ] Quick Booking card pulses continuously
[ ] All cards have proper elevation/shadows
[ ] Colors match design (green, orange, blue, purple)
[ ] Stats show correct numbers (12, 8)

Dialog Testing:
[ ] Dialog opens on Quick Booking tap
[ ] 8 time slots are displayed
[ ] Slots have correct colors (green/gray badges)
[ ] Active slots are bright, inactive are dimmed
[ ] Close button works
[ ] Cancel button works
[ ] Refresh button works

Interaction Testing:
[ ] Can tap active & available slots
[ ] Cannot tap inactive slots
[ ] Cannot tap unavailable slots
[ ] Confirmation dialog appears on slot tap
[ ] Confirmation shows correct slot details

API Testing (Backend Required):
[ ] Successful booking creates booking ID
[ ] Error handling works properly
[ ] Loading indicator appears during API call
[ ] Toast messages appear correctly
[ ] Dialog closes after successful booking
```

---

## 📊 Test Report Template

```
Test Date: __________
Tester: __________
Device: __________
Android Version: __________
Backend Status: [ ] Running [ ] Not Running

Results:
- Total Tests: ___
- Passed: ___
- Failed: ___
- Blocked: ___

Issues Found:
1. ____________________________________
2. ____________________________________
3. ____________________________________

Overall Status: [ ] Ready [ ] Needs Work
Comments: _________________________________
_________________________________
```

---

**Happy Testing! 🧪✨**
