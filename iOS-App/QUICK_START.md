# iOS App Quick Start Guide

Get the Riyadh Transport iOS app up and running in under 10 minutes!

## Prerequisites

- Mac with macOS 12.0+
- Xcode 14.0+ (download from App Store)
- 15 minutes of time

## Quick Setup (5 Steps)

### Step 1: Open Xcode

```bash
# Navigate to the iOS-App directory
cd iOS-App

# Open Xcode
open -a Xcode
```

### Step 2: Create New Project

1. **File → New → Project**
2. Choose **iOS** → **App**
3. Configure:
   - Product Name: `RiyadhTransport`
   - Interface: **SwiftUI**
   - Language: **Swift**
4. Save in `iOS-App` directory

### Step 3: Add Files

Drag these folders from Finder into Xcode's Project Navigator:
- `Models`
- `Services`
- `Utilities`
- `Views`
- `Resources`

Make sure to:
- ✅ Check "Copy items if needed"
- ✅ Select "Create groups"
- ✅ Add to target: RiyadhTransport

### Step 4: Replace Main Files

Replace the auto-generated files:
1. Delete default `RiyadhTransportApp.swift` and `ContentView.swift`
2. Use the ones from this project (already in Views/)

### Step 5: Build & Run

1. Select **iPhone 14 Pro** simulator
2. Click **▶️** (or press Cmd+R)
3. Wait for build (first time takes ~1 minute)
4. App launches!

## That's It! 🎉

The app should now be running. You'll see:
- Map centered on Riyadh
- Bottom sheet with tabs
- Route, Stations, and Lines tabs

## First Launch Checklist

When the app first runs:

1. ✅ **Grant Location Permission**
   - Tap "Allow While Using App"

2. ✅ **Test Route Tab**
   - Tap location button to use current location
   - Enter coordinates: `24.7136, 46.6753`

3. ✅ **Test Stations Tab**
   - Browse stations list
   - Try the search
   - Tap "Nearby Stations"

4. ✅ **Test Lines Tab**
   - View Metro lines (Blue, Red, Orange, Yellow, Green, Purple)
   - Switch to Bus lines

5. ✅ **Test Settings**
   - Tap gear icon (⚙️)
   - Try switching to Arabic

## Common First-Run Issues

### "No such module 'MapKit'"
**Fix**: Clean build folder (Cmd+Shift+K) and rebuild

### "Cannot find 'Station' in scope"
**Fix**: Make sure all files are added to target

### Location not working
**Fix**: 
- Simulator: Features → Location → Custom Location
- Device: Settings → Privacy → Location Services

### Arabic text appears wrong
**Fix**: Device/Simulator must support Arabic. Try Settings → General → Language & Region

## Quick Tips

### Change Backend URL
Edit `Services/APIService.swift`:
```swift
private let baseURL = "http://YOUR_SERVER:5000/"
```

### Test on Device
1. Connect iPhone via USB
2. Select device from menu
3. Click Run
4. Trust certificate on device

### View in Arabic
1. Simulator: Settings → General → Language & Region → Arabic
2. Or use Settings in app

### Enable Dark Mode
1. Simulator: Settings → Developer → Dark Appearance
2. Or Control Center → Brightness → Long press

## Project File Structure

After setup, your Xcode project should look like:

```
RiyadhTransport/
├── RiyadhTransportApp.swift     ← Entry point
├── Models/
│   └── *.swift                  ← 6 model files
├── Services/
│   └── APIService.swift         ← API client
├── Utilities/
│   └── *.swift                  ← 3 utility files
├── Views/
│   └── *.swift                  ← 9 view files
├── Resources/
│   ├── Localizable.strings      ← English
│   └── ar.lproj/
│       └── Localizable.strings  ← Arabic
├── Assets.xcassets/
└── Info.plist
```

## Testing Checklist

Once the app runs, test these features:

- [ ] Map displays Riyadh
- [ ] Bottom sheet opens/closes
- [ ] Tabs switch correctly
- [ ] Route planning works
- [ ] Station list loads
- [ ] Search functions
- [ ] Line list displays
- [ ] Settings open
- [ ] Language switches
- [ ] Favorites save

## Next Steps

After successful setup:

1. **Customize UI**: Modify colors in Views/
2. **Add Icons**: Import app icons to Assets.xcassets
3. **Test Features**: Try all functionality
4. **Read Docs**: Check README.md for details
5. **Deploy**: See SETUP_GUIDE.md for distribution

## Keyboard Shortcuts

Useful Xcode shortcuts:

- `Cmd + R` - Run
- `Cmd + B` - Build
- `Cmd + .` - Stop
- `Cmd + Shift + K` - Clean
- `Cmd + /` - Comment
- `Cmd + Click` - Jump to definition
- `Option + Click` - Quick help

## Simulator Tips

Useful simulator features:

- **Device → Rotate Left/Right**: Test landscape
- **Features → Location**: Test GPS
- **Features → Toggle Appearance**: Test dark mode
- **I/O → Keyboard**: Show keyboard
- **Window → Show Device Bezels**: Show notch

## Developer Resources

- [SwiftUI Tutorials](https://developer.apple.com/tutorials/swiftui)
- [MapKit Documentation](https://developer.apple.com/documentation/mapkit)
- [iOS Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)

## Need Help?

### Quick Troubleshooting

1. **Clean build folder**: Cmd+Shift+K
2. **Restart Xcode**: Sometimes needed
3. **Check target membership**: File Inspector → Target Membership
4. **Verify Info.plist**: Check location permissions
5. **Check build settings**: iOS Deployment Target = 15.0

### Documentation

- `README.md` - Full feature documentation
- `SETUP_GUIDE.md` - Detailed setup instructions
- `COMPARISON.md` - Android vs iOS comparison
- `FEATURES.md` - Feature implementation status

### Get Support

- Check the main repository README
- Open an issue on GitHub
- Tag with `ios` label

## Video Walkthrough (Conceptual)

If you prefer visual guidance:

1. **[0:00-2:00]** Create Xcode project
2. **[2:00-4:00]** Add source files
3. **[4:00-6:00]** Configure project
4. **[6:00-8:00]** Build and run
5. **[8:00-10:00]** Test features

## Success Indicators

You'll know setup is complete when:

- ✅ App builds without errors
- ✅ App launches in simulator
- ✅ Map is visible
- ✅ Tabs are clickable
- ✅ Location permission appears
- ✅ No crashes on navigation

## Minimum Viable Test

To verify everything works:

```
1. Launch app
2. Tap "Stations" tab
3. See list of stations
4. Tap any station
5. See station details

If this works, your setup is successful! ✅
```

## Production Checklist

Before deploying to App Store:

- [ ] Add app icons (all sizes)
- [ ] Configure bundle identifier
- [ ] Set up code signing
- [ ] Test on physical device
- [ ] Add screenshots
- [ ] Write app description
- [ ] Set privacy policy URL
- [ ] Submit for review

## Time Estimates

- Basic setup: **5-10 minutes**
- Testing all features: **15-20 minutes**
- Customization: **30-60 minutes**
- App Store prep: **2-4 hours**

## Final Notes

### This Quick Start Gets You:
- ✅ Working iOS app
- ✅ All features functional
- ✅ Ready for testing
- ✅ Ready for customization

### You Can Now:
- Run on simulator
- Test on device
- Modify code
- Deploy to users

## Congratulations! 🎊

You now have a fully functional iOS version of the Riyadh Transport app!

For more details, see:
- `README.md` - Complete documentation
- `SETUP_GUIDE.md` - Detailed setup
- `FEATURES.md` - Feature list

Happy coding! 🚀
