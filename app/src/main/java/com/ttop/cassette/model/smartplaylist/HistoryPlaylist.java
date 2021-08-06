package com.ttop.cassette.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ttop.cassette.R;
import com.ttop.cassette.loader.TopAndRecentlyPlayedTracksLoader;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.provider.HistoryStore;
import com.ttop.cassette.util.MusicUtil;
import com.ttop.cassette.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HistoryPlaylist extends AbsSmartPlaylist {

    public HistoryPlaylist(@NonNull Context context) {
        super(context.getString(R.string.history), R.drawable.ic_access_time_white_24dp);
    }

    @NonNull
    @Override
    public String getInfoString(@NonNull Context context) {
        String cutoff = PreferenceUtil.getInstance().getRecentlyPlayedCutoffText(context);

        return MusicUtil.buildInfoString(
            cutoff,
            super.getInfoString(context)
        );
    }

    @Nullable
    @Override
    public String getPlaylistPreference() {
        return PreferenceUtil.RECENTLY_PLAYED_CUTOFF_V2;
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        HistoryStore.getInstance(context).clear();
        super.clear(context);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected HistoryPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<HistoryPlaylist> CREATOR = new Creator<HistoryPlaylist>() {
        public HistoryPlaylist createFromParcel(Parcel source) {
            return new HistoryPlaylist(source);
        }

        public HistoryPlaylist[] newArray(int size) {
            return new HistoryPlaylist[size];
        }
    };
}
