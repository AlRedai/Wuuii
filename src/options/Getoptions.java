package options;

import Wuuii.Wuuii;

public class Getoptions extends Wuuii {
	private static boolean transmission = false;
	private static boolean hangup = false;
	private static boolean record = false;
	private static boolean testAudio = false;
	private static boolean handshake = true;
	private static boolean spectrum = false;
	
	public Getoptions() { }
	
	public void setTestAudio(boolean value) {
		testAudio = value;
	}
	
	public boolean getTestAudio() {
		return testAudio;
	}
	
	public void setTransmission(boolean value) {
		transmission = value;
	}
	
	public boolean getTransmission() {
		return transmission;
	}
	
	public void setHangup(boolean value) {
		hangup = value;
	}
	
	public boolean getHangup() {
		return hangup;
	}
	
	public void setRecord(boolean value) {
		record = value;
	}
	
	public boolean getRecord() {
		return record;
	}
	
	public void setHandshake(boolean value) {
		handshake = value;
	}
	
	public boolean getHandshake() {
		return handshake;
	}
	
	public void setSpectrum(boolean value) {
		spectrum = value;
	}
	
	public boolean getSpectrum() {
		return spectrum;
	}
	
	public void prFlush(String message) {
		System.out.println(message);
		System.out.flush();
	}
	
	public void jprint(String message) {
		Wuuii.output.append(message+"\n");
	}

}
