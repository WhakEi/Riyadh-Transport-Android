package com.riyadhtransport.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;

public class LocaleHelper {

    /**
     * Check if the app is currently in Arabic locale
     */
    public static boolean isArabic(Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = config.getLocales().get(0);
        } else {
            locale = config.locale;
        }
        
        // Also check Locale.getDefault() as a fallback
        if (locale == null || locale.getLanguage().isEmpty()) {
            locale = Locale.getDefault();
        }
        
        boolean isAr = locale.getLanguage().equals("ar");
        android.util.Log.d("LocaleHelper", "isArabic check: locale=" + locale + ", language=" + locale.getLanguage() + ", result=" + isAr);
        return isAr;
    }

    /**
     * Get the language code for the current locale
     */
    public static String getLanguageCode(Context context) {
        return isArabic(context) ? "ar" : "en";
    }

    /**
     * Get the API path prefix based on current locale
     * Returns "/ar/" if Arabic, empty string otherwise
     */
    public static String getApiPrefix(Context context) {
        return isArabic(context) ? "ar/" : "";
    }
    
    /**
     * Update the configuration with the given locale
     */
    public static Context updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }
}
