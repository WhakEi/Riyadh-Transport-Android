# Integration Guide for Live Journey Update Feature

## Quick Start

### 1. Initialize API Client

In your `Application` class or `MainActivity.onCreate()`:

```java
import com.riyadhtransport.api.ApiClient;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}
```

### 2. Launch RouteDetailsActivity with a Route

From your route planning screen, after getting a route from the API:

```java
import com.riyadhtransport.RouteDetailsActivity;
import com.riyadhtransport.models.Route;
import com.google.gson.Gson;
import android.content.Intent;

// Assuming you have a Route object from your route planning
Route route = ...; // obtained from route planning API

// Convert to JSON
Gson gson = new Gson();
String routeJson = gson.toJson(route);

// Launch activity
Intent intent = new Intent(this, RouteDetailsActivity.class);
intent.putExtra("route_json", routeJson);
startActivity(intent);
```

### 3. Route Object Structure

Ensure your Route object follows this structure:

```java
Route route = new Route();
route.setTotalTime(1200); // total seconds

List<RouteSegment> segments = new ArrayList<>();

// Example: Walking segment
RouteSegment walkSegment = new RouteSegment();
walkSegment.setType("walk");
walkSegment.setDuration(300); // 5 minutes in seconds
walkSegment.setDistance(400.0); // 400 meters
List<String> walkStations = Arrays.asList("Start Point", "Metro Station A");
walkSegment.setStations(walkStations);
segments.add(walkSegment);

// Example: Metro segment
RouteSegment metroSegment = new RouteSegment();
metroSegment.setType("metro");
metroSegment.setLine("Blue Line");
metroSegment.setDuration(600); // 10 minutes
List<String> metroStations = Arrays.asList(
    "Metro Station A", 
    "Metro Station B", 
    "Metro Station C"
);
metroSegment.setStations(metroStations);
segments.add(metroSegment);

// Example: Bus segment
RouteSegment busSegment = new RouteSegment();
busSegment.setType("bus");
busSegment.setLine("230");
busSegment.setDuration(480); // 8 minutes
List<String> busStations = Arrays.asList(
    "Bus Stop 1",
    "Bus Stop 2", 
    "Bus Stop 3"
);
busSegment.setStations(busStations);
segments.add(busSegment);

// Final walking segment
RouteSegment finalWalk = new RouteSegment();
finalWalk.setType("walk");
finalWalk.setDuration(180); // 3 minutes
finalWalk.setDistance(200.0);
List<String> finalStations = Arrays.asList("Bus Stop 3", "Destination");
finalWalk.setStations(finalStations);
segments.add(finalWalk);

route.setSegments(segments);
```

## Testing with Mock Data

### Option 1: Create a Test Activity

Create a simple test activity to launch RouteDetailsActivity with mock data:

```java
public class TestRouteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Button testButton = new Button(this);
        testButton.setText("Test Live Route");
        testButton.setOnClickListener(v -> launchTestRoute());
        setContentView(testButton);
    }
    
    private void launchTestRoute() {
        Route route = createMockRoute();
        
        Gson gson = new Gson();
        String routeJson = gson.toJson(route);
        
        Intent intent = new Intent(this, RouteDetailsActivity.class);
        intent.putExtra("route_json", routeJson);
        startActivity(intent);
    }
    
    private Route createMockRoute() {
        Route route = new Route();
        List<RouteSegment> segments = new ArrayList<>();
        
        // Walk to Olaya Station
        RouteSegment walk1 = new RouteSegment();
        walk1.setType("walk");
        walk1.setDuration(300);
        walk1.setDistance(400.0);
        walk1.setStations(Arrays.asList("Current Location", "Olaya"));
        segments.add(walk1);
        
        // Take Metro Blue Line
        RouteSegment metro = new RouteSegment();
        metro.setType("metro");
        metro.setLine("Blue Line");
        metro.setDuration(900);
        metro.setStations(Arrays.asList("Olaya", "King Abdullah", "Qasr Al Hokm"));
        segments.add(metro);
        
        // Walk to destination
        RouteSegment walk2 = new RouteSegment();
        walk2.setType("walk");
        walk2.setDuration(240);
        walk2.setDistance(300.0);
        walk2.setStations(Arrays.asList("Qasr Al Hokm", "Destination"));
        segments.add(walk2);
        
        route.setSegments(segments);
        route.setTotalTime(1440);
        
        return route;
    }
}
```

### Option 2: Modify Existing Route Fragment

If you have an existing route planning fragment, modify it to use RouteDetailsActivity:

