package com.ttop.cassette.ui.fragments.mainactivity.library;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialcab.MaterialCab;
import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.TabLayoutUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.ttop.cassette.R;
import com.ttop.cassette.adapter.MusicLibraryPagerAdapter;
import com.ttop.cassette.databinding.FragmentLibraryBinding;
import com.ttop.cassette.dialogs.CreatePlaylistDialog;
import com.ttop.cassette.discog.Discography;
import com.ttop.cassette.helper.SortOrder;
import com.ttop.cassette.interfaces.CabHolder;
import com.ttop.cassette.ui.activities.MainActivity;
import com.ttop.cassette.ui.activities.SearchActivity;
import com.ttop.cassette.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.ttop.cassette.ui.fragments.mainactivity.library.pager.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import com.ttop.cassette.ui.fragments.mainactivity.library.pager.AlbumsFragment;
import com.ttop.cassette.ui.fragments.mainactivity.library.pager.ArtistsFragment;
import com.ttop.cassette.ui.fragments.mainactivity.library.pager.PlaylistsFragment;
import com.ttop.cassette.ui.fragments.mainactivity.library.pager.SongsFragment;
import com.ttop.cassette.util.CassetteColorUtil;
import com.ttop.cassette.util.PreferenceUtil;
import com.ttop.cassette.util.Util;

