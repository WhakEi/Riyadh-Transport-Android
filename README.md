# Riyadh Transport Android App

An Android mobile application for Riyadh public transport, built in Java. This app provides route planning, station information, and live arrival times for metro and bus services in Riyadh.

## Feature Checklist

- ✅ **Route Planning**: Find routes between locations using public transport
- ✅ **Station Search**: Browse and search all metro and bus stations
- ✅ **Live Arrivals**: View real-time arrival information for metro and buses
- ✅ **Interactive Map**: View stations and routes with OpenStreetMap and MapTiler
- ✅ **GPS Location**: Use your current location as starting point
- ✅ **Multilingual**: Supports English and Arabic (العربية) with language-specific map labels
- ✅ **Favorites and Route History**: Ability to save a location or reopen a recently searched one to redo a route search. As well as favorite a line or view arrivals on a favorite station
- ✅ **Dark Mode**: View UI and Map in Dark Mode when enabled by OS
- ✅ **Material Design 3**: Modern Material You design with dynamic colors on Android 12+
- ⏳ **iOS Support**: [Check out the iOS Repository for more information](https://github.com/WhakEi/Riyadh-Transport-iOS)


## Installation
You may install the Pre-Built APK in [Releases](https://github.com/WhakEi/Riyadh-Transport-Mobile/releases). A device running Android 7.0 (Nougat) or higher is required

## Build from Source

### 1. Clone the Repository

```bash
git clone https://github.com/WhakEi/Riyadh-Transport-Mobile.git
cd Riyadh-Transport-Mobile
```

### 2. Build and Run
**Prerequisites:**
- Android Studio Arctic Fox or later
- Gradle 8.3.6 or higher

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click Run (▶️) button

## Project Structure

```
Riyadh-Transport-Mobile/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/riyadhtransport/
│   │       │   ├── adapters/          # RecyclerView adapters
│   │       │   ├── api/               # API service and client
│   │       │   ├── fragments/         # UI fragments
│   │       │   ├── models/            # Data models
│   │       │   ├── utils/             # Utility classes
│   │       │   └── MainActivity.java  # Main activity
│   │       ├── res/
│   │       │   ├── layout/            # XML layouts
│   │       │   ├── values/            # Strings, colors, themes (English)
│   │       │   └── values-ar/         # Arabic translations
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## API Integration

The app will communicate with the Flask backend server using the following endpoints:

- `GET /api/stations` - Get all stations
- `POST /nearbystations` - Find nearby stations
- `POST /route_from_coords` - Find route from coordinates
- `POST /metro_arrivals` - Get metro arrival times
- `POST /bus_arrivals` - Get bus arrival times
- `GET /buslines` - Get all bus lines
- `GET /mtrlines` - Get all metro lines

## Technologies Used

- **Language**: Java
- **UI Framework**: Android SDK, Material Design 3 (Material You)
- **Maps**: OSMDroid (OpenStreetMap) with MapTiler tiles
- **Location**: Google Play Services Location
- **Networking**: Retrofit 2, OkHttp
- **JSON Parsing**: Gson
- **Architecture**: Fragment-based with ViewPager2 and TabLayout

## Design

The app uses **Material Design 3** (Material You) providing:
- Modern, refined UI components
- Adaptive colors that match user's wallpaper on Android 12+
- Automatic light/dark theme switching
- Full backward compatibility with Android 7+
- Consistent design language across the app

For more details, see [MATERIAL_DESIGN_3_MIGRATION.md](MATERIAL_DESIGN_3_MIGRATION.md)

## Map Features

The app uses **OSMDroid** with **MapTiler** tiles, providing:
- Language-specific map labels (automatically matches app language)
- High-quality street maps
- No API key required
- Open-source mapping solution
- Same map provider as the web frontend for consistency

## Permissions Required

- `ACCESS_FINE_LOCATION` - For GPS-based features
- `ACCESS_COARSE_LOCATION` - For network-based location
- `INTERNET` - For API communication and map tiles
- `ACCESS_NETWORK_STATE` - For network status checking
- `WRITE_EXTERNAL_STORAGE` - For map tile caching (Android 12 and below)
- `READ_EXTERNAL_STORAGE` - For map tile caching (Android 12 and below)

## Testing

The app can be tested with:
- **Android Emulator**: Use Android Studio's built-in emulator
- **Physical Device**: Enable USB debugging and connect your device

Note: For local backend testing on emulator, use `http://10.0.2.2:5000/` as the BASE_URL.

## Known Limitations

- Backend server must be running and accessible
- Some features require active internet connection
- Map tiles are downloaded on demand (requires internet)

## Future Enhancements

- Push notifications for service alerts
- Server-side vector map rendering

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues or questions, please open an issue on the GitHub repository.
