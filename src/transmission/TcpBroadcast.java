
package transmission;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

//import javax.swing.SwingWorker;

//import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import Wuuii.Wuuii;
import audio.AudioOption;
import audio.AudioRecord;
import audio.TaskTone;
import options.Getoptions;
import error.ErrorMessage;

public class TcpBroadcast {
	
	protected void ServerTransmission() throws IOException {
		Getoptions opt = new Getoptions();
		AudioOption ao = new AudioOption();
		
		Charset charset = Charset.forName("ISO-8859-1");
		CharsetDecoder decoder = charset.newDecoder();
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		Selector selector = Selector.open();
		
		ServerSocketChannel server = ServerSocketChannel.open();
		server.socket().bind(new java.net.InetSocketAddress(5555));
		server.configureBlocking(false);
		
		SelectionKey serverkey = server.register(selector, SelectionKey.OP_ACCEPT);	
		AudioFormat format = ao.getTransmissionFormat();
		SourceDataLine	sLine = null;

    	while(true) {
			selector.select();
			Set<?> keys = selector.selectedKeys();
			for(Iterator<?> i=keys.iterator(); i.hasNext();) {
				SelectionKey key = (SelectionKey)i.next();
				i.remove();
				
				if(key == serverkey) {
					if(key.isAcceptable()) {
						SocketChannel client = server.accept();
						client.configureBlocking(false);
						SelectionKey clientkey = client.register(selector, SelectionKey.OP_READ);
						clientkey.attach(new Integer(0));
					}
				} else {
					SocketChannel client = (SocketChannel)key.channel();
					if(!key.isReadable())
						continue;
					int bytesread = client.read(buffer);
					if(bytesread == -1 || opt.getHangup()) {
						if(Wuuii.DEBUG)
							opt.prFlush("Bytes not read kill Client connection");
						key.cancel();
						client.close();
						opt.setTransmission(false);
						opt.setHandshake(true);
						continue;
					}
					
					buffer.flip();
					String request = decoder.decode(buffer).toString();
					if(opt.getTransmission()) {
						Wuuii.audio = new byte[buffer.remaining()];
						Wuuii.audio = buffer.array();			
						try {
		    				if(opt.getHandshake() && opt.getTransmission()) {
		    					opt.setHandshake(false);
		    					DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format, 40960);
		    					sLine = (SourceDataLine)AudioSystem.getLine(sourceInfo);
		    					sLine.open(format, 40960);
		    					sLine.start();
		    				}
		    				Wuuii.Spectrum(Wuuii.audio, format, Wuuii.sendCapture);
		    				sLine.write(Wuuii.audio, 0, Wuuii.audio.length);	
		    			} catch (LineUnavailableException e) {
		    				ErrorMessage error = new ErrorMessage("Line unavailable: " + e);
		    				error.EMessage();
		    			}
						buffer.clear();
					} else {
						buffer.clear();
					}
					
					if(opt.getHangup()) {
						if(Wuuii.DEBUG) {
							opt.jprint("HANGUP send signal..");
							opt.prFlush("HANGUP from server");
						}
						key.cancel();
						client.close();
						sLine.drain();
						sLine.close();
						continue;
					}
					
					if(request.trim().equals("CALL")) {
						InetAddress address = client.socket().getInetAddress();
    					String ip = address.toString().split("/")[1];
						opt.jprint(request + "  from " + ip);
						(Wuuii.tone = new TaskTone()).execute();
						while(!opt.getTransmission() && !opt.getHangup()) {
							System.out.flush();
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) { 
								e.printStackTrace();
							}
						}
						if(opt.getTransmission()) {
							client.write(ByteBuffer.wrap("RESPOND".getBytes()));
	    					ClientTransmission(ip, 5555, true, 2);
							opt.setHandshake(true);
						}
						if(opt.getHangup()) {
							client.write(ByteBuffer.wrap("HANGUP".getBytes()));
							opt.setTransmission(false);
						}
					}
				}
			}
		}
	}
	
	protected void ClientTransmission(final String host, final int port, final boolean join, final int channel) throws IOException {
		Runnable runner = new Runnable() {
			public void run() {	
				AudioRecord ar = new AudioRecord();
				Getoptions opt = new Getoptions();
				ByteBuffer rbuff = ByteBuffer.allocate(512);		
				Charset charset = Charset.forName("ISO-8859-1");
				CharsetDecoder decoder = charset.newDecoder();	
				String CALL_REQUEST = "CALL";
				SocketChannel sc = null;

				if(Wuuii.DEBUG) {
					opt.jprint(CALL_REQUEST+" "+host);
					System.out.println(CALL_REQUEST + " " + host);
				}	
				try {
					sc = SocketChannel.open();
					sc.configureBlocking(false);
					sc.connect(new InetSocketAddress(host, 5555));
					while (!sc.finishConnect()) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace(); 
						}
					}
					if(!opt.getTransmission()) {
						ByteBuffer buffer = ByteBuffer.wrap(CALL_REQUEST.getBytes());
						sc.write(buffer);
						buffer.clear();
						while(sc.read(rbuff) != -1) {
							rbuff.flip();
							String response = decoder.decode(rbuff).toString();
							rbuff.clear();
							if(response.trim().equals("RESPOND")) {
								if(Wuuii.DEBUG) {
									opt.jprint(response + " from " + host);
									System.out.println(response + " from " + host);
								}
								opt.setTransmission(true);
								break;
							}
							rbuff.clear();		
						}	
					}
					if(opt.getTransmission()) {
						if(Wuuii.DEBUG) {
							opt.jprint("Start transmission ...");
							opt.prFlush("Start transmission");
						}
						ar.sendAudioSocks(sc, join, channel);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}  finally {
					if(Wuuii.DEBUG) {
						opt.jprint("HANGUP Client connection closed");
						opt.prFlush("HANGUP Client connection closed");
					}
					if (sc != null) {
						try {
							opt.setTransmission(false);
							opt.setHandshake(true);
							sc.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}	
					
				}
			}
		};
		
		Thread ThClient = new Thread(runner);
		ThClient.start();
	}
}