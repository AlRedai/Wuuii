package audio;

import javax.swing.SwingWorker;


public class TaskTone extends SwingWorker<Void,Void> {
	@Override
	public Void doInBackground() {
	    AudioPlay ad = new AudioPlay();
		while(!isCancelled()) {
			ad.Ringtone();
		}
		return null;
	}
}