# Riyadh Transport - Wear OS Companion App

A companion Android Wear OS application for the Riyadh Transport mobile app, providing quick access to public transport information optimized for smartwatch displays.

## Features

### 1. GPS Location Handling
- **Built-in Watch GPS**: Automatically detects and uses the watch's GPS if available
- **Phone GPS Fallback**: Falls back to the connected phone's GPS if the watch doesn't have built-in GPS
- **Error Handling**: Displays clear error messages if no GPS is available

### 2. Search Feature
- **GPS-Based Starting Point**: Always uses the user's current GPS location as the starting point
- **No Keyboard Input**: Due to screen size limitations, users select destinations from:
  - **Favorites**: Synced from the paired phone
  - **Search History**: Recent searches synced from the paired phone
- **Swipeable Route Instructions**: Each instruction is presented as a slide that users can swipe through
- **Summary Slide**: Final slide shows journey summary with a return button
- **No Map Display**: Optimized for small screen - text-based instructions only

### 3. Stations Near Me
- **Compass Layout**: Innovative compass-style visualization showing nearby stations
- **User Position Indicator**: Center dot showing which direction the user is facing
- **Distance-Based Positioning**: 
  - Stations closer to user appear near the center
  - Farther stations appear near the edge
- **Station Clustering**: 
  - Overlapping stations are automatically clustered
  - Tap a cluster to zoom in and decluster
  - Reset button to return to full compass view
- **Real-time Orientation**: Uses device sensors to update compass as user rotates

### 4. Station Detail View
- **Live Arrivals**: Shows the closest upcoming arrivals for the selected station
- **Metro and Bus Support**: Works for both metro and bus stations
- **Clear Display**: Optimized list view showing line, destination, and arrival time

## Technical Architecture

### Module Structure
```
wear/
├── src/main/
│   ├── java/com/riyadhtransport/wear/
│   │   ├── MainActivity.java                 # Main menu
│   │   ├── SearchActivity.java               # Search and destination selection
│   │   ├── StationsNearMeActivity.java       # Compass view for nearby stations
│   │   ├── StationDetailsActivity.java       # Live arrivals display
│   │   ├── RouteInstructionsActivity.java    # Swipeable route slides
│   │   ├── adapters/                         # RecyclerView adapters
│   │   │   ├── DestinationAdapter.java
│   │   │   ├── ArrivalAdapter.java
│   │   │   └── RouteInstructionAdapter.java
│   │   ├── api/                              # API integration
│   │   │   ├── WearApiClient.java
│   │   │   └── WearTransportService.java
│   │   ├── models/                           # Data models
│   │   │   ├── WearStation.java
│   │   │   ├── WearArrival.java
│   │   │   ├── RouteInstruction.java
│   │   │   └── WearFavorite.java
│   │   ├── utils/                            # Utility classes
│   │   │   ├── WearLocationHelper.java
│   │   │   └── DataSyncHelper.java
│   │   └── views/                            # Custom views
│   │       └── CompassView.java
│   └── res/
│       ├── layout/                           # Activity and item layouts
│       ├── values/                           # Strings, colors, themes
│       └── xml/                              # Wearable configuration
└── build.gradle                              # Wear module dependencies
```

### Key Technologies
- **Wear OS 3.0+**: Minimum SDK 28 (Android 9.0)
- **Sensors**: Rotation vector sensor for compass orientation
- **Location**: FusedLocationProviderClient for GPS
- **Networking**: Retrofit + OkHttp for API calls
- **UI Components**: RecyclerView, ViewPager2, Custom Views
- **Data Sync**: Wearable Data Layer API (prepared for phone sync)

### Custom Components

#### CompassView
A custom view that visualizes nearby stations on a compass layout:
- Calculates bearing and distance for each station
- Normalizes distances for display (0.0 to 1.0)
- Handles station clustering based on visual overlap
- Responds to user rotation via sensors
- Supports zoom for decluttering

#### WearLocationHelper
Manages GPS location with intelligent fallback:
1. First attempts to get location from watch GPS
2. Falls back to phone GPS if watch GPS unavailable
3. Provides clear error messages if neither available
4. Utility methods for bearing and distance calculations

#### DataSyncHelper
Manages data synchronization between phone and watch:
- Stores favorites and search history locally
- Prepared for Wearable Data Layer integration
- Provides sample data for testing

## Installation

