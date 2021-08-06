package com.ttop.cassette.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.ttop.cassette.R;
import com.ttop.cassette.glide.palette.BitmapPaletteTarget;
import com.ttop.cassette.glide.palette.BitmapPaletteWrapper;
import com.ttop.cassette.util.CassetteColorUtil;

public abstract class CassetteColoredTarget extends BitmapPaletteTarget {
    public CassetteColoredTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        onColorReady(getDefaultFooterColor());
    }

    @Override
    public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
        super.onResourceReady(resource, glideAnimation);
        onColorReady(CassetteColorUtil.getColor(resource.getPalette(), getDefaultFooterColor()));
    }

    protected int getDefaultFooterColor() {
        return ATHUtil.resolveColor(getView().getContext(), R.attr.defaultFooterColor);
    }

    protected int getAlbumArtistFooterColor() {
        return ATHUtil.resolveColor(getView().getContext(), R.attr.cardBackgroundColor);
    }

    public abstract void onColorReady(int color);
}
