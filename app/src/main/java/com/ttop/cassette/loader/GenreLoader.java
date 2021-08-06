package com.ttop.cassette.loader;

import androidx.annotation.NonNull;

import com.ttop.cassette.discog.Discography;
import com.ttop.cassette.model.Genre;
import com.ttop.cassette.model.Song;
import com.ttop.cassette.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class GenreLoader {
    @NonNull
    public static ArrayList<Genre> getAllGenres() {
        ArrayList<Genre> genres = Discography.getInstance().getAllGenres();
        Collections.sort(genres, (g1, g2) -> StringUtil.compareIgnoreAccent(g1.name, g2.name));
        return genres;
    }

    @NonNull
    public static ArrayList<Song> getSongs(final long genreId) {
        Collection<Song> genreSongs = Discography.getInstance().getSongsForGenre(genreId);
        if (genreSongs == null) {
            return new ArrayList<>();
        }
        else {
            ArrayList<Song> songs = new ArrayList<>(genreSongs);
            Collections.sort(songs, SongLoader.BY_TITLE);
            return songs;
        }
    }
}