### Prerequisites
- Android Wear OS device or emulator running Wear OS 3.0+
- Paired Android phone with Riyadh Transport mobile app
- Location permissions granted

### Build Instructions

1. Open the project in Android Studio
2. Ensure the wear module is included in settings.gradle
3. Select the wear module configuration
4. Connect a Wear OS device or start a Wear OS emulator
5. Click Run

### Pairing with Phone App

The wear app is designed to work as a companion to the mobile app:
1. Install the mobile app on your phone
2. Install the wear app on your watch
3. Pair the devices via Wear OS companion app
4. Favorites and search history will sync automatically

## Usage

### First Launch
1. Grant location permission when prompted
2. The app will detect if using watch or phone GPS
3. Main menu provides two options: Search and Stations Near Me

### Search Flow
1. Tap "Search" button
2. Select destination from Favorites or Search History
3. Wait for route calculation
4. Swipe through route instructions (one step per slide)
5. View summary on final slide
6. Tap "Return to Menu" to go back

### Stations Near Me Flow
1. Tap "Stations Near Me" button
2. Compass loads showing nearby stations
3. Rotate to see stations in different directions
4. Tap individual station to view live arrivals
5. Tap cluster to zoom in and decluster
6. Use "Reset View" button to zoom back out

## Configuration

### API Endpoint
Update the BASE_URL in `WearApiClient.java`:
```java
private static final String BASE_URL = "https://riyadhtransport.tech/";
```

### Mock Data
For testing without backend, the app uses mock data:
- Sample favorites and search history in `DataSyncHelper`
- Mock nearby stations in `StationsNearMeActivity`
- Mock arrivals in `StationDetailsActivity`

## Permissions

Required permissions in AndroidManifest.xml:
- `ACCESS_FINE_LOCATION`: For GPS functionality
- `ACCESS_COARSE_LOCATION`: For network-based location
- `INTERNET`: For API calls
- `ACCESS_NETWORK_STATE`: For connectivity checks
- `BODY_SENSORS`: For orientation sensors
- `WAKE_LOCK`: For keeping screen on during navigation

## Limitations

### Screen Size
- No keyboard input (use voice or selection only)
- No map display (compass visualization instead)
- Limited text display (abbreviated station names)

### GPS Requirements
- Watch must have GPS or be connected to phone
- App cannot function without location data
- Indoor accuracy may be limited

### Data Sync
- Favorites and history sync requires phone connection
- Real-time sync not yet implemented (uses local storage)
- Manual refresh may be needed

## Future Enhancements

### Phase 1 (Current)
- ✅ GPS location handling
- ✅ Search with favorites/history
- ✅ Compass view for nearby stations
- ✅ Station details with arrivals
- ✅ Swipeable route instructions

### Phase 2 (Planned)
- [ ] Real-time data sync with phone via Wearable Data Layer
- [ ] Voice input for destination selection
- [ ] Haptic feedback for turn-by-turn navigation
- [ ] Complications for quick access
- [ ] Tile for at-a-glance information

### Phase 3 (Future)
- [ ] Offline mode with cached data
- [ ] Heart rate monitoring during walks
- [ ] Step tracking integration
- [ ] Multi-language support (Arabic)
- [ ] Battery optimization

## Troubleshooting

### GPS Not Working
- Ensure location permissions are granted
- Check that GPS is enabled on watch/phone
- Try moving outdoors for better signal
- Restart the app

### No Nearby Stations
- Check internet connection
- Verify you're in Riyadh area
- Try moving to a different location
- Check if backend API is accessible

### Stations Not Loading
- App falls back to mock data if API unavailable
- Check backend server status
- Verify API endpoint configuration
- Check device logs for errors

## Development

### Testing
```bash
# Build debug APK
./gradlew :wear:assembleDebug

# Install to connected device
./gradlew :wear:installDebug

# Run tests
./gradlew :wear:test
```

### Debugging
- Use Android Studio's Wear OS emulator
- Enable USB debugging on physical watch
- Check logcat for detailed logs
- Use mock data for offline testing

## Contributing

Contributions welcome! Areas for improvement:
- UI/UX enhancements
- Performance optimization
- Battery life improvements
- Additional features
- Bug fixes

## License

Same as main Riyadh Transport project.

## Support

For issues or questions:
- Open an issue on GitHub
- Check existing documentation
- Review logcat for error details
