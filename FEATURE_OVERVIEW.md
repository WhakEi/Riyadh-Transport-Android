# Live Journey Update Feature - Overview

## What Problem Does This Solve?

When planning a journey with public transport, static times don't account for:
- Real-time arrival delays
- Current wait times at stations
- Whether you can actually catch a connection

This feature calculates a **dynamic journey time** based on live arrival data, ensuring users know exactly when they'll arrive.

## Visual Example

### Scenario: Journey from Home to Destination

```
┌─────────────────────────────────────────────────────────────────┐
│                     ROUTE DETAILS                                │
│                                                                   │
│  Total Time: 28 minutes  ← Updates based on live data            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  🚶 Walk to Olaya Metro Station                    5 min         │
│     0.4 km                                                        │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  🚇 Take the Blue Line towards KAFD                15 min        │
│     and disembark at King Abdullah                                │
│                                                                   │
│                               🟢 8 min  15 min  28 min  ← Live!  │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  🚌 Take Bus 230 towards Al-Malqa                  8 min         │
│     and disembark at Northern Station                            │
│                                                                   │
│                               🟢 3 min  18 min  35 min  ← Live!  │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  🚶 Walk to your destination                        3 min        │
│     0.2 km                                                        │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

🟢 = Animated pulsing green dot

## How It Works

### 1. Time Chain Algorithm

The system tracks TWO separate time counters:

```
Cumulative Travel Time (CTT):
  → Tracks "how long until I can board this vehicle?"
  → Used to determine if arrivals are catchable
  
New Total Journey Time (NTJT):
  → Tracks "how long will my entire trip take?"
  → Includes live wait times
  → This is what we show to the user
```

### 2. Example Calculation

Let's walk through the example above:

#### Segment 1: Walk to Olaya (5 minutes)
```
Action: Walk
CTT:  0 + 5 = 5 minutes
NTJT: 0 + 5 = 5 minutes
```
*You'll arrive at Olaya station in 5 minutes*

#### Segment 2: Metro Blue Line (15 minutes ride)
```
Action: Check live arrivals at Olaya
Found:  Metro arrives in 8 minutes (from now)

Can we catch it?
  8 minutes (arrival) >= 5 minutes (CTT) ✓ Yes!

Wait time: 8 - 5 = 3 minutes

CTT:  5 + 15 = 20 minutes  (just the ride time)
NTJT: 5 + 3 + 15 = 23 minutes  (walk + wait + ride)
```
*You wait 3 minutes, then ride for 15 minutes*

#### Segment 3: Bus 230 (8 minutes ride)
```
Action: Check live arrivals at Northern Station
Found:  Bus arrives in 23 minutes (from now)

Can we catch it?
  23 minutes (arrival) >= 20 minutes (CTT) ✓ Yes!

Wait time: 23 - 20 = 3 minutes

CTT:  20 + 8 = 28 minutes
NTJT: 23 + 3 + 8 = 34 minutes
```
*You arrive at the bus stop in 20 min, wait 3 min, then ride 8 min*

#### Segment 4: Final Walk (3 minutes)
```
Action: Walk to destination
CTT:  28 + 3 = 31 minutes
NTJT: 34 + 3 = 37 minutes
```

**Final journey time: 37 minutes** (instead of the static 31 minutes!)

### 3. Missed Connection Handling

If you can't catch an arrival:

```
Scenario: Metro arrives in 3 minutes, but you need 5 minutes to walk there

Found:  Metro arrives in 3 minutes
CTT:    5 minutes

Can we catch it?
  3 minutes < 5 minutes ✗ No!

Action: Use static time (fallback)
CTT:  5 + 15 = 20 minutes
NTJT: 5 + 15 = 20 minutes
Status: "hidden" (no live times shown)
```

### 4. API Fallback Chain

```
Primary Attempt:
  POST /metro_arrivals or /bus_arrivals
    ↓
  Success? Show live times ✓
    ↓
  Failed? Try fallback...

Fallback Attempt:
  1. POST /giveMeId → Get station ID
    ↓
  2. POST to rpt.sa → Get departures
    ↓
  3. POST /refineTerminus → Clean up names
    ↓
  4. Convert to standard format
    ↓
  Success? Show live times ✓
    ↓
  Failed? Use static times
