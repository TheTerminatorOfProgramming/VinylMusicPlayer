package com.ttop.cassette.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.ttop.cassette.R;
import com.ttop.cassette.appshortcuts.DynamicShortcutManager;
import com.ttop.cassette.appwidgets.AppWidgetBig;
import com.ttop.cassette.appwidgets.AppWidgetClassic;
import com.ttop.cassette.appwidgets.AppWidgetFull;
import com.ttop.cassette.appwidgets.AppWidgetMini;
import com.ttop.cassette.databinding.ActivityPreferencesBinding;
import com.ttop.cassette.helper.MusicPlayerRemote;
import com.ttop.cassette.preferences.BlacklistPreference;
import com.ttop.cassette.preferences.BlacklistPreferenceDialog;
import com.ttop.cassette.preferences.LibraryPreference;
import com.ttop.cassette.preferences.LibraryPreferenceDialog;
import com.ttop.cassette.preferences.NowPlayingScreenPreference;
import com.ttop.cassette.preferences.NowPlayingScreenPreferenceDialog;
import com.ttop.cassette.preferences.PreAmpPreference;
import com.ttop.cassette.preferences.PreAmpPreferenceDialog;
import com.ttop.cassette.preferences.SmartPlaylistPreference;
import com.ttop.cassette.preferences.SmartPlaylistPreferenceDialog;
import com.ttop.cassette.service.MusicService;
import com.ttop.cassette.ui.activities.base.AbsBaseActivity;
import com.ttop.cassette.ui.activities.intro.AppIntroActivity;
import com.ttop.cassette.util.FileUtil;
import com.ttop.cassette.util.ImageTheme.ThemeStyleUtil;
import com.ttop.cassette.util.NavigationUtil;
import com.ttop.cassette.util.PreferenceUtil;
import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {

    Toolbar toolbar;
    private static final int IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityPreferencesBinding binding = ActivityPreferencesBinding.inflate(LayoutInflater.from(this));
        toolbar = binding.toolbar;
        setContentView(binding.getRoot());

        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final int title = dialog.getTitle();
        if (title == R.string.primary_color) {
            ThemeStore.editTheme(this)
                    .primaryColor(selectedColor)
                    .commit();
        } else if (title == R.string.accent_color) {
            ThemeStore.editTheme(this)
                    .accentColor(selectedColor)
                    .commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        MusicService musicService = MusicPlayerRemote.musicService;
        final AppWidgetClassic appWidgetClassic = AppWidgetClassic.getInstance();
        final AppWidgetBig appWidgetBig = AppWidgetBig.getInstance();
        final AppWidgetFull appWidgetFull = AppWidgetFull.getInstance();
        final AppWidgetMini appWidgetMini = AppWidgetMini.getInstance();
        ArrayList<String> AppName = new ArrayList<>();

        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_library);
            addPreferencesFromResource(R.xml.pref_theme);
            addPreferencesFromResource(R.xml.pref_colors);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_playlists);
            addPreferencesFromResource(R.xml.pref_advanced);
            addPreferencesFromResource(R.xml.pref_about);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                addPreferencesFromResource(R.xml.pref_lockscreen);
            }

            // set summary for whitelist, in order to indicate start directory
            final String strSummaryWhitelist = getString(R.string.pref_summary_whitelist);
            final File startDirectory = PreferenceUtil.getInstance().getStartDirectory();
            final String startPath = FileUtil.safeGetCanonicalPath(startDirectory);
            findPreference(PreferenceUtil.WHITELIST_ENABLED).setSummary(strSummaryWhitelist + startPath);

        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            } else if (preference instanceof LibraryPreference) {
                return LibraryPreferenceDialog.newInstance();
            } else if (preference instanceof PreAmpPreference) {
                return PreAmpPreferenceDialog.newInstance();
            } else if (preference instanceof SmartPlaylistPreference) {
                return SmartPlaylistPreferenceDialog.newInstance(preference.getKey());
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            invalidateSettings();
            PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        }

        private void invalidateSettings() {

            final Preference intro = findPreference("intro");
            intro.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AppIntroActivity.class));
                return false;
            });

            final TwoStatePreference lockscreenArt = findPreference("album_art_on_lockscreen");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                if (lockscreenArt != null) {
                    lockscreenArt.setChecked(false);
                    lockscreenArt.setEnabled(false);
                }
            }

            final TwoStatePreference widget_override = findPreference("widget_override");
            widget_override.setChecked(PreferenceUtil.getInstance().getWidgetOverride());
            widget_override.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setWidgetOverride((Boolean) newValue);

                appWidgetClassic.notifyThemeChange(musicService);
                appWidgetBig.notifyThemeChange(musicService);
                appWidgetFull.notifyThemeChange(musicService);
                appWidgetMini.notifyThemeChange(musicService);
                return true;
            });

            final ListPreference podcast = findPreference("podcast");
            assert podcast != null;
            podcast.setEnabled(false);
            podcast.setVisible(false);
