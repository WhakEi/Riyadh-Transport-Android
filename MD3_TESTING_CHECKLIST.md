# Material Design 3 Testing Checklist

## Pre-Build Verification

### Code Review
- [x] Material library updated to 1.11.0+ in `app/build.gradle`
- [x] Base theme changed to `Theme.Material3.DayNight` in `values/themes.xml`
- [x] Dark theme created in `values-night/themes.xml`
- [x] Android 12+ theme with dynamic colors in `values-v31/themes.xml`
- [x] MD3 color scheme implemented with all color roles
- [x] Legacy colors maintained for backward compatibility
- [x] Material component styles updated to `Widget.Material3.*`
- [x] Drawables updated to use theme colors

## Build Verification

### Gradle Build
```bash
./gradlew clean assembleDebug
```
**Expected Result**: Build succeeds without errors

### Lint Check
```bash
./gradlew lint
```
**Expected Result**: No new Material-related warnings

## Runtime Testing

### Test Matrix

| Test Case | Android 7 | Android 8-11 | Android 12+ |
|-----------|-----------|--------------|-------------|
| Light Mode | ✓ Required | ✓ Required | ✓ Required |
| Dark Mode | ✓ Required | ✓ Required | ✓ Required |
| Dynamic Colors | N/A | N/A | ✓ Required |
| Theme Switch | ✓ Required | ✓ Required | ✓ Required |

### 1. Android 7 (API 24-25) Testing

**Device**: Android 7.x device or emulator

#### Light Mode
- [ ] Launch app in light mode
- [ ] Verify primary green color on FABs and action buttons
- [ ] Check bottom sheet has proper background
- [ ] Test tab navigation - tabs should have proper selection indicator
- [ ] Verify MaterialCardView components have elevation
- [ ] Check TextInputLayout fields have proper outline
- [ ] Verify text is readable on all backgrounds

#### Dark Mode
- [ ] Switch device to dark mode
- [ ] Restart app or wait for auto-switch
- [ ] Verify dark background throughout app
- [ ] Check text colors are light and readable
- [ ] Verify cards have darker background than screen
- [ ] Test all interactive components are visible

#### Component Testing
- [ ] **FloatingActionButton**: Green primary, white icon
- [ ] **MaterialButton (Filled)**: Green background, white text
- [ ] **MaterialButton (Outlined)**: Green outline, green text
- [ ] **MaterialCardView**: Elevated, proper corner radius
- [ ] **TabLayout**: Proper selection indicator
- [ ] **TextInputLayout**: Outlined style, proper focus state
- [ ] **Bottom Sheet**: Smooth drag behavior, proper handle

### 2. Android 8-11 (API 26-30) Testing

**Device**: Android 8-11 device or emulator

Repeat all Android 7 tests, plus:

#### Additional Checks
- [ ] Verify enhanced dark mode (deeper blacks if available)
- [ ] Test gesture navigation compatibility (if applicable)
- [ ] Check status bar color matches theme
- [ ] Verify navigation bar respects theme

### 3. Android 12+ (API 31+) Testing

**Device**: Android 12+ device or emulator

#### Static Colors (Before Wallpaper Change)
- [ ] Launch app with default wallpaper
- [ ] Verify theme uses either wallpaper colors or fallback to green
- [ ] Check all components inherit theme colors

#### Dynamic Colors Testing
1. **Blue Wallpaper Test**
   - [ ] Change device wallpaper to predominantly blue image
   - [ ] Return to app (may need to restart)
   - [ ] Verify primary colors shift toward blue tones
   - [ ] Check buttons, FABs, and tabs use blue-ish colors
   - [ ] Verify text contrast remains good

2. **Red Wallpaper Test**
   - [ ] Change device wallpaper to predominantly red image
   - [ ] Return to app (may need to restart)
   - [ ] Verify primary colors shift toward red tones
   - [ ] Check all components adapt to red theme

3. **Green Wallpaper Test**
   - [ ] Change device wallpaper to predominantly green image
   - [ ] Return to app (may need to restart)
   - [ ] Verify colors stay close to original design
   - [ ] Should feel natural with existing green brand

4. **Neutral Wallpaper Test**
   - [ ] Change device wallpaper to grayscale image
   - [ ] Verify fallback to static colors
   - [ ] Check theme remains coherent

#### Dynamic Color Verification
- [ ] Colors change based on wallpaper
- [ ] Text remains readable in all color schemes
- [ ] Metro line colors stay consistent (not affected by dynamic colors)
- [ ] Alerts and error states remain clear

### 4. Theme Switching

On all Android versions:

#### Light to Dark
- [ ] Launch app in light mode
- [ ] Navigate to multiple screens
- [ ] Switch device to dark mode
- [ ] Verify app switches to dark theme automatically
- [ ] Check all screens maintain proper theming
- [ ] Verify no layout shifts or visual glitches

#### Dark to Light
- [ ] Launch app in dark mode
- [ ] Navigate to multiple screens
- [ ] Switch device to light mode
- [ ] Verify app switches to light theme automatically
- [ ] Check all screens maintain proper theming

### 5. Screen-by-Screen Testing

#### Main Activity
- [ ] Map displays correctly
- [ ] Bottom sheet has rounded corners at top
- [ ] Pull handle is visible and functional
- [ ] FABs are properly colored (green primary, blue secondary)
- [ ] Tab navigation works smoothly
- [ ] Tab selection indicator is visible