```

## UI States

### State 1: Checking
```
┌─────────────────────────────────────────────┐
│  🚇 Take the Blue Line                      │
│     and disembark at King Abdullah          │
│                                              │
│                    🕐 Checking...            │
└─────────────────────────────────────────────┘
```

### State 2: Live (<59 minutes)
```
┌─────────────────────────────────────────────┐
│  🚇 Take the Blue Line towards KAFD         │
│     and disembark at King Abdullah          │
│                                              │
│            🟢 3 min  12 min  25 min         │
└─────────────────────────────────────────────┘
```
*Green pulsing animation*

### State 3: Normal (≥59 minutes)
```
┌─────────────────────────────────────────────┐
│  🚇 Take the Blue Line towards KAFD         │
│     and disembark at King Abdullah          │
│                                              │
│                        🕐 9:45 PM            │
└─────────────────────────────────────────────┘
```
*Black text, clock icon, no animation*

### State 4: Hidden (missed/no data)
```
┌─────────────────────────────────────────────┐
│  🚇 Take the Blue Line                      │
│     and disembark at King Abdullah          │
│                                              │
│     (no arrival times shown)                │
└─────────────────────────────────────────────┘
```

## Animation Details

The live arrival indicator cycles through 3 frames:

```
Frame 1 (0.0s):  ●●●●●●  (Large circle)
Frame 2 (0.5s):  ●●●●    (Medium circle)  
Frame 3 (1.0s):  ●●      (Small circle)
Frame 1 (1.5s):  ●●●●●●  (Large circle)
...repeats...
```

This creates a "breathing" or "pulsing" effect that draws attention to live data.

## Auto-Refresh Behavior

```
Timeline:
  0:00  → Load route, start fetching live data
  0:02  → Live data loaded, display arrivals
  1:00  → Auto-refresh triggered
  1:02  → Updated arrivals displayed
  2:00  → Auto-refresh triggered
  ...every 60 seconds...
```

When the user:
- **Leaves app**: Refresh stops (onPause)
- **Returns to app**: Refresh resumes (onResume)
- **Closes activity**: Cleanup happens (onDestroy)

## Key Benefits

1. **Accurate Times**: Real-time wait times included
2. **Connection Validation**: Won't show arrivals you can't catch
3. **Multiple Fallbacks**: Resilient to API failures
4. **User-Friendly**: Clear visual indicators (animation, colors, icons)
5. **Localized**: Supports English and Arabic (including RTL)
6. **Battery Efficient**: 60-second refresh, stops when not visible
7. **Graceful Degradation**: Falls back to static times if APIs fail

## Architecture Summary

```
┌─────────────────────────────────────────────────────────────┐
│                   RouteDetailsActivity                       │
│  - Timer (60s)                                               │
│  - Total time display                                        │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              JourneyTimeCalculator                           │
│  - Implements time chain logic                               │
│  - Processes segments sequentially                           │
│  - Handles cumulative timing                                 │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│               LiveArrivalManager                             │
│  - Fetches from primary APIs                                 │
│  - Fallback to RPT APIs                                      │
│  - Validates & filters arrivals                              │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              RouteSegmentAdapter                             │
│  - Displays segments                                         │
│  - Animates live arrivals                                    │
│  - Updates UI every refresh                                  │
└─────────────────────────────────────────────────────────────┘
```

## Files Added/Modified

### New Files (11):
1. `LiveArrivalManager.java` - Live data fetching
2. `JourneyTimeCalculator.java` - Time chain logic
3. `RptStationService.java` - RPT API interface
4. `StationIdResponse.java` - Station ID model
5. `RefinedTerminusResponse.java` - Terminus model
6. `StationDeparture.java` - RPT departure model
7. `ArrivalResponse.java` - Arrival wrapper model
8. `ic_clock.xml` - Clock icon drawable
9. `lt1.xml`, `lt2.xml`, `lt3.xml` - LTR animation frames
10. `lr1.xml`, `lr2.xml`, `lr3.xml` - RTL animation frames

### Modified Files (9):
1. `RouteDetailsActivity.java` - Added refresh logic
2. `RouteSegmentAdapter.java` - Added live arrival display
3. `RouteSegment.java` - Added runtime fields
4. `ApiClient.java` - Added RPT service support
5. `TransportApiService.java` - Added new endpoints
6. `item_route_segment.xml` - Added arrival layout
7. `activity_route_details.xml` - Fixed TextView ID
8. `strings.xml` (en) - Added live arrival strings
9. `strings.xml` (ar) - Added Arabic translations

### Documentation Files (3):
1. `LIVE_ARRIVAL_FEATURE.md` - Technical documentation
2. `INTEGRATION_GUIDE.md` - Integration examples
3. `FEATURE_OVERVIEW.md` - This file

## Next Steps for Testing

1. Create a TestRouteActivity (see INTEGRATION_GUIDE.md)
2. Launch with mock Route data
3. Verify animations and live times appear
4. Check logs for API calls
5. Test auto-refresh (wait 60+ seconds)
6. Test Arabic/RTL layout
7. Test with network failures

## Performance Impact

- **Memory**: ~100 KB additional (models + managers)
- **Network**: 1-3 API calls per transit segment per refresh
- **Battery**: Minimal (60s refresh, Handler cleanup)
- **CPU**: Low (main work is I/O, not computation)

## Compatibility

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Languages**: English, Arabic
- **Layout**: LTR, RTL
