package com.ttop.cassette.util.ImageTheme;


import com.ttop.cassette.util.PreferenceUtil;

public class ThemeStyleUtil {
    private static ThemeStyle sInstance;

    public static ThemeStyle updateInstance(int themeState) {
        synchronized (ThemeStyleUtil.class) {
            if (themeState == PreferenceUtil.ROUNDED_THEME) {
                sInstance = new MaterialTheme();
            } else {
                sInstance = new FlatTheme();
            }
            return sInstance;
        }
    }

    public static synchronized ThemeStyle getInstance() {
        if (sInstance == null) {
            return updateInstance(PreferenceUtil.getInstance().getThemeStyle());
        }
        return sInstance;
    }
}