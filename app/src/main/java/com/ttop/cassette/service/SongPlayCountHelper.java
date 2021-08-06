package com.ttop.cassette.service;

import com.ttop.cassette.helper.StopWatch;
import com.ttop.cassette.model.Song;

class SongPlayCountHelper {
    public static final String TAG = com.ttop.cassette.service.SongPlayCountHelper.class.getSimpleName();

    private StopWatch stopWatch = new StopWatch();
    private Song song = Song.EMPTY_SONG;

    public Song getSong() {
        return song;
    }

    boolean shouldBumpPlayCount() {
        return song.duration * 0.5d < stopWatch.getElapsedTime();
    }

    void notifySongChanged(Song song) {
        synchronized (this) {
            stopWatch.reset();
            this.song = song;
        }
    }

    void notifyPlayStateChanged(boolean isPlaying) {
        synchronized (this) {
            if (isPlaying) {
                stopWatch.start();
            } else {
                stopWatch.pause();
            }
        }
    }
}
