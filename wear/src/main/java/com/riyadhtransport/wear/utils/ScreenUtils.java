package com.riyadhtransport.wear.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {
    
    /**
     * Get the screen width in pixels
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
    
    /**
     * Get the screen height in pixels
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
    
    /**
     * Check if the screen is round
     */
    public static boolean isScreenRound(Context context) {
        return context.getResources().getConfiguration().isScreenRound();
    }
    
    /**
     * Get the usable circular diameter for round screens
     * This calculates the inscribed circle within the screen rectangle
     */
    public static int getCircularDiameter(Context context) {
        int width = getScreenWidth(context);
        int height = getScreenHeight(context);
        return Math.min(width, height);
    }
    
    /**
     * Get the usable circular radius for round screens
     */
    public static int getCircularRadius(Context context) {
        return getCircularDiameter(context) / 2;
    }
    
    /**
     * Convert dp to pixels
     */
    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }
    
    /**
     * Convert pixels to dp
     */
    public static float pxToDp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / metrics.density;
    }
    
    /**
     * Get a scaled text size based on screen size
     * Base size is for 320px diameter screens (typical wear OS)
     */
    public static float getScaledTextSize(Context context, float baseSize) {
        int diameter = getCircularDiameter(context);
        float scale = diameter / 320f; // 320 is baseline
        return baseSize * scale;
    }
    
    /**
     * Get scaled dimension based on screen size
     */
    public static int getScaledDimension(Context context, int baseDimension) {
        int diameter = getCircularDiameter(context);
        float scale = diameter / 320f; // 320 is baseline
        return Math.round(baseDimension * scale);
    }
    
    /**
     * Get inset for circular screens (chin and sides)
     */
    public static int getCircularInset(Context context) {
        if (isScreenRound(context)) {
            int diameter = getCircularDiameter(context);
            // Approximate 15% inset for safe area
            return (int) (diameter * 0.15f);
        }
        return 0;
    }
}
