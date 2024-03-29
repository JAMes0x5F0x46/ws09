package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import java.util.ArrayList;
import java.util.List;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;
import at.tuwien.ifs.somtoolbox.data.metadata.MP3VectorMetaData;

public class AudioPlayer {

    private AudioVectorMetaData currentSong = null;

    // private int currentPos = 0;

    // private int mode;
    // private AdvancedPlayer player = null;
    private AudioPlayThread player = null;

    protected List<PlayerListener> listeners;

    public AudioPlayer() {
        listeners = new ArrayList<PlayerListener>();
    }

    public void play() {
        if (player != null)
            stop();
        if (currentSong != null) {
            player = createPlayThread(currentSong);
            player.start();
            informListenersStart(PlayerListener.START_MODE_NEW, currentSong);
        }
    }

    public void pause() {
        stop();
        // mode = PLAYER_STATUS_PAUSED;
    }

    public void togglePlayPause() {
        if (player != null) {
            play();
        } else {
            pause();
        }
    }

    public void stop() {
        if (player != null) {
            // player.stop();
            player.stopPlaying();
            player = null;
        }
        // mode = PLAYER_STATUS_STOPPED;
    }

    public void play(AudioVectorMetaData item) {
        stop();
        currentSong = item;
        play();
    }

    private void informListenersStart(int mode, AudioVectorMetaData song) {
        for (PlayerListener listener : listeners) {
            listener.playStarted(mode, song);
        }
    }

    private void informListenersStop(int reason, AudioVectorMetaData song) {
        for (PlayerListener listener : listeners) {
            listener.playStopped(reason, song);
        }
    }

    public boolean isPlaying() {
        return (player != null && player.isAlive());
    }

    public void addMP3PlayerListener(PlayerListener l) {
        listeners.add(l);
    }

    public void removeMP3PlayerListener(PlayerListener l) {
        listeners.remove(l);
    }

    private AudioPlayThread createPlayThread(AudioVectorMetaData toPlay) {
        PlayerListener rec = new PlayerListener() {
            @Override
            public void playStarted(int mode, AudioVectorMetaData song) {
            }

            @Override
            public void playStopped(int reason, AudioVectorMetaData song) {
                informListenersStop(reason, song);
            }

        };
        if (toPlay instanceof MP3VectorMetaData) {
            return new MP3PlayThread(toPlay, rec);
        } else {
            return new FlatAudioPlayThread(toPlay, rec);
        }
    }

}
