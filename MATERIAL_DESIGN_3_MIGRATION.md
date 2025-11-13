# Material Design 3 Migration Guide

## Overview

This document describes the Material Design 3 (Material You) migration implemented in this project, including backward compatibility with Android 7 (API 24) and dynamic color support for Android 12+ (API 31+).

## What Changed

### 1. Dependencies
- **Before**: `com.google.android.material:material:1.10.0` (Material Design 2)
- **After**: `com.google.android.material:material:1.11.0` (Material Design 3)

### 2. Theme System

#### Base Theme (Android 7-11)
- **Location**: `res/values/themes.xml`
- **Parent**: `Theme.Material3.DayNight`
- Uses static Material Design 3 color scheme
- Automatically adapts to system light/dark mode

#### Night Theme (Android 7-11)
- **Location**: `res/values-night/themes.xml`
- Uses dark color variants optimized for dark mode

#### Android 12+ Theme
- **Location**: `res/values-v31/themes.xml`
- **Special Feature**: Dynamic colors (Material You)
- Adapts to user's wallpaper colors automatically
- Falls back to static colors if dynamic colors unavailable

### 3. Color System

#### Material Design 3 Color Roles
The new color system uses semantic color roles instead of fixed colors:

**Light Theme Colors:**
- `md_theme_light_primary`: Green (#1aad00) - Main brand color
- `md_theme_light_secondary`: Blue (#1379c6) - Accent color
- `md_theme_light_tertiary`: Yellow-brown - Additional accent
- `md_theme_light_surface`: Background surfaces
- `md_theme_light_error`: Error states

**Dark Theme Colors:**
- `md_theme_dark_primary`: Light green (#82d867)
- `md_theme_dark_secondary`: Light blue (#8accff)
- All color variants optimized for dark backgrounds

**Legacy Colors Maintained:**
- `colorPrimary`, `colorAccent`, etc. for backward compatibility
- Metro line colors unchanged
- Custom app colors (bus, walk, etc.) preserved

### 4. Component Updates

#### Material Components
All Material components automatically inherit Material Design 3 styles:

- **MaterialCardView**: Inherits MD3 elevation and shape
- **MaterialButton**: Uses MD3 button styles
  - Updated: `Widget.MaterialComponents.Button.OutlinedButton` → `Widget.Material3.Button.OutlinedButton`
- **TextInputLayout**: Inherits MD3 text field styles
- **FloatingActionButton**: Inherits MD3 FAB styles
- **TabLayout**: Uses MD3 tab styles
- **Chip**: Uses MD3 chip styles

## How It Works

### Version-Specific Theming

The Android resource system automatically selects the appropriate theme:

1. **Android 7-11 (API 24-30)**: Uses `values/themes.xml` and `values-night/themes.xml`
2. **Android 12+ (API 31+)**: Uses `values-v31/themes.xml` with dynamic colors

### Dynamic Colors (Android 12+)

Material You's dynamic color system:
- Extracts colors from user's wallpaper
- Generates harmonious color palette
- Applies to all Material components automatically
- Provides fallback to static colors

### Backward Compatibility

The migration maintains full backward compatibility:

1. **Theme Hierarchy**: Uses `Theme.Material3.DayNight` which extends Material Components
2. **Legacy Color Names**: Preserved for existing code references
3. **Component Compatibility**: All existing Material components work seamlessly

## Testing Guidelines

### Visual Testing

Test on multiple Android versions to verify theming:

1. **Android 7-8 (API 24-26)**: Static colors, light/dark mode
2. **Android 9-11 (API 28-30)**: Static colors, enhanced dark mode
3. **Android 12+ (API 31+)**: Dynamic colors from wallpaper

### Test Cases

#### 1. Light/Dark Mode Switching
- Switch system theme between light and dark
- Verify all screens adapt correctly
- Check text contrast and readability

#### 2. Dynamic Colors (Android 12+ only)
- Change device wallpaper
- Verify app colors adapt to wallpaper
- Test with various wallpaper colors

#### 3. Component Appearance
- **Buttons**: Check filled, outlined, text variants
- **Cards**: Verify elevation and shape
- **Text Fields**: Test focus and error states
- **FABs**: Check size and color
- **Tabs**: Verify selection indicators

#### 4. Legacy Features
- Metro line colors should remain consistent
- Bus/walk colors should be unchanged
- All existing functionality should work

### Manual Testing Steps

1. **Install on Test Devices**:
   ```bash
   ./gradlew installDebug
   ```

2. **Test Light Mode**:
   - Open app in light mode
   - Navigate through all screens
   - Verify colors are readable and appropriate

3. **Test Dark Mode**:
   - Switch to dark mode in system settings
   - Verify dark theme applies
   - Check that text is visible

4. **Test Dynamic Colors (Android 12+)**:
   - Change wallpaper to a blue image
   - Verify app adopts blue-ish tones
   - Change wallpaper to a red image
   - Verify app adopts red-ish tones

## Design Principles

### Material Design 3 Key Features

1. **Personal**: Adapts to user's personal color preferences
2. **Expressive**: Refined UI with enhanced contrast and accessibility
3. **Adaptive**: Works across all screen sizes and form factors

### Color Roles

- **Primary**: Main brand color, used for key components
- **Secondary**: Accent color, used for less prominent components
- **Tertiary**: Additional accent for special cases
- **Error**: Error states and warnings
- **Surface**: Background surfaces (cards, sheets, etc.)
- **Outline**: Borders and dividers

## Troubleshooting

### Issue: Colors Don't Match Design
**Solution**: Verify you're testing on correct Android version. Dynamic colors only work on Android 12+.

### Issue: Dark Mode Not Working
**Solution**: Check that device is set to dark mode. Theme uses `DayNight` variant which auto-switches.

### Issue: Old Components Look Wrong
**Solution**: Update component styles to use `Widget.Material3.*` instead of `Widget.MaterialComponents.*`

### Issue: Build Errors
**Solution**: Ensure Material library version is 1.11.0 or higher and Gradle sync completed.

## Resources

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Material Components for Android](https://material.io/develop/android)
- [Dynamic Color Documentation](https://material.io/blog/material-you-dynamic-color)

## Future Enhancements

Potential improvements for future releases:

1. **Custom Color Schemes**: Allow users to choose from preset color themes
2. **High Contrast Mode**: Enhanced accessibility mode
3. **Animation Refinements**: Leverage MD3 motion system
4. **Component Customization**: Fine-tune specific component appearances

## Compatibility Matrix

| Android Version | Theme Used | Dynamic Colors | Notes |
|----------------|------------|----------------|-------|
| 7.0-7.1 (API 24-25) | Material3 Static | ❌ | Base MD3 support |
| 8.0-8.1 (API 26-27) | Material3 Static | ❌ | Full MD3 support |
| 9.0 (API 28) | Material3 Static | ❌ | Enhanced dark mode |
| 10 (API 29) | Material3 Static | ❌ | Gesture navigation |
| 11 (API 30) | Material3 Static | ❌ | Improved theming |
| 12+ (API 31+) | Material3 Dynamic | ✅ | Material You |

## Migration Checklist

When updating other Material components in the future:

- [ ] Check Material Design 3 guidelines for component
- [ ] Update style references to `Widget.Material3.*`
- [ ] Test on Android 7 for backward compatibility
- [ ] Test on Android 12+ for dynamic colors
- [ ] Verify light and dark mode appearance
- [ ] Update documentation if needed

## Code References

### Theme Files
- `app/src/main/res/values/themes.xml` - Base theme
- `app/src/main/res/values-night/themes.xml` - Dark theme
- `app/src/main/res/values-v31/themes.xml` - Android 12+ theme

### Color Files
- `app/src/main/res/values/colors.xml` - All color definitions
- `app/src/main/res/values-night/colors.xml` - Dark mode overrides

### Build Configuration
- `app/build.gradle` - Material library dependency

## Support

For issues or questions about the Material Design 3 implementation:
1. Check this documentation
2. Review Material Design 3 official documentation
3. Test on actual devices with different Android versions
4. Verify theme files are correctly structured
