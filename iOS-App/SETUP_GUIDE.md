# iOS App Setup Guide

This guide will help you set up the iOS version of the Riyadh Transport app in Xcode.

## Prerequisites

- macOS 12.0 or later
- Xcode 14.0 or later
- iOS 15.0+ device or simulator
- Apple Developer account (for device testing)

## Step-by-Step Setup

### 1. Create a New Xcode Project

Since the iOS app files are provided as source code, you need to create an Xcode project:

1. **Open Xcode**
2. **File → New → Project**
3. Choose **iOS** tab
4. Select **App** template
5. Click **Next**

### 2. Configure Project Settings

Fill in the project details:

- **Product Name**: `RiyadhTransport`
- **Team**: Select your team (or None for simulator-only testing)
- **Organization Identifier**: `com.riyadhtransport` (or your own)
- **Bundle Identifier**: Will be auto-generated as `com.riyadhtransport.RiyadhTransport`
- **Interface**: **SwiftUI**
- **Language**: **Swift**
- **Storage**: **None** (we'll use UserDefaults)
- **Include Tests**: Check if you want to add tests

Click **Next** and save the project in the `iOS-App` directory (select the parent of the RiyadhTransport folder).

### 3. Add Source Files to Project

#### Option A: Drag and Drop (Recommended)

1. In Xcode's Project Navigator, right-click on the `RiyadhTransport` folder
2. Select **Add Files to "RiyadhTransport"...**
3. Navigate to the `RiyadhTransport` folder in Finder
4. Select all folders: `Models`, `Services`, `Utilities`, `Views`, `Resources`
5. Make sure these options are checked:
   - ✅ **Copy items if needed**
   - ✅ **Create groups**
   - ✅ **Add to targets: RiyadhTransport**
6. Click **Add**

#### Option B: Manual File Addition

For each folder (Models, Services, Utilities, Views, Resources):
1. Right-click on `RiyadhTransport` in Project Navigator
2. Choose **New Group**
3. Name it appropriately (Models, Services, etc.)
4. Right-click on the new group
5. Choose **Add Files to "RiyadhTransport"...**
6. Add the corresponding `.swift` files

### 4. Replace App Entry Point

Delete the default `RiyadhTransportApp.swift` and `ContentView.swift` if they were auto-generated, then add the versions from this project.

### 5. Configure Info.plist

1. Select the `Info.plist` file in Project Navigator
2. Verify or add these keys:

**Location Permissions:**
- Key: `Privacy - Location When In Use Usage Description`
- Value: `This app needs access to your location to show nearby stations and plan routes.`

**Supported Localizations:**
- Add `ar` (Arabic) to the localizations list

**Appearance:**
- The app automatically supports dark mode via iOS system settings

### 6. Add Localizations

1. Select the project in Project Navigator
2. Select the `RiyadhTransport` target
3. Go to the **Info** tab
4. Under **Localizations**, click **+** to add **Arabic (ar)**
5. Add the `Localizable.strings` files:
   - Right-click `Resources` folder
   - New File → Strings File
   - Name it `Localizable.strings`
   - In File Inspector, click **Localize...**
   - Add both English and Arabic versions

### 7. Configure Build Settings

1. Select the project in Project Navigator
2. Select the `RiyadhTransport` target
3. Go to **Build Settings** tab

**Deployment Info:**
- iOS Deployment Target: `15.0` or later

**Swift Compiler:**
- Swift Language Version: `Swift 5`

### 8. Configure Signing & Capabilities

1. Select the `RiyadhTransport` target
2. Go to **Signing & Capabilities** tab
3. **Team**: Select your team
4. **Bundle Identifier**: Verify it's set correctly

**Add Capabilities:**
- Click **+ Capability**
- Add **Background Modes** (optional, for location updates)
  - Check **Location updates** if needed

### 9. Test Build

1. Select a simulator (e.g., iPhone 14 Pro)
2. Click the **Run** button (▶️) or press `Cmd + R`
3. The app should build and run

If you encounter errors, check:
- All files are added to the target
- Swift version is correct
- No duplicate files

### 10. Test on Device

To test on a physical device:

1. Connect your iPhone/iPad via USB
2. Select your device from the device menu
3. Ensure your device is registered in your Apple Developer account
4. Click Run

**First Time Setup:**
- You may need to trust the developer certificate on your device
- Settings → General → VPN & Device Management → Trust

## Project Structure in Xcode

Your Xcode project should look like this:

```
RiyadhTransport
├── RiyadhTransport/
│   ├── RiyadhTransportApp.swift
│   ├── Models/
│   │   ├── Station.swift
│   │   ├── Route.swift
│   │   ├── RouteSegment.swift
│   │   ├── Line.swift
│   │   ├── Arrival.swift
│   │   └── SearchResult.swift
│   ├── Services/
│   │   └── APIService.swift
│   ├── Utilities/
│   │   ├── LocationManager.swift
│   │   ├── FavoritesManager.swift
│   │   └── LineColorHelper.swift
│   ├── Views/
│   │   ├── ContentView.swift
│   │   ├── MapView.swift
│   │   ├── RouteView.swift
│   │   ├── StationsView.swift
│   │   ├── LinesView.swift
│   │   ├── StationDetailView.swift
│   │   ├── LineDetailView.swift
│   │   ├── FavoritesView.swift
│   │   └── SettingsView.swift
│   ├── Resources/
│   │   ├── Localizable.strings (English)
│   │   └── ar.lproj/
│   │       └── Localizable.strings (Arabic)
│   ├── Assets.xcassets/
│   └── Info.plist
└── RiyadhTransport.xcodeproj
```

## Common Issues and Solutions

### Issue: "No such module 'MapKit'"
**Solution**: Make sure you're building for iOS, not macOS. Check the deployment target.

### Issue: Location not working
**Solution**: 
1. Check Info.plist has location permission strings
2. Run on a device or simulator with location enabled
3. In simulator: Features → Location → Custom Location

### Issue: Views not loading
**Solution**: 
1. Check all files are added to the target
2. Verify import statements are correct
3. Clean build folder (Cmd + Shift + K)

### Issue: Localization not working
**Solution**:
1. Verify Localizable.strings files are in correct folders
2. Check they're added to the target
3. Test by changing device language in Settings

### Issue: API calls failing
**Solution**:
1. Check internet connectivity
2. Verify backend server is running
3. Check NSAppTransportSecurity settings if using HTTP

## Running Tests

If you added tests during project creation:

1. Press `Cmd + U` to run all tests
2. Or click the diamond icon next to test functions

## Next Steps

After setup:

1. **Customize the app**: Update colors, fonts, and layouts
2. **Add features**: Implement additional functionality
3. **Test thoroughly**: Test on multiple devices and iOS versions
4. **Prepare for release**: Configure for App Store submission

## Additional Configuration

### App Icons

1. Open `Assets.xcassets`
2. Click `AppIcon`
3. Drag and drop icon images for each size
4. Use 1024x1024 for App Store

Recommended tool: [App Icon Generator](https://appicon.co/)

### Launch Screen

1. Open `LaunchScreen.storyboard` (if using storyboards)
2. Or configure in Build Settings → Launch Screen
3. Keep it simple - iOS shows this briefly

### Backend URL Configuration

To change the backend URL:
1. Open `Services/APIService.swift`
2. Modify the `baseURL` constant
3. For local testing: Use your Mac's IP address (not localhost)

Example: `http://192.168.1.100:5000/`

## Distribution

### TestFlight (Internal Testing)

1. Archive the app (Product → Archive)
2. Upload to App Store Connect
3. Invite internal testers

### App Store

1. Complete App Store Connect setup
2. Provide screenshots and metadata
3. Submit for review

## Support

If you encounter issues:
1. Check this guide
2. Review the main README.md
3. Open an issue on GitHub with:
   - Xcode version
   - iOS version
   - Error messages
   - Steps to reproduce

## Resources

- [Apple SwiftUI Tutorials](https://developer.apple.com/tutorials/swiftui)
- [Swift Documentation](https://swift.org/documentation/)
- [MapKit Documentation](https://developer.apple.com/documentation/mapkit)
- [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