#### Route Fragment
- [ ] TextInputLayout fields have proper outline
- [ ] "Find Route" button is filled with primary color
- [ ] "Use My Location" button has outlined style
- [ ] Loading spinner matches theme
- [ ] Route segments display properly in cards

#### Stations Fragment
- [ ] Search field has proper styling
- [ ] Station list items are readable
- [ ] Card elevations are appropriate
- [ ] Scroll behavior is smooth

#### Lines Fragment
- [ ] Search field has proper styling
- [ ] Metro line colors remain consistent
- [ ] Line cards display properly
- [ ] Selection feedback is clear

#### Station Details Activity
- [ ] Toolbar has proper color
- [ ] Station name is readable
- [ ] Chip component uses theme colors
- [ ] Arrival information is clear
- [ ] "Show on Map" button has outlined style

#### Favorites Activity
- [ ] Favorite items display in cards
- [ ] Interactive elements are properly styled
- [ ] Empty state is visible

#### Search Location Activity
- [ ] Search field is prominent
- [ ] Results display clearly
- [ ] Selection feedback works

### 6. Component-Specific Tests

#### MaterialButton
- [ ] **Filled**: Primary color background, white text
- [ ] **Outlined**: Primary color border and text
- [ ] **Text**: Primary color text only
- [ ] Touch ripple effect visible
- [ ] Disabled state is clear

#### MaterialCardView
- [ ] Proper elevation shadow
- [ ] Corner radius is 8dp
- [ ] Background adapts to theme
- [ ] Touch ripple for clickable cards

#### FloatingActionButton
- [ ] Proper size (normal)
- [ ] Primary/secondary colors as specified
- [ ] Icon is centered and properly tinted
- [ ] Elevation shadow visible
- [ ] Touch feedback works

#### TextInputLayout
- [ ] Outline style applied
- [ ] Label floats on focus
- [ ] Start icons display correctly
- [ ] Error state is red and clear
- [ ] Focus color matches theme

#### TabLayout
- [ ] Fixed mode with 3 tabs
- [ ] Selection indicator is visible
- [ ] Swipe between tabs works
- [ ] Text labels are readable

#### BottomSheet
- [ ] Drag handle is visible
- [ ] Peek height is appropriate (500dp)
- [ ] Expands smoothly when dragged
- [ ] Collapses smoothly when dragged down
- [ ] Cannot be completely hidden

### 7. Accessibility Testing

- [ ] Text contrast meets WCAG AA standards in light mode
- [ ] Text contrast meets WCAG AA standards in dark mode
- [ ] Touch targets are at least 48x48dp
- [ ] Content descriptions present for icon buttons
- [ ] TalkBack navigation works properly
- [ ] Font scales properly with system font size

### 8. Edge Cases

#### Low Light Conditions
- [ ] Dark mode is comfortable in low light
- [ ] No overly bright elements
- [ ] Text remains readable

#### High Ambient Light
- [ ] Light mode is readable in bright conditions
- [ ] Sufficient contrast on all elements

#### Color Blindness
- [ ] Not relying solely on color for information
- [ ] Metro lines have labels, not just colors
- [ ] Icons supplement color coding

### 9. Performance Testing

- [ ] Theme switching is instant
- [ ] No lag when changing modes
- [ ] No memory leaks from theme changes
- [ ] Smooth animations and transitions

### 10. Backward Compatibility Verification

#### Legacy Color References
- [ ] `R.color.colorPrimary` still works in Java code
- [ ] `R.color.colorAccent` still works in Java code
- [ ] Metro line colors unchanged
- [ ] Custom colors (bus, walk) unchanged

#### Legacy Code
- [ ] LineColorHelper.java works correctly
- [ ] SearchResultAdapter color references work
- [ ] All existing features functional

## Regression Testing

### Core Features
- [ ] Route planning works
- [ ] Station search works
- [ ] Map interaction works
- [ ] Favorites work
- [ ] Language switching works
- [ ] Location services work
- [ ] API calls succeed

### UI Interactions
- [ ] All buttons are clickable
- [ ] Text fields accept input
- [ ] Lists scroll smoothly
- [ ] Navigation between screens works
- [ ] Back button works correctly

## Issue Tracking

### Known Issues
Document any issues found during testing:

| Issue | Android Version | Severity | Status | Notes |
|-------|----------------|----------|--------|-------|
| | | | | |

### Issue Template
When logging issues:
1. **Description**: What is the problem?
2. **Steps to Reproduce**: How to recreate?
3. **Expected Behavior**: What should happen?
4. **Actual Behavior**: What actually happens?
5. **Android Version**: Which version(s) affected?
6. **Screenshots**: Include if possible

## Sign-off

### Testing Completed By
- **Name**: _________________
- **Date**: _________________
- **Android 7 Device**: _________________
- **Android 12+ Device**: _________________

### Results Summary
- [ ] All critical tests passed
- [ ] No blocking issues
- [ ] Ready for release

### Notes
_Add any additional observations or concerns_

---

## Quick Test Script

For rapid verification, run through this quick checklist:

1. [ ] Install on Android 7 device → Light mode → All screens load
2. [ ] Switch to dark mode → All screens adapt
3. [ ] Install on Android 12+ device → Change wallpaper → Colors adapt
4. [ ] Test all buttons and inputs → All functional
5. [ ] Check metro line colors → Remain consistent
6. [ ] Verify backward compatibility → No legacy code broken

**Result**: ✅ PASS / ❌ FAIL

If FAIL, refer to detailed checklist above for specific issue tracking.