```java
// In your RouteFragment or similar
private void displayRoute(Route route) {
    // Instead of showing route inline, launch details activity
    Gson gson = new Gson();
    String routeJson = gson.toJson(route);
    
    Intent intent = new Intent(getActivity(), RouteDetailsActivity.class);
    intent.putExtra("route_json", routeJson);
    startActivity(intent);
}
```

## Verifying the Feature

### Visual Checks

1. **Initial Load**: 
   - Total time should be displayed at top
   - Walking segments should NOT show arrival times
   - Transit segments should show "Checking..." initially

2. **After 1-2 seconds**:
   - Transit segments should show live arrival times in green
   - Animation should be pulsing (3 frames, 0.5s each)
   - Up to 3 arrival times displayed per segment
   - Text format: "5 min", "12 min", "25 min"

3. **For arrivals > 59 minutes**:
   - Should show clock icon instead of animation
   - Time displayed as "9:45 PM" in black text

4. **Auto-refresh**:
   - Every 60 seconds, arrival times should update
   - Animation should continue smoothly
   - Total journey time may change based on live data

### Log Checks

Enable verbose logging to see the feature working:

```bash
adb logcat | grep -E "(LiveArrivalManager|JourneyTimeCalculator|RouteDetailsActivity)"
```

Expected logs:
```
D/LiveArrivalManager: Getting live arrivals for: Olaya, type: metro
D/LiveArrivalManager: Primary API success: 3 arrivals
D/JourneyTimeCalculator: Processing segment 1 (type: metro, duration: 15 min)
D/JourneyTimeCalculator: Fetching live arrivals for Olaya (cumulative time: 5 min)
D/JourneyTimeCalculator: Valid arrival found: 8 min until departure
D/RouteDetailsActivity: Journey time updated: 28 minutes
```

## Troubleshooting

### Issue: "Checking..." never changes

**Possible causes:**
1. Network connectivity issues
2. API endpoints not configured correctly
3. Station names don't match API data

**Solutions:**
- Check API base URL in `ApiClient.java`
- Verify station names match API database
- Check logcat for error messages

### Issue: Animation not showing

**Possible causes:**
1. Arrival status not set to "live"
2. Arrivals > 59 minutes (shows as "normal" instead)
3. No valid arrivals found

**Solutions:**
- Check that `upcomingArrivals` list is populated
- Verify `arrivalStatus` is set to "live" not "normal"
- Ensure arrivals are within catchable timeframe

### Issue: Total time doesn't update

**Possible causes:**
1. Timer not running
2. Activity in background (paused)
3. Callback not being invoked

**Solutions:**
- Check that `onResume()` is starting the timer
- Verify `refreshHandler` is not null
- Add logging to `refreshLiveData()` callback

## Advanced Configuration

### Changing Refresh Interval

In `RouteDetailsActivity.java`:

```java
private static final long REFRESH_INTERVAL_MS = 30000; // 30 seconds
```

### Changing Max Wait Time

In `LiveArrivalManager.java`:

```java
private static final int MAX_WAIT_MINUTES = 60; // Allow up to 60 minute waits
```

### Customizing Animation Speed

In `RouteSegmentAdapter.java`, in the `startLiveAnimation()` method:

```java
animationHandler.postDelayed(this, 300); // 0.3 seconds per frame
```

## API Requirements

Your backend APIs must support:

### 1. Metro/Bus Arrivals API

**Endpoint:** `POST /metro_arrivals` or `POST /bus_arrivals`

**Request:**
```json
{
  "station_name": "Olaya"
}
```

**Response:**
```json
{
  "station_name": "Olaya",
  "arrivals": [
    {
      "line": "Blue Line",
      "destination": "King Abdullah Financial District",
      "minutes_until": 5
    },
    {
      "line": "Blue Line",
      "destination": "King Abdullah Financial District",
      "minutes_until": 12
    }
  ]
}
```

### 2. Station ID Lookup (Fallback)

**Endpoint:** `POST /giveMeId`

**Request:**
```json
{
  "station_name": "Olaya"
}
```

**Response:**
```json
{
  "station_name": "Olaya",
  "matches": [
    {
      "full_station_name": "Olaya (Metro)",
      "station_id": "10012345",
      "type": "metro"
    }
  ]
}
```

### 3. Terminus Refinement

**Endpoint:** `POST /refineTerminus`

**Request:**
```json
{
  "line_number": "230",
  "api_destination": "King Abdullah Financial District"
}
```

**Response:**
```json
{
  "line_number": "230",
  "api_destination": "King Abdullah Financial District",
  "refined_terminus": "KAFD"
}
```

## Next Steps

1. Test with real API data
2. Add error messages for network failures
3. Implement pull-to-refresh gesture
4. Add notification for missed connections
5. Save last refresh timestamp
6. Add offline mode with cached predictions
