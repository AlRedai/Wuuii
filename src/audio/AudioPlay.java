package audio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;

import Wuuii.Wuuii;
import options.Getoptions;
import error.ErrorMessage;

public class AudioPlay extends AudioOption {

	public void Ringtone() {
		try {
			Synthesizer synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			MidiChannel[] channels = synthesizer.getChannels();
			int one = 90;
			int two = 110;
			for(int i=0; i<100; i++) {
				channels[0].noteOn(one, one);
				Thread.sleep(50);
				channels[0].noteOn(two, two);
				Thread.sleep(60);
				
				channels[0].noteOn(one, one);
				Thread.sleep(50);
				channels[0].noteOn(two, two);
				Thread.sleep(60);
			}
			synthesizer.close();
		} catch (Exception e) { }
	}
	
    public void reproduction() {
    	final Getoptions opt = new Getoptions();
    	//final AudioOption ao = new AudioOption();
    	try {
    		byte audio[] = Wuuii.out.toByteArray();
    		
    		InputStream input = new ByteArrayInputStream(audio);
    		final AudioFormat format = getTransmissionFormat();
    	    final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
    	    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
    	    final SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);
    	    line.open(format);
    	    line.start();
    	    Runnable runner = new Runnable() {
    	    	int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
    	    	byte buffer[] = new byte[bufferSize];

    	    	public void run() {
    	    		opt.setTestAudio(true);
    	    		try {
    	    			int count;
    	    			while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
    	    				if (count > 0) {
    	    					line.write(buffer, 0, count);
    	    					Wuuii.Spectrum(buffer, format, Wuuii.sendCapture);
    	    				}
    	    			}
    	    			line.drain();
    	    			line.close();
    	    		} catch (IOException e) {
    	    			ErrorMessage error = new ErrorMessage("I/O Problems: " + e);
    	    			error.EMessage();
    	    		}
    	    		opt.setTestAudio(false);
    	       }
    	    };
    	    Thread playThread = new Thread(runner);
    	    playThread.start();
    	} catch (LineUnavailableException e) {
    		ErrorMessage error = new ErrorMessage("Line unavailable: " + e);
			error.EMessage();
    	} 
    }
}

