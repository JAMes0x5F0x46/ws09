package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

public class PlayerControl extends JPanel implements PlayerListener {
    private static final long serialVersionUID = 1L;

    private JButton play, next, prev;

    private JLabel status;

    private static final String ACT_PLAY = "PLAY";

    private static final String ACT_STOP = "STOP";

    private static final String ACT_NEXT = "NEXT";

    private static final String ACT_PREV = "PREV";

    protected static final String ICON_PREFIX = "rsc/icons/pl_";

    protected static final String ICON_SUFFIX = ".gif";

    private ImageIcon playIcon = null, stopIcon = null, nextIcon = null, prevIcon = null;

    private PlayList playlist;

    public PlayerControl(PlayList playlist) {
        this.playlist = playlist;
        initialize();
    }

    protected ImageIcon createImageIcon(String actionKey) {
        String path = ICON_PREFIX + actionKey.toLowerCase() + ICON_SUFFIX;
        try {
            return new ImageIcon(ClassLoader.getSystemResource(path));
        } catch (Exception e) {
            Logger.getLogger("at.tuwien.ifs.somtoolbox.player").warning("Icon for " + actionKey + " (" + path + ") not found.");
            return null;
        }
    }

    private void adaptPlayButton(String targetAction) {
        Icon i = null;
        String caption = "";

        if (ACT_PLAY.equals(targetAction)) {
            i = playIcon;
            caption = ACT_PLAY;
        } else {
            i = stopIcon;
            caption = ACT_STOP;
        }

        play.setActionCommand(targetAction);
        play.setIcon(i);
        if (i == null) {
            play.setText(caption);
        } else {
            play.setText("");
        }
    }

    private void initialize() {

        playIcon = createImageIcon(ACT_PLAY);
        stopIcon = createImageIcon(ACT_STOP);
        nextIcon = createImageIcon(ACT_NEXT);
        prevIcon = createImageIcon(ACT_PREV);

        setLayout(new GridBagLayout());
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.gridy = 0;
        gbcB.weightx = 1;
        gbcB.weighty = 1;

        prev = new JButton();
        prev.setActionCommand(ACT_PREV);
        if (prevIcon == null) {
            prev.setText(ACT_PREV);
        } else {
            prev.setBorder(null);
            prev.setIcon(prevIcon);
        }
        prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playlist.prev();
            }
        });
        add(prev, gbcB);

        play = new JButton();
        play.setBorder(null);
        play.setActionCommand(ACT_PLAY);
        adaptPlayButton(ACT_PLAY);
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals(ACT_PLAY)) {
                    playlist.play();
                    // play.setActionCommand(ACT_STOP);
                    // adaptPlayButton(ACT_STOP);
                } else {
                    playlist.stop();
                    // play.setActionCommand(ACT_PLAY);
                    // adaptPlayButton(ACT_PLAY);
                }
            }
        });
        add(play, gbcB);

        next = new JButton();
        add(next, gbcB);
        next.setActionCommand(ACT_NEXT);
        next.setIcon(nextIcon);
        if (nextIcon == null) {
            next.setText(ACT_NEXT);
        } else {
            next.setBorder(null);
        }

        next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                playlist.next();
            }
        });
        GridBagConstraints gbcL = new GridBagConstraints();
        gbcL.gridy = 1;
        gbcL.weightx = 1;
        gbcL.weighty = 0;
        gbcL.fill = GridBagConstraints.BOTH;
        gbcL.gridwidth = GridBagConstraints.REMAINDER;

        add(status = new JLabel("Stopped"), gbcL);
        // status.q

        playlist.addPlayerListener(this);
    }

    @Override
    public void playStarted(int mode, AudioVectorMetaData song) {
        status.setText(song.getDisplayLabel());
        adaptPlayButton(ACT_STOP);
    }

    @Override
    public void playStopped(int reason, AudioVectorMetaData song) {
        switch (reason) {
            case PlayerListener.STOP_REASON_ENDED:
                status.setText("Finished");
                break;
            case PlayerListener.STOP_REASON_STOPPED:
                status.setText("Stopped");
                break;
        }
        adaptPlayButton(ACT_PLAY);
    }

}
