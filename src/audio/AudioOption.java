package audio;

import javax.sound.sampled.AudioFormat;

public class AudioOption {
	
	public AudioFormat get8BitFormat() {
    	float sampleRate = 8000;
    	int sampleSizeInBits = 8;
    	int channels = 1;
    	boolean signed = true;
    	boolean bigEndian = false;
    	return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }	
	
	public AudioFormat getTransmissionFormat() {
    	float sampleRate = 44100.0F;
    	int sampleSizeInBits = 16;
    	int channels = 2;
    	int bytes = 4;
    	boolean bigEndian = false;
    	return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, sampleSizeInBits, channels, bytes, sampleRate, bigEndian);
    }	
}
