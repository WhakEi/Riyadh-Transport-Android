# Live Journey Update Feature

## Overview

This feature provides real-time updates for journey planning by fetching live arrival data for transit segments and dynamically calculating journey times based on actual arrival information.

## Architecture

### Core Components

1. **LiveArrivalManager** (`utils/LiveArrivalManager.java`)
   - Fetches live arrival data from multiple API sources
   - Implements fallback logic when primary APIs fail
   - Refines destination names for better user instructions
   - Validates arrivals based on cumulative travel time

2. **JourneyTimeCalculator** (`utils/JourneyTimeCalculator.java`)
   - Implements the "time chain" logic with two counters:
     - **Cumulative Travel Time**: Tracks time for catching connections
     - **New Total Journey Time**: Includes live wait times
   - Processes segments sequentially considering dependencies
   - Handles missed connections (>45 min wait or already departed)

3. **RouteSegmentAdapter** (`adapters/RouteSegmentAdapter.java`)
   - Displays live arrival times with animations
   - Shows up to 3 upcoming arrivals per transit segment
   - Supports different UI states: checking, live, hidden, normal

4. **RouteDetailsActivity** (`RouteDetailsActivity.java`)
   - Manages auto-refresh every 60 seconds
   - Coordinates live data updates with UI
   - Proper lifecycle management

## API Integration

### Primary APIs
- `/metro_arrivals` - Gets metro arrival times
- `/bus_arrivals` - Gets bus arrival times

### Fallback APIs (when primary fails)
1. `/giveMeId` - Converts station name to station ID
2. RPT Station Details - Gets departures from rpt.sa
3. `/refineTerminus` - Refines destination names

## Time Chain Logic

The algorithm maintains two separate time counters:

```
For each segment:
  segmentRideMinutes = segment.duration / 60

  IF segment is WALK:
    cumulativeTravelTime += segmentRideMinutes
    newTotalJourneyTime += segmentRideMinutes
    
  ELSE IF segment is BUS or METRO:
    Fetch live arrivals for segment.stations[0]
    
    Find valid arrival where:
      - Matches segment.line
      - Matches destination
      - arrival.minutesUntil >= cumulativeTravelTime
      - wait time <= 45 minutes
    
    IF valid arrival found:
      waitMinutes = arrival.minutesUntil - cumulativeTravelTime
      newTotalJourneyTime += waitMinutes + segmentRideMinutes
      cumulativeTravelTime += segmentRideMinutes
      segment.status = "live" or "normal"
    ELSE:
      // Missed connection - use static time
      cumulativeTravelTime += segmentRideMinutes
      newTotalJourneyTime += segmentRideMinutes
      segment.status = "hidden"
```

## UI States

### Arrival Status Values

1. **"checking"**
   - Displayed while fetching live data
   - Shows "Checking..." text with clock icon

2. **"live"**
   - Arrival is less than 59 minutes away
   - Shows arrival times in green with animated indicator
   - Example: "5 min", "12 min", "25 min"

3. **"normal"**
   - Arrival is 59+ minutes away
   - Shows formatted time in black with clock icon
   - Example: "9:45 PM"

4. **"hidden"**
   - No live data available or connection missed
   - Arrival times are not displayed

## Animation

The live arrival indicator uses a 3-frame animation that cycles every 0.5 seconds:

- **LTR (English)**: lt3.xml → lt2.xml → lt1.xml
- **RTL (Arabic)**: lr3.xml → lr2.xml → lr1.xml

Each frame is a green pulsing circle that creates a "live" effect.

## String Resources

### English (`values/strings.xml`)
- `checking_live_data`: "Checking..."
- `arriving_now`: "Arriving now"
- `arriving_in_minutes`: "%d min"
- `arriving_at_time`: "%s"
- `towards`: "towards %s"
- `take_bus_towards`: "Take Bus %1$s towards %2$s and disembark at %3$s"
- `take_metro_towards`: "Take the %1$s towards %2$s and disembark at %3$s"

### Arabic (`values-ar/strings.xml`)
- Corresponding Arabic translations with proper RTL formatting

## Data Flow

```
RouteDetailsActivity
  └─> Timer (60s interval)
      └─> JourneyTimeCalculator.calculateLiveJourneyTime()
          └─> For each transit segment:
              └─> LiveArrivalManager.getLiveArrivals()
                  ├─> Try primary API (metro_arrivals/bus_arrivals)
                  └─> Fallback to RPT APIs if needed
                      ├─> giveMeId (get station ID)
                      ├─> RPT Station Details (get departures)
                      └─> refineTerminus (refine destination name)
              └─> Find valid arrival
              └─> Update segment with live data
          └─> Calculate new total journey time
      └─> Update UI
          └─> RouteSegmentAdapter.notifyDataSetChanged()
              └─> For each segment:
                  └─> Update arrival times with animation
```

## Usage

### Passing Route to Activity

```java
Intent intent = new Intent(context, RouteDetailsActivity.class);
Gson gson = new Gson();
String routeJson = gson.toJson(route);
intent.putExtra("route_json", routeJson);
startActivity(intent);
```

### Route Object Structure

```json
{
  "segments": [
    {
      "type": "walk",
      "duration": 300,
      "distance": 400,
      "stations": ["Start Point", "Metro Station"]
    },
    {
      "type": "metro",
      "line": "Blue Line",
      "duration": 600,
      "stations": ["Metro Station", "Transfer Station", "End Station"]
    },
    {
      "type": "walk",
      "duration": 180,
      "distance": 200,
      "stations": ["End Station", "Destination"]
    }
  ],
  "total_time": 1080
}
```

## Segment Runtime Fields

The following fields are added to `RouteSegment` at runtime (not serialized):

- `waitMinutes` (Integer): Wait time before boarding transit
- `arrivalStatus` (String): UI state - "checking", "live", "hidden", "normal"
- `refinedTerminus` (String): Refined destination name
- `nextArrivalMinutes` (Integer): Minutes until next arrival
- `upcomingArrivals` (List<Integer>): Up to 3 upcoming arrival times

## Performance Considerations

1. **Network Calls**: Multiple API calls per segment; uses callbacks to avoid blocking
2. **Memory**: Animation handlers properly cleaned up in `onPause()`/`onDestroy()`
3. **Battery**: 60-second refresh interval balances accuracy with battery usage
4. **Thread Safety**: Uses `AtomicInteger` for counter updates

## Error Handling

- Network failures trigger fallback APIs
- Missing data falls back to static times
- Invalid arrivals are filtered out
- Graceful degradation at every level

## Future Enhancements

1. User-configurable refresh intervals
2. Push notifications for delays
3. Historical accuracy tracking
4. Offline mode with cached predictions
5. Alternative route suggestions when connections are missed