/*
            if (!PreferenceUtil.getInstance().checkPreferences("podcast"))
            {
                podcast.setSummary(R.string.pref_summary_podcast);
            }
            else
            {
                podcast.setSummary(PreferenceUtil.getInstance().getPodcastApp());
            }

            List<ApplicationInfo> packages = Util.getInstalledApplication(getContext());

            for (ApplicationInfo packageInfo : packages) {
                AppName.add(Util.getAppNameFromPkgName(getContext(), packageInfo.packageName));
            }

            AppName.removeAll(Arrays.asList(null, ""));

            Collections.sort(AppName);

            CharSequence entries[] = new String[AppName.size()];

            entries[0] = "None";

            for (int i = 1; i < AppName.size(); i++) {
                entries[i] = AppName.get(i);
            }

            podcast.setEntries(entries);
            podcast.setEntryValues(entries);

            podcast.setOnPreferenceChangeListener((preference, newValue) -> {
                String app = (String) newValue;

                PreferenceUtil.getInstance().setPodcastApp(app);

                podcast.setSummary(app);
                PreferenceUtil.getInstance().setShouldRecreate(true);
                return true;
            });
 */

            final TwoStatePreference pause_zero = findPreference("pause_on_zero");
            pause_zero.setChecked(PreferenceUtil.getInstance().getPauseZero());
            pause_zero.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setPauseZero((Boolean) newValue);
                return true;
            });

            final TwoStatePreference bluetooth = findPreference("bluetooth_autoplay");
            bluetooth.setChecked(PreferenceUtil.getInstance().getBluetoothAutoplay());
            bluetooth.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setBluetoothAutoplay((Boolean) newValue);
                return true;
            });

            final ListPreference delay = findPreference("bluetooth_delay");
            if (!PreferenceUtil.getInstance().checkPreferences("bluetooth_delay"))
            {
                delay.setSummary(R.string.pref_summary_bluetooth_playback_delay);
            }
            else
            {
                delay.setSummary(PreferenceUtil.getInstance().getBluetoothDelay() + " Seconds");
            }

            String[] vals = new String[6];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = String.valueOf(i);
            }

            delay.setEntries(vals);
            delay.setEntryValues(vals);
            delay.setDefaultValue("2");

            delay.setOnPreferenceChangeListener((preference, newValue) -> {
                String secs = (String) newValue;

                PreferenceUtil.getInstance().setBluetoothDelay(secs);

                delay.setSummary(secs + " Seconds");
                PreferenceUtil.getInstance().setShouldRecreate(true);
                return true;
            });

            final TwoStatePreference headset = findPreference("headset_autoplay");
            headset.setChecked(PreferenceUtil.getInstance().getHeadsetAutoplay());
            headset.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setHeadsetAutoplay((Boolean) newValue);
                return true;
            });

            final TwoStatePreference expand_panel = findPreference("show_now_playing");
            expand_panel.setChecked(PreferenceUtil.getInstance().getExpandPanel());
            expand_panel.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setExpandPanel((Boolean) newValue);
                return true;
            });

            final TwoStatePreference extra_controls = findPreference("extra_controls");
            extra_controls.setChecked(PreferenceUtil.getInstance().getExtraControls());
            extra_controls.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setExtraControls((Boolean) newValue);
                PreferenceUtil.getInstance().setShouldRecreate(true);
                return true;
            });

            final TwoStatePreference next_prev = findPreference("next_prev_buttons");
            next_prev.setChecked(PreferenceUtil.getInstance().getNextPrevButtons());
            next_prev.setOnPreferenceChangeListener((preference, newValue) -> {
                PreferenceUtil.getInstance().setNextPrevButtons((Boolean) newValue);
                PreferenceUtil.getInstance().setShouldRecreate(true);
                return true;
            });

            final Preference app_info = findPreference("app_info");
            app_info.setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getContext().getPackageName()));
                startActivity(i);
                return false;
            });

            final Preference battery = findPreference("optimization");
            battery.setOnPreferenceClickListener(preference -> {
                PowerManager pm = (PowerManager) getContext().getSystemService(POWER_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (pm != null && !pm.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                            startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST);
                        }
                    } else {
                        Intent myIntent = new Intent();
                        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(myIntent);
                    }
                }
                return false;
            });

            final Preference about = findPreference("about");
            about.setOnPreferenceClickListener(preference -> {
                new Handler().postDelayed(() -> startActivity(new Intent(getActivity(), AboutActivity.class)), 200);
                return false;
            });

            final Preference generalTheme = findPreference(PreferenceUtil.GENERAL_THEME);
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, o) -> {
                String themeName = (String) o;

                setSummary(generalTheme, o);

                PreferenceUtil.getInstance().setAppTheme(themeName);

                appWidgetClassic.notifyThemeChange(musicService);
                appWidgetBig.notifyThemeChange(musicService);
                appWidgetFull.notifyThemeChange(musicService);
                appWidgetMini.notifyThemeChange(musicService);

                if (getActivity() != null) {
                    ThemeStore.markChanged(getActivity());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    // Set the new theme so that updateAppShortcuts can pull it
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));

                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }

                getActivity().recreate();
                return true;
            });

            final Preference themeStyle = findPreference("theme_style");
            themeStyle.setOnPreferenceChangeListener((preference, o) -> {
                ThemeStyleUtil.updateInstance(PreferenceUtil.getThemeStyleFromPrefValue((String) o));
                if (getActivity() != null) {
                    ThemeStore.markChanged(getActivity());
                }

                return true;
            });

            final Preference autoDownloadImagesPolicy = findPreference(PreferenceUtil.AUTO_DOWNLOAD_IMAGES_POLICY);
            setSummary(autoDownloadImagesPolicy);
            autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(autoDownloadImagesPolicy, o);
                return true;
            });

            final ATEColorPreference primaryColorPref = findPreference("primary_color");
            if (getActivity() != null) {
                final int primaryColor = ThemeStore.primaryColor(getActivity());
                primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
                primaryColorPref.setOnPreferenceClickListener(preference -> {
                    new ColorChooserDialog.Builder(getActivity(), R.string.primary_color)
                            .accentMode(false)
                            .allowUserColorInput(true)
                            .allowUserColorInputAlpha(false)
                            .preselect(primaryColor)
                            .show(getActivity());
                    return true;
                });
            }

            final ATEColorPreference accentColorPref = findPreference("accent_color");
            final int accentColor = ThemeStore.accentColor(getActivity());
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(getActivity(), R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show(getActivity());
                return true;
            });

            TwoStatePreference colorNavBar = findPreference("should_color_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setVisible(false);
            } else {
                colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
                colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredNavigationBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }

            final TwoStatePreference classicNotification = findPreference(PreferenceUtil.CLASSIC_NOTIFICATION);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance().classicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setClassicNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference coloredNotification = findPreference(PreferenceUtil.COLORED_NOTIFICATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                coloredNotification.setEnabled(PreferenceUtil.getInstance().classicNotification());
            } else {
                coloredNotification.setChecked(PreferenceUtil.getInstance().coloredNotification());
                coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setColoredNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference colorAppShortcuts = findPreference("should_color_app_shortcuts");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance().coloredAppShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance().setColoredAppShortcuts((Boolean) newValue);

                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            final Preference equalizer = findPreference("equalizer");
            if (!hasEqualizer()) {
                equalizer.setEnabled(false);
                equalizer.setSummary(getResources().getString(R.string.no_equalizer));
            }
            equalizer.setOnPreferenceClickListener(preference -> {
                NavigationUtil.openEqualizer(getActivity());
                return true;
            });

            if (PreferenceUtil.getInstance().getReplayGainSourceMode() == PreferenceUtil.RG_SOURCE_MODE_NONE) {
                Preference pref = findPreference("replaygain_preamp");
                pref.setEnabled(false);
                pref.setSummary(getResources().getString(R.string.pref_rg_disabled));
            }

            updateNowPlayingScreenSummary();
            updatePlaylistsSummary();
        }

        private boolean hasEqualizer() {
            final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            if (getActivity() != null) {
                PackageManager pm = getActivity().getPackageManager();
                ResolveInfo ri = pm.resolveActivity(effects, 0);
                return ri != null;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                    updateNowPlayingScreenSummary();
                    break;
                case PreferenceUtil.CLASSIC_NOTIFICATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        findPreference("colored_notification").setEnabled(sharedPreferences.getBoolean(key, false));
                    }
                    break;
                case PreferenceUtil.RG_SOURCE_MODE_V2:
                    Preference pref = findPreference("replaygain_preamp");
                    if (!sharedPreferences.getString(key, "none").equals("none")) {
                        pref.setEnabled(true);
                        pref.setSummary(R.string.pref_summary_rg_preamp);
                    } else {
                        pref.setEnabled(false);
                        pref.setSummary(getResources().getString(R.string.pref_rg_disabled));
                    }
                    break;
                case PreferenceUtil.WHITELIST_ENABLED:
                    getContext().sendBroadcast(new Intent(MusicService.MEDIA_STORE_CHANGED));
                    break;
                case PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2:
                case PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2:
                case PreferenceUtil.LAST_ADDED_CUTOFF_V2:
                    updatePlaylistsSummary();
                    break;
            }
        }

        private void updateNowPlayingScreenSummary() {
            findPreference(PreferenceUtil.NOW_PLAYING_SCREEN_ID).setSummary(PreferenceUtil.getInstance().getNowPlayingScreen().titleRes);
        }

        private void updatePlaylistsSummary() {
            final Context context = getContext();
            final PreferenceUtil preferenceUtil = PreferenceUtil.getInstance();

            findPreference(PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getRecentlyPlayedCutoffText(context));
            findPreference(PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getNotRecentlyPlayedCutoffText(context));
            findPreference(PreferenceUtil.LAST_ADDED_CUTOFF_V2)
                    .setSummary(preferenceUtil.getLastAddedCutoffText(context));
        }
    }
}
