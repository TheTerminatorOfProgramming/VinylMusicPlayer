package com.ttop.cassette;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.ttop.cassette.appshortcuts.DynamicShortcutManager;
import com.ttop.cassette.discog.Discography;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends MultiDexApplication {
    public static final String TAG = App.class.getSimpleName();

    private static App app;

    private static Context context;

    private static Discography discography = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        context = getApplicationContext();

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.md_red_700)
                    .accentColorRes(R.color.md_blue_700)
                    .commit();
        }

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        // setup discography
        discography = new Discography();
    }

    public static App getInstance() {
        return app;
    }

    public static Context getStaticContext() {
        return context;
    }

    @NonNull
    public static Discography getDiscography() {
        return discography;
    }
}
