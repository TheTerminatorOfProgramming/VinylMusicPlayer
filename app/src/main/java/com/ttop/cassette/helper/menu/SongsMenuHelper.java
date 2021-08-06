package com.ttop.cassette.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.ttop.cassette.R;
import com.ttop.cassette.dialogs.AddToPlaylistDialog;
import com.ttop.cassette.dialogs.DeleteSongsDialog;
import com.ttop.cassette.helper.MusicPlayerRemote;
import com.ttop.cassette.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongsMenuHelper {
    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull ArrayList<Song> songs, int menuItemId) {
        if (menuItemId == R.id.action_play_next) {
            MusicPlayerRemote.playNext(songs);
            return true;
        } else if (menuItemId == R.id.action_add_to_current_playing) {
            MusicPlayerRemote.enqueue(songs);
            return true;
        } else if (menuItemId == R.id.action_add_to_playlist) {
            AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
            return true;
        } else if (menuItemId == R.id.action_delete_from_device) {
            DeleteSongsDialog.create(songs).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
            return true;
        }
        return false;
    }
}