public class LibraryFragment extends AbsMainActivityFragment implements CabHolder, MainActivity.MainActivityFragmentCallbacks, ViewPager.OnPageChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {
    Toolbar toolbar;
    TabLayout tabs;
    AppBarLayout appbar;
    ViewPager pager;
    private MusicLibraryPagerAdapter pagerAdapter;
    private MaterialCab cab;

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
    }

    public LibraryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentLibraryBinding binding = FragmentLibraryBinding.inflate(inflater, container, false);
        toolbar = binding.toolbar;
        tabs = binding.tabs;
        appbar = binding.appbar;
        pager = binding.pager;
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        PreferenceUtil.getInstance().unregisterOnSharedPreferenceChangedListener(this);
        super.onDestroyView();
        pager.removeOnPageChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        PreferenceUtil.getInstance().registerOnSharedPreferenceChangedListener(this);
        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpToolbar();
        setUpViewPager();
        checkOrientation();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (PreferenceUtil.LIBRARY_CATEGORIES.equals(key)) {
            Fragment current = getCurrentFragment();
            pagerAdapter.setCategoryInfos(PreferenceUtil.getInstance().getLibraryCategoryInfos());
            pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);
            int position = pagerAdapter.getItemPosition(current);
            if (position < 0) position = 0;
            pager.setCurrentItem(position);
            PreferenceUtil.getInstance().setLastPage(position);

            checkOrientation();

            // hide the tab bar with single tab
            tabs.setVisibility(pagerAdapter.getCount() == 1 ? View.GONE : View.VISIBLE);
        }
    }

    private void setUpToolbar() {
        int primaryColor = ThemeStore.primaryColor(getActivity());
        appbar.setBackgroundColor(primaryColor);
        toolbar.setBackgroundColor(primaryColor);
        toolbar.setNavigationIcon(R.drawable.ic_settings_white_24dp);

        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(toolbar);
    }

    private void setUpViewPager() {
        pagerAdapter = new MusicLibraryPagerAdapter(getActivity(), getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        tabs.setupWithViewPager(pager);

        int primaryColor = ThemeStore.primaryColor(getActivity());
        int normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(getActivity(), primaryColor);
        int selectedColor = ToolbarContentTintHelper.toolbarTitleColor(getActivity(), primaryColor);
        TabLayoutUtil.setTabIconColors(tabs, normalColor, selectedColor);
        tabs.setTabTextColors(normalColor, selectedColor);
        tabs.setSelectedTabIndicatorColor(ThemeStore.accentColor(getActivity()));

        updateTabVisibility();

        if (PreferenceUtil.getInstance().rememberLastTab()) {
            pager.setCurrentItem(PreferenceUtil.getInstance().getLastPage());
        }
        pager.addOnPageChangeListener(this);
    }

    private void updateTabVisibility() {
        // hide the tab bar when only a single tab is visible
        tabs.setVisibility(pagerAdapter.getCount() == 1 ? View.GONE : View.VISIBLE);
    }

    public Fragment getCurrentFragment() {
        return pagerAdapter.getFragment(pager.getCurrentItem());
    }

    private boolean isPlaylistPage() {
        return getCurrentFragment() instanceof PlaylistsFragment;
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menuRes, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(getMainActivity(), R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(CassetteColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(getActivity())))
                .setPopupMenuTheme(PreferenceUtil.getInstance().getGeneralTheme())
                .start(callback);
        return cab;
    }

    public void addOnAppBarOffsetChangedListener(AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
        appbar.addOnOffsetChangedListener(onOffsetChangedListener);
    }

    public void removeOnAppBarOffsetChangedListener(AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
        appbar.removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    public int getTotalAppBarScrollingRange() {
        return appbar.getTotalScrollRange();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (pager == null) return;
        inflater.inflate(R.menu.menu_main, menu);
        if (isPlaylistPage()) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.new_playlist_title);
        }
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment && currentFragment.isAdded()) {
            AbsLibraryPagerRecyclerViewCustomGridSizeFragment absLibraryRecyclerViewCustomGridSizeFragment = (AbsLibraryPagerRecyclerViewCustomGridSizeFragment) currentFragment;

            MenuItem gridSizeItem = menu.findItem(R.id.action_grid_size);
            if (Util.isLandscape(getResources())) {
                gridSizeItem.setTitle(R.string.action_grid_size_land);
            }
            setUpGridSizeMenu(absLibraryRecyclerViewCustomGridSizeFragment, gridSizeItem.getSubMenu());

            menu.findItem(R.id.action_colored_footers).setChecked(absLibraryRecyclerViewCustomGridSizeFragment.usePalette());
            menu.findItem(R.id.action_colored_footers).setEnabled(absLibraryRecyclerViewCustomGridSizeFragment.canUsePalette());
            menu.findItem(R.id.action_colored_footers).setVisible(absLibraryRecyclerViewCustomGridSizeFragment.canUsePalette());

            setUpSortOrderMenu(absLibraryRecyclerViewCustomGridSizeFragment, menu.findItem(R.id.action_sort_order).getSubMenu());
        } else {
            menu.removeItem(R.id.action_grid_size);
            menu.removeItem(R.id.action_colored_footers);
            menu.removeItem(R.id.action_sort_order);
        }
        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(getActivity(), toolbar, menu, ATHToolbarActivity.getToolbarBackgroundColor(toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, toolbar);

        boolean isInstalled = Util.isPackageInstalled(Util.getPkgNameFromAppName(getContext(), PreferenceUtil.getInstance().getPodcastApp()), getContext().getPackageManager());
        if (!PreferenceUtil.getInstance().checkPreferences("podcast") || PreferenceUtil.getInstance().getPodcastApp().equals("None")) {
            menu.removeItem(R.id.action_podcast);
        }else{
            if (!isInstalled){
                menu.removeItem(R.id.action_podcast);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (pager == null) return false;
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment) {
            AbsLibraryPagerRecyclerViewCustomGridSizeFragment absLibraryRecyclerViewCustomGridSizeFragment = (AbsLibraryPagerRecyclerViewCustomGridSizeFragment) currentFragment;
            if (item.getItemId() == R.id.action_colored_footers) {
                item.setChecked(!item.isChecked());
                absLibraryRecyclerViewCustomGridSizeFragment.setAndSaveUsePalette(item.isChecked());
                return true;
            }
            if (handleGridSizeMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true;
            }
            if (handleSortOrderMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true;
            }
        }

        final int id = item.getItemId();
        if (id == R.id.action_new_playlist) {
            CreatePlaylistDialog.create().show(getChildFragmentManager(), "CREATE_PLAYLIST");
            return true;
        }else if (id == R.id.action_rescan) {
            Discography.getInstance().triggerSyncWithMediaStore(true);
            return true;
        } else if (id == R.id.action_search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
        }else if (id == R.id.action_podcast) {
            String packageName = "";

            if (PreferenceUtil.getInstance().checkPreferences("podcast"))
            {
                if (!PreferenceUtil.getInstance().getPodcastApp().equals("None"))
                {
                    packageName = Util.getPkgNameFromAppName(getContext(), PreferenceUtil.getInstance().getPodcastApp());

                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpGridSizeMenu(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull SubMenu gridSizeMenu) {
        switch (fragment.getGridSize()) {
            case 1:
                gridSizeMenu.findItem(R.id.action_grid_size_1).setChecked(true);
                break;
            case 2:
                gridSizeMenu.findItem(R.id.action_grid_size_2).setChecked(true);
                break;
            case 3:
                gridSizeMenu.findItem(R.id.action_grid_size_3).setChecked(true);
                break;
            case 4:
                gridSizeMenu.findItem(R.id.action_grid_size_4).setChecked(true);
                break;
            case 5:
                gridSizeMenu.findItem(R.id.action_grid_size_5).setChecked(true);
                break;
            case 6:
                gridSizeMenu.findItem(R.id.action_grid_size_6).setChecked(true);
                break;
            case 7:
                gridSizeMenu.findItem(R.id.action_grid_size_7).setChecked(true);
                break;
            case 8:
                gridSizeMenu.findItem(R.id.action_grid_size_8).setChecked(true);
                break;
        }
        int maxGridSize = fragment.getMaxGridSize();
        if (maxGridSize < 8) {
            gridSizeMenu.findItem(R.id.action_grid_size_8).setVisible(false);
        }
        if (maxGridSize < 7) {
            gridSizeMenu.findItem(R.id.action_grid_size_7).setVisible(false);
        }
        if (maxGridSize < 6) {
            gridSizeMenu.findItem(R.id.action_grid_size_6).setVisible(false);
        }
        if (maxGridSize < 5) {
            gridSizeMenu.findItem(R.id.action_grid_size_5).setVisible(false);
        }
        if (maxGridSize < 4) {
            gridSizeMenu.findItem(R.id.action_grid_size_4).setVisible(false);
        }
        if (maxGridSize < 3) {
            gridSizeMenu.findItem(R.id.action_grid_size_3).setVisible(false);
        }
    }

    private boolean handleGridSizeMenuItem(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull MenuItem item) {
        int gridSize = 0;
        final int itemId = item.getItemId();
        if (itemId == R.id.action_grid_size_1) {
            gridSize = 1;
        } else if (itemId == R.id.action_grid_size_2) {
            gridSize = 2;
        } else if (itemId == R.id.action_grid_size_3) {
            gridSize = 3;
        } else if (itemId == R.id.action_grid_size_4) {
            gridSize = 4;
        } else if (itemId == R.id.action_grid_size_5) {
            gridSize = 5;
        } else if (itemId == R.id.action_grid_size_6) {
            gridSize = 6;
        } else if (itemId == R.id.action_grid_size_7) {
            gridSize = 7;
        } else if (itemId == R.id.action_grid_size_8) {
            gridSize = 8;
        }
        if (gridSize > 0) {
            item.setChecked(true);
            fragment.setAndSaveGridSize(gridSize);
            toolbar.getMenu().findItem(R.id.action_colored_footers).setEnabled(fragment.canUsePalette());
            toolbar.getMenu().findItem(R.id.action_colored_footers).setVisible(fragment.canUsePalette());
            return true;
        }
        return false;
    }

    private void setUpSortOrderMenu(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull SubMenu sortOrderMenu) {
        String currentSortOrder = fragment.getSortOrder();
        sortOrderMenu.clear();

        if (fragment instanceof AlbumsFragment) {
            sortOrderMenu.add(0, R.id.action_album_sort_order_asc, 0, R.string.sort_order_a_z)
                    .setChecked(currentSortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_A_Z));
            sortOrderMenu.add(0, R.id.action_album_sort_order_desc, 1, R.string.sort_order_z_a)
                    .setChecked(currentSortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_Z_A));
            sortOrderMenu.add(0, R.id.action_album_sort_order_artist, 2, R.string.sort_order_artist)
                    .setChecked(currentSortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_ARTIST));
            sortOrderMenu.add(0, R.id.action_album_sort_order_year, 3, R.string.sort_order_year)
                    .setChecked(currentSortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_YEAR_REVERSE));
            sortOrderMenu.add(0, R.id.action_album_sort_order_date_added, 4, R.string.sort_order_date_added)
                    .setChecked(currentSortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED_REVERSE));
        } else if (fragment instanceof ArtistsFragment) {
            sortOrderMenu.add(0, R.id.action_artist_sort_order_asc, 0, R.string.sort_order_a_z)
                    .setChecked(currentSortOrder.equals(SortOrder.ArtistSortOrder.ARTIST_A_Z));
            sortOrderMenu.add(0, R.id.action_artist_sort_order_desc, 1, R.string.sort_order_z_a)
                    .setChecked(currentSortOrder.equals(SortOrder.ArtistSortOrder.ARTIST_Z_A));
        } else if (fragment instanceof SongsFragment) {
            sortOrderMenu.add(0, R.id.action_song_sort_order_asc, 0, R.string.sort_order_a_z)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_A_Z));
            sortOrderMenu.add(0, R.id.action_song_sort_order_desc, 1, R.string.sort_order_z_a)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_Z_A));
            sortOrderMenu.add(0, R.id.action_song_sort_order_artist, 2, R.string.sort_order_artist)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_ARTIST));
            sortOrderMenu.add(0, R.id.action_song_sort_order_album, 3, R.string.sort_order_album)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_ALBUM));
            sortOrderMenu.add(0, R.id.action_song_sort_order_year, 4, R.string.sort_order_year)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_YEAR_REVERSE));
            sortOrderMenu.add(0, R.id.action_song_sort_order_date_added, 5, R.string.sort_order_date_added)
                    .setChecked(currentSortOrder.equals(SortOrder.SongSortOrder.SONG_DATE_ADDED_REVERSE));
        }

        sortOrderMenu.setGroupCheckable(0, true, true);
    }

    private boolean handleSortOrderMenuItem(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull MenuItem item) {
        String sortOrder = null;
        final int itemId = item.getItemId();
        if (fragment instanceof AlbumsFragment) {
            if (itemId == R.id.action_album_sort_order_asc) {
                sortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z;
            } else if (itemId == R.id.action_album_sort_order_desc) {
                sortOrder = SortOrder.AlbumSortOrder.ALBUM_Z_A;
            } else if (itemId == R.id.action_album_sort_order_artist) {
                sortOrder = SortOrder.AlbumSortOrder.ALBUM_ARTIST;
            } else if (itemId == R.id.action_album_sort_order_year) {
                sortOrder = SortOrder.AlbumSortOrder.ALBUM_YEAR_REVERSE;
            } else if (itemId == R.id.action_album_sort_order_date_added) {
                sortOrder = SortOrder.AlbumSortOrder.ALBUM_DATE_ADDED_REVERSE;
            }
        } else if (fragment instanceof ArtistsFragment) {
            if (itemId == R.id.action_artist_sort_order_asc) {
                sortOrder = SortOrder.ArtistSortOrder.ARTIST_A_Z;
            } else if (itemId == R.id.action_artist_sort_order_desc) {
                sortOrder = SortOrder.ArtistSortOrder.ARTIST_Z_A;
            }
        } else if (fragment instanceof SongsFragment) {
            if (itemId == R.id.action_song_sort_order_asc) {
                sortOrder = SortOrder.SongSortOrder.SONG_A_Z;
            } else if (itemId == R.id.action_song_sort_order_desc) {
                sortOrder = SortOrder.SongSortOrder.SONG_Z_A;
            } else if (itemId == R.id.action_song_sort_order_artist) {
                sortOrder = SortOrder.SongSortOrder.SONG_ARTIST;
            } else if (itemId == R.id.action_song_sort_order_album) {
                sortOrder = SortOrder.SongSortOrder.SONG_ALBUM;
            } else if (itemId == R.id.action_song_sort_order_year) {
                sortOrder = SortOrder.SongSortOrder.SONG_YEAR_REVERSE;
            } else if (itemId == R.id.action_song_sort_order_date_added) {
                sortOrder = SortOrder.SongSortOrder.SONG_DATE_ADDED_REVERSE;
            }
        }

        if (sortOrder != null) {
            item.setChecked(true);
            fragment.setAndSaveSortOrder(sortOrder);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleBackPress() {
        if (cab != null && cab.isActive()) {
            cab.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        PreferenceUtil.getInstance().setLastPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void checkOrientation(){

        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (pagerAdapter.getCount() == 5){
                tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
                tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
            }
            else
            {
                tabs.setTabMode(TabLayout.MODE_FIXED);
                tabs.setTabGravity(TabLayout.GRAVITY_FILL);
            }
        } else {
            tabs.setTabMode(TabLayout.MODE_FIXED);
            tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            tabs.setTabMode(TabLayout.MODE_FIXED);
            tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            if (pagerAdapter.getCount() == 5){
                tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
                tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
            }
            else
            {
                tabs.setTabMode(TabLayout.MODE_FIXED);
                tabs.setTabGravity(TabLayout.GRAVITY_FILL);
            }
        }

    }
}