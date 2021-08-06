package com.ttop.cassette.ui.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ttop.cassette.R;
import com.ttop.cassette.databinding.ActivityMainContentBinding;
import com.ttop.cassette.databinding.ActivityMainDrawerLayoutBinding;
import com.ttop.cassette.databinding.SlidingMusicPanelLayoutBinding;
import com.ttop.cassette.dialogs.ChangelogDialog;
import com.ttop.cassette.discog.Discography;
import com.ttop.cassette.helper.MusicPlayerRemote;
import com.ttop.cassette.helper.SearchQueryHelper;
import com.ttop.cassette.loader.AlbumLoader;
import com.ttop.cassette.loader.ArtistLoader;
import com.ttop.cassette.loader.PlaylistSongLoader;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.service.MusicService;
import com.ttop.cassette.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.ttop.cassette.ui.activities.intro.AppIntroActivity;
import com.ttop.cassette.ui.fragments.mainactivity.library.LibraryFragment;
import com.ttop.cassette.util.PreferenceUtil;

import java.util.ArrayList;

public class MainActivity extends AbsSlidingMusicPanelActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int APP_INTRO_REQUEST = 100;

    ;
    public static final String EXPAND_PANEL = "expand_panel";

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    private boolean blockRequestPermissions;
    private boolean scanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();

        setCurrentFragment(LibraryFragment.newInstance());

        if (!checkShowIntro()) {
            showChangelog();
        }

        final Discography discog = Discography.getInstance();
        discog.startService(this);
        addMusicServiceEventListener(discog);

        PreferenceUtil.getInstance().setShouldRecreate(false);

        if (!PreferenceUtil.getInstance().checkPreferences("pause_on_zero")){
            PreferenceUtil.getInstance().setPauseZero(true);
        }

        if (!PreferenceUtil.getInstance().checkPreferences("bluetooth_autoplay")){
            PreferenceUtil.getInstance().setBluetoothAutoplay(true);
        }

        if (!PreferenceUtil.getInstance().checkPreferences("SHUFFLE_MODE")){
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putInt("SHUFFLE_MODE", 1)
                    .apply();
        }
    }

    @Override
    protected void onDestroy() {
        final Discography discog = Discography.getInstance();
        removeMusicServiceEventListener(discog);
        discog.stopService();

        super.onDestroy();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false;
            if (!hasPermissions()) {
                requestPermissions();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Settings")
                    .setMessage("Do You Want To Setup The Settings Now?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)))
                    .setNegativeButton("No", (dialog, id) -> {

                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions();
    }

    @Override
    protected View createContentView() {
        ActivityMainDrawerLayoutBinding binding = ActivityMainDrawerLayoutBinding.inflate(getLayoutInflater());

        ViewGroup drawerContent = binding.drawerContentContainer;

        SlidingMusicPanelLayoutBinding slidingPanelBinding = createSlidingMusicPanel();
        ActivityMainContentBinding.inflate(
                getLayoutInflater(),
                slidingPanelBinding.contentContainer,
                true);
        drawerContent.addView(slidingPanelBinding.getRoot());

        return binding.getRoot();
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        handlePlaybackIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final ArrayList<Song> songs = SearchQueryHelper.getSongs(intent.getExtras());
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true);
            }
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                ArrayList<Song> songs = PlaylistSongLoader.getPlaylistSongList(this, id);
                MusicPlayerRemote.openQueue(songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(id).songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                // TODO ArtistId might be not usable if it's sent by another app
                //      Discography (used by ArtistLoader) has an internal ID
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistLoader.getArtist(id).getSongs(), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
    }

    private boolean checkShowIntro() {
        if (!PreferenceUtil.getInstance().introShown()) {
            PreferenceUtil.getInstance().setIntroShown();
            ChangelogDialog.setChangelogRead(this);
            blockRequestPermissions = true;
            new Handler().postDelayed(() -> startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), APP_INTRO_REQUEST), 50);
            return true;
        }
        return false;
    }

    public boolean isNotScanning() {
        return !scanning;
    }

    public void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    private void showChangelog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int currentVersion = pInfo.versionCode;
            if (currentVersion != PreferenceUtil.getInstance().getLastChangelogVersion()) {
                ChangelogDialog.create().show(getSupportFragmentManager(), "CHANGE_LOG_DIALOG");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void reload() {
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PreferenceUtil.getInstance().getShouldRecreate()){
            postRecreate();
            PreferenceUtil.getInstance().setShouldRecreate(false);
        }

        if (PreferenceUtil.getInstance().getCurrentPlay()){
            MusicPlayerRemote.resumePlaying();
            PreferenceUtil.getInstance().setCurrentPlay(false);
        }

        if (getIntent().hasExtra(EXPAND_PANEL) &&
                getIntent().getBooleanExtra(EXPAND_PANEL, false) &&
                PreferenceUtil.getInstance().getExpandPanel()
        ) {
            expandPanel();
            getIntent().removeExtra(EXPAND_PANEL);
        }
    }
}
