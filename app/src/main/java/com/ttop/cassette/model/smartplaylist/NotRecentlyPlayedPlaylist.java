package com.ttop.cassette.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ttop.cassette.R;
import com.ttop.cassette.loader.TopAndRecentlyPlayedTracksLoader;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.util.MusicUtil;
import com.ttop.cassette.util.PreferenceUtil;

import java.util.ArrayList;

/**
 * @author SC (soncaokim)
 */
public class NotRecentlyPlayedPlaylist extends AbsSmartPlaylist {

    public NotRecentlyPlayedPlaylist(@NonNull Context context) {
        super(context.getString(R.string.not_recently_played), R.drawable.ic_watch_later_white_24dp);
    }

    @NonNull
    @Override
    public String getInfoString(@NonNull Context context) {
        String cutoff = PreferenceUtil.getInstance().getNotRecentlyPlayedCutoffText(context);

        return MusicUtil.buildInfoString(
                cutoff,
                super.getInfoString(context)
        );
    }

    @Nullable
    @Override
    public String getPlaylistPreference() {
        return PreferenceUtil.NOT_RECENTLY_PLAYED_CUTOFF_V2;
    }

    @NonNull
    @Override
    public ArrayList<Song> getSongs(@NonNull Context context) {
        return TopAndRecentlyPlayedTracksLoader.getNotRecentlyPlayedTracks(context);
    }

    @Override
    public boolean isClearable() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected NotRecentlyPlayedPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<NotRecentlyPlayedPlaylist> CREATOR = new Creator<NotRecentlyPlayedPlaylist>() {
        public NotRecentlyPlayedPlaylist createFromParcel(Parcel source) {
            return new NotRecentlyPlayedPlaylist(source);
        }

        public NotRecentlyPlayedPlaylist[] newArray(int size) {
            return new NotRecentlyPlayedPlaylist[size];
        }
    };
}
