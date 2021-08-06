package com.ttop.cassette.appwidgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.ttop.cassette.R;
import com.ttop.cassette.appwidgets.base.BaseAppWidget;
import com.ttop.cassette.glide.CassetteGlideExtension;
import com.ttop.cassette.glide.CassetteSimpleTarget;
import com.ttop.cassette.glide.GlideApp;
import com.ttop.cassette.glide.palette.BitmapPaletteWrapper;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.service.MusicService;
import com.ttop.cassette.util.ImageUtil;
import com.ttop.cassette.util.PreferenceUtil;
import com.ttop.cassette.util.Util;

public class AppWidgetFull extends BaseAppWidget {
    public static final String NAME = "app_widget_full";

    private static AppWidgetFull mInstance;

    public static synchronized AppWidgetFull getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetFull();
        }
        return mInstance;
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        appWidgetView = new RemoteViews(service.getPackageName(), getLayout());

        // Set the titles and artwork
        setTitlesArtwork(service, "single");

        // Set the buttons
        setButtons(service);

        // Link actions buttons to intents
        linkButtons(service);

        // Load the album cover async and push the update on completion
        Point p = Util.getScreenSize(service);

        final int widgetImageSize = Math.min(p.x, p.y);

        final Context appContext = service.getApplicationContext();

        service.runOnUiThread(() -> {
            if (target != null) {
                GlideApp.with(appContext).clear(target);
            }
            final boolean isPlaying = service.isPlaying();
            final Song song = service.getCurrentSong();
            GlideApp.with(appContext)
                    .asBitmapPalette()
                    .load(CassetteGlideExtension.getSongModel(song))
                    .transition(CassetteGlideExtension.getDefaultTransition())
                    .songOptions(song)
                    .dontAnimate()
                    .into(new CassetteSimpleTarget<BitmapPaletteWrapper>(widgetImageSize, widgetImageSize) {
                        @Override
                        public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                            Palette palette = resource.getPalette();
                            update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(MaterialValueHelper.getSecondaryTextColor(appContext, true))));
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            update(null, MaterialValueHelper.getSecondaryTextColor(appContext, true));
                        }

                        private void update(@Nullable Bitmap bitmap, int color) {
                            if (bitmap == null) {
                                appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
                            } else {
                                appWidgetView.setImageViewBitmap(R.id.image, bitmap);
                            }

                            // Set correct drawable for pause state
                            int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
                            appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, playPauseRes, color)));

                            // Set prev/next button drawables
                            appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, R.drawable.ic_skip_next_white_24dp, color)));
                            appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(appContext, R.drawable.ic_skip_previous_white_24dp, color)));

                            appWidgetView.setInt(getId(), "setBackgroundResource", PreferenceUtil.getThemeColorFromPrefValue(PreferenceUtil.getInstance().getAppTheme()));

                            pushUpdate(appContext, appWidgetIds);
                        }
                    });
        });

        appWidgetView.setTextColor(R.id.title, service.getResources().getColor(PreferenceUtil.getWidgetTextColorFromPrefValue(PreferenceUtil.getInstance().getAppTheme())));
        appWidgetView.setTextColor(R.id.text, service.getResources().getColor(PreferenceUtil.getWidgetTextColorFromPrefValue(PreferenceUtil.getInstance().getAppTheme())));
    }

    public int getLayout() {
        return R.layout.app_widget_full;
    }

    public int getId() {
        return R.id.app_widget_full;
    }

    public int getImageSize(MusicService service) {
        return 0;
    }

    public float getCardRadius(MusicService service) {
        return 0;
    }
}
