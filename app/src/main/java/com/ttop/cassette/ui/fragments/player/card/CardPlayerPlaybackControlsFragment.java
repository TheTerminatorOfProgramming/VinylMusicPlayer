package com.ttop.cassette.ui.fragments.player.card;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.ttop.cassette.R;
import com.ttop.cassette.databinding.FragmentCardPlayerPlaybackControlsBinding;
import com.ttop.cassette.helper.MusicPlayerRemote;
import com.ttop.cassette.helper.MusicProgressViewUpdateHelper;
import com.ttop.cassette.helper.PlayPauseButtonOnClickHandler;
import com.ttop.cassette.misc.SimpleOnSeekbarChangeListener;
import com.ttop.cassette.service.MusicService;
import com.ttop.cassette.ui.fragments.AbsMusicServiceFragment;
import com.ttop.cassette.util.MusicUtil;
import com.ttop.cassette.util.PreferenceUtil;
import com.ttop.cassette.views.PlayPauseDrawable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class CardPlayerPlaybackControlsFragment extends AbsMusicServiceFragment implements MusicProgressViewUpdateHelper.Callback {
    FloatingActionButton playPauseFab;
    ImageButton prevButton;
    ImageButton nextButton;
    ImageButton repeatButton;
    ImageButton shuffleButton;

    SeekBar progressSlider;
    TextView songTotalTime;
    TextView songCurrentProgress;

    int seekTime = 1000;

    private PlayPauseDrawable playerFabPlayPauseDrawable;

    private int lastPlaybackControlsColor;
    private int lastDisabledPlaybackControlsColor;

    private MusicProgressViewUpdateHelper progressViewUpdateHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressViewUpdateHelper = new MusicProgressViewUpdateHelper(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentCardPlayerPlaybackControlsBinding binding = FragmentCardPlayerPlaybackControlsBinding.inflate(inflater, container, false);
        playPauseFab = binding.playerPlayPauseFab;
        prevButton = binding.playerPrevButton;
        nextButton = binding.playerNextButton;
        repeatButton = binding.playerRepeatButton;
        shuffleButton = binding.playerShuffleButton;
        progressSlider = binding.playerProgressSlider;
        songTotalTime = binding.playerSongTotalTime;
        songCurrentProgress = binding.playerSongCurrentProgress;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMusicControllers();
        updateProgressTextColor();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressViewUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progressViewUpdateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        updatePlayPauseDrawableState(false);
        updateRepeatState();
        updateShuffleState();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseDrawableState(true);
    }

    @Override
    public void onRepeatModeChanged() {
        updateRepeatState();
    }

    @Override
    public void onShuffleModeChanged() {
        updateShuffleState();
    }

    public void setDark(boolean dark) {
        if (dark) {
            lastPlaybackControlsColor = MaterialValueHelper.getSecondaryTextColor(getActivity(), true);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getSecondaryDisabledTextColor(getActivity(), true);
        } else {
            lastPlaybackControlsColor = MaterialValueHelper.getPrimaryTextColor(getActivity(), false);
            lastDisabledPlaybackControlsColor = MaterialValueHelper.getPrimaryDisabledTextColor(getActivity(), false);
        }

        updateRepeatState();
        updateShuffleState();
        updatePrevNextColor();
        updateProgressTextColor();
    }

    private void setUpPlayPauseFab() {
        final int fabColor = Color.WHITE;
        TintHelper.setTintAuto(playPauseFab, fabColor, true);

        playerFabPlayPauseDrawable = new PlayPauseDrawable(getActivity());

        playPauseFab.setImageDrawable(playerFabPlayPauseDrawable); // Note: set the drawable AFTER TintHelper.setTintAuto() was called
        playPauseFab.setColorFilter(MaterialValueHelper.getPrimaryTextColor(getContext(), ColorUtil.isColorLight(fabColor)), PorterDuff.Mode.SRC_IN);
        playPauseFab.setOnClickListener(new PlayPauseButtonOnClickHandler());
        playPauseFab.post(() -> {
            if (playPauseFab != null) {
                playPauseFab.setPivotX(playPauseFab.getWidth() / 2);
                playPauseFab.setPivotY(playPauseFab.getHeight() / 2);
            }
        });
    }

    protected void updatePlayPauseDrawableState(boolean animate) {
        if (MusicPlayerRemote.isPlaying()) {
            playerFabPlayPauseDrawable.setPause(animate);
        } else {
            playerFabPlayPauseDrawable.setPlay(animate);
        }
    }

    private void setUpMusicControllers() {
        setUpPlayPauseFab();

        if (PreferenceUtil.getInstance().getNextPrevButtons()) {
            nextButton.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);

            setUpPrevNext();
        } else {
            nextButton.setVisibility(View.GONE);
            prevButton.setVisibility(View.GONE);
        }

        setUpRepeatButton();
        setUpShuffleButton();
        setUpProgressSlider();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpPrevNext() {
        updatePrevNextColor();

        nextButton.setOnTouchListener(new com.ttop.cassette.misc.LongTouchActionListener(requireContext()) {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.playNextSong();
            }

            @Override
            public void onLongTouchAction(View v) {
                // get current song position
                int currentPosition = MusicPlayerRemote.getSongProgressMillis();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekTime <= MusicPlayerRemote.getSongDurationMillis()) {
                    // forward song
                    MusicPlayerRemote.seekTo(currentPosition + seekTime);
                } else {
                    // forward to end position
                    MusicPlayerRemote.seekTo(MusicPlayerRemote.getSongDurationMillis());
                }
            }
        });

        prevButton.setOnTouchListener(new com.ttop.cassette.misc.LongTouchActionListener(requireContext()) {
            @Override
            public void onClick(View v) {
                MusicPlayerRemote.back();
            }

            @Override
            public void onLongTouchAction(View v) {
                // get current song position
                int currentPosition = MusicPlayerRemote.getSongProgressMillis();
                // check if seekRewind time is more than 0
                if (currentPosition - seekTime >= 0) {
                    // rewind song
                    MusicPlayerRemote.seekTo(currentPosition - seekTime);
                } else {
                    // rewind to start position
                    MusicPlayerRemote.seekTo(0);
                }
            }
        });
    }

    private void updateProgressTextColor() {
        int color = MaterialValueHelper.getPrimaryTextColor(getContext(), false);
        songTotalTime.setTextColor(color);
        songCurrentProgress.setTextColor(color);
    }

    private void updatePrevNextColor() {
        nextButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        prevButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    private void setUpShuffleButton() {
        shuffleButton.setOnClickListener(v -> MusicPlayerRemote.toggleShuffleMode());
    }

    private void updateShuffleState() {
        if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
            shuffleButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        } else {
            shuffleButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setUpRepeatButton() {
        repeatButton.setOnClickListener(v -> MusicPlayerRemote.cycleRepeatMode());
    }

    private void updateRepeatState() {
        switch (MusicPlayerRemote.getRepeatMode()) {
            case MusicService.REPEAT_MODE_NONE:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(lastDisabledPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_ALL:
                repeatButton.setImageResource(R.drawable.ic_repeat_white_24dp);
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case MusicService.REPEAT_MODE_THIS:
                repeatButton.setImageResource(R.drawable.ic_repeat_one_white_24dp);
                repeatButton.setColorFilter(lastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
    }

    public void show() {
        playPauseFab.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public void hide() {
        if (playPauseFab != null) {
            playPauseFab.setScaleX(0f);
            playPauseFab.setScaleY(0f);
            playPauseFab.setRotation(0f);
        }
    }

    private void setUpProgressSlider() {
        int color = MaterialValueHelper.getPrimaryTextColor(getContext(), false);
        progressSlider.getThumb().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        progressSlider.getProgressDrawable().mutate().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);

        progressSlider.setOnSeekBarChangeListener(new SimpleOnSeekbarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    MusicPlayerRemote.seekTo(progress);
                    onUpdateProgressViews(MusicPlayerRemote.getSongProgressMillis(), MusicPlayerRemote.getSongDurationMillis());
                }
            }
        });
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        progressSlider.setMax(total);
        progressSlider.setProgress(progress);
        songTotalTime.setText(MusicUtil.getReadableDurationString(total));
        songCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }
}