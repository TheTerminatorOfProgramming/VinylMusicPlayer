package com.ttop.cassette.adapter.song;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ttop.cassette.R;
import com.ttop.cassette.discog.tagging.MultiValuesTagUtil;
import com.ttop.cassette.interfaces.CabHolder;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.util.MusicUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AlbumSongAdapter extends SongAdapter {

    public AlbumSongAdapter(AppCompatActivity activity, ArrayList<Song> dataSet, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, R.layout.item_list, usePalette, cabHolder);
        this.showAlbumImage = false; // We don't want to load it in this adapter
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final String trackNumberString = MusicUtil.getTrackNumberInfoString(song);
            holder.imageText.setText(trackNumberString);
        }
    }

    @Override
    protected String getSongText(Song song) {
        return MusicUtil.buildInfoString(
                MusicUtil.getReadableDurationString(song.duration),
                MultiValuesTagUtil.infoString(song.artistNames));
    }
}
