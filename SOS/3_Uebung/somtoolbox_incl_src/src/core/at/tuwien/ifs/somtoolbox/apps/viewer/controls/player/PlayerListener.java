package at.tuwien.ifs.somtoolbox.apps.viewer.controls.player;

import at.tuwien.ifs.somtoolbox.data.metadata.AudioVectorMetaData;

public interface PlayerListener {
	
	public static final int STOP_REASON_PAUSED = 1;
	public static final int STOP_REASON_STOPPED = 2;
	public static final int STOP_REASON_ENDED = 3;
	
	public static final int START_MODE_NEW = 1;
	public static final int START_MODE_RESUME = 2;
	
	void playStarted(int mode, AudioVectorMetaData song);
	void playStopped(int reason, AudioVectorMetaData song);
}
