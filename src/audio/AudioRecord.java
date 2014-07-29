package audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import Wuuii.Wuuii;
import options.Getoptions;
import error.ErrorMessage;

public class AudioRecord extends AudioOption {
	
	private static TargetDataLine tLine;
	
	public void capture() {
		final Getoptions opt = new Getoptions();

		try {
    	    final AudioFormat format = getTransmissionFormat();
    		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    	    final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(info);
    	    line.open(format);
    	    line.start();
    	    Runnable runner = new Runnable() {
    	    	int bufferSize = (int)format.getSampleRate()  * format.getFrameSize();
    	        byte buffer[] = new byte[bufferSize];

    	        public void run() {
    	        	Wuuii.out = new ByteArrayOutputStream();
    	        	opt.setRecord(true);
    	        	try {
    	        		while (opt.getRecord()) {
    	        			int count = line.read(buffer, 0, buffer.length);
    	        			if (count > 0) {
    	        				Wuuii.Spectrum(buffer, format, Wuuii.recvCapture);
    	        				Wuuii.out.write(buffer, 0, count);
    	        			}
    	        		}
    	        		Wuuii.out.close();
    	        	} catch (IOException e) {
    	        		ErrorMessage error = new ErrorMessage("I/O problems: " + e);
    	        		error.EMessage();

    	        	}
    	       }
    	    };
    	    
    	    Thread captureThread = new Thread(runner);
    	    captureThread.start();
    	} catch (LineUnavailableException e) {
    		ErrorMessage error = new ErrorMessage("Line Unavailable: " + e);
    		error.EMessage();	
    	}
    }
	
	public void sendAudioSocks(final SocketChannel chan, final boolean join, final int channel) {
		Runnable runner = new Runnable() {

			public void run() {
				Getoptions opt = new Getoptions();

				if(Wuuii.DEBUG) {
					System.out.println("Send audio byte[] buffer join="+join +" transmission=" + opt.getTransmission());
				}
				
				//AudioFormat	format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0F, 16, channel, 4, 44100.0F, false);
				
				AudioFormat format = getTransmissionFormat();
				DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format, 40960);
				byte[]	audioBuffer = new byte[40960];
				int	nBufferSize = audioBuffer.length;
				try {
					tLine = (TargetDataLine)AudioSystem.getLine(targetInfo);
					tLine.open(format, 40960);
					tLine.start();
					
					while(opt.getTransmission()) {
						tLine.read(audioBuffer, 0, nBufferSize);
						try {	
							//if(ByteBuffer.wrap(aBuffer) != null && chan.isOpen()) {
							Wuuii.Spectrum(audioBuffer, format, Wuuii.recvCapture);
							int len = chan.write(ByteBuffer.wrap(audioBuffer));
							if(len == -1)
								break;
						} catch (IOException e) {
							ErrorMessage error = new ErrorMessage("I/O Exception" + e);
							error.EMessage();
						}		
					}
					if(Wuuii.DEBUG) {
						opt.prFlush("HANGUP record from server");
					}
					try {
						chan.close();
					} catch (IOException e) {
						ErrorMessage error = new ErrorMessage("I/O Exception" + e);
						error.EMessage();
					}
					tLine.drain();
					tLine.close();
				} catch (LineUnavailableException e) {
					ErrorMessage error = new ErrorMessage("Line Unavailable: " + e);
		    		error.EMessage();
				}
			}
		};
		Thread rec = new Thread(runner);
		rec.start();
		if(join) {
			try {
				rec.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}
	}
	
}
