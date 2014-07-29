package Wuuii;

import info.Info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import audio.AudioPlay;
import audio.AudioRecord;
import audio.TaskTone;
import options.Getoptions;
import transmission.TcpBroadcast;

/*
import Wuuii.AudioPlay;
import Wuuii.AudioRecord;
import Wuuii.TcpBroadcast;
*/

public class Wuuii extends TcpBroadcast {
	public static Vector<Line2D.Double> sendCapture = new Vector<Line2D.Double>();
	public static Vector<Line2D.Double> recvCapture = new Vector<Line2D.Double>();
	public static ByteArrayOutputStream out;
	public static byte[] audio;
	public static boolean DEBUG = true;	
	public static JFrame frame;
	public static TaskTone tone;	
	public static JTextArea output;
	
    public JTextField host, port;
    public JCheckBoxMenuItem spectrum;
    public Graphics2D g2D;
    
    private JPanel spectrum1, spectrum2;
    private JButton play, record, stop, caller, hangup, respond;  //audioTest
        
    public static void Spectrum(byte[] audioBytes, AudioFormat format, Vector<Line2D.Double> SpectrumData) {
    	SpectrumData.removeAllElements();
    	int h=105, w=689;
        int[] audioData = null;
        int nlengthInSamples = audioBytes.length/2;
        audioData = new int[nlengthInSamples];

        for (int i = 0; i < nlengthInSamples; i++) {
        	int LSB = (int)audioBytes[2*i];
            int MSB = (int)audioBytes[2*i+1];
            audioData[i] = MSB << 8 | (255 & LSB);
        }

        int frames_per_pixel = audioBytes.length/format.getFrameSize()/w;
        byte my_byte = 0;
        double y_last = 0;
        int numChannels = format.getChannels();
        
        for (double x=0; x<w && audioData != null; x++) {
            int idx = (int) (frames_per_pixel * numChannels * x);
            my_byte = (byte) (128 * audioData[idx] / 32768 );        
            double y_new = (double) (h * (128 - my_byte) / 256);
            SpectrumData.add(new Line2D.Double(x+25.F, y_last+35.F, x+25.F, y_new+35.F));
            frame.repaint();
            y_last = y_new;
        }
    }
    
    public static class CallerSpectrum extends JPanel {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			Getoptions opt = new Getoptions();
    	    super.paintComponent(g); 
    	    Graphics2D g2D;
    	    g2D = (Graphics2D) g;
    		g.drawRect(20,20,700,130);  
    	    g.setColor(Color.BLACK);  
    	    g.fillRect(20,20,700,130);  
    	    g.setColor(Color.YELLOW);
    	    g.drawRect(25,25,690,120);
    	    while(opt.getTestAudio() && sendCapture.size() > 0 && opt.getSpectrum() || opt.getTransmission() && sendCapture.size() > 0 && opt.getSpectrum()) {
    	    	for(int i=0; i<sendCapture.size()-1; i++) {
    	    		try {
    	    		g2D.draw((Line2D) (sendCapture.get(i)));
    	    		g2D.setColor(Color.GREEN);
    	    		} catch(IndexOutOfBoundsException e) {
    	    			break;
    	    		}
    	    	}
    	   }
    	}
    }
    
    public static class RecieverSpectrum extends JPanel {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			Getoptions opt = new Getoptions();
    	    super.paintComponent(g); 
    	    Graphics2D g2D;
    	    g2D = (Graphics2D) g;
    		g.drawRect(20,20,700,130);  
    	    g.setColor(Color.BLACK);  
    	    g.fillRect(20,20,700,130);  
    	    g.setColor(Color.YELLOW);
    	    g.drawRect(25,25,690,120);
    	    int i = 0;
    	    while(opt.getRecord() && recvCapture.size() > 0 && opt.getSpectrum() || opt.getTransmission() && recvCapture.size() > 0 && opt.getSpectrum()) {
    	    	try {
    	    		if(recvCapture.size()-1 >= i) {
    	    			g2D.draw((Line2D) (recvCapture.get(i)));
    	    			g2D.setColor(Color.GREEN);
    	    			i++;
    	    		} else {
    	    			i=0;
    	    		}
    	    	} catch(IndexOutOfBoundsException e) { 
    	    		i=0;
    	    	}
    	    }
    	}
    }
    
    public void init(){  
    	frame = new JFrame();
    	
    	final JMenuBar menu  = new JMenuBar();
    	JMenu main     = new JMenu("Main");
    	JMenu help     = new JMenu("Help");
    	JMenuItem exit = new JMenuItem("Exit");
    	JMenuItem info = new JMenuItem("Info");
    	
    	main.setMnemonic(KeyEvent.VK_M);
    	exit.setMnemonic(KeyEvent.VK_E);
    	info.setMnemonic(KeyEvent.VK_I);
    	
    	exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
    	
    	info.addActionListener(new ActionListener() {
    		@Override
            public void actionPerformed(ActionEvent event) {
    			Info info = new Info();
    			info.init();
            }
        });
    	
    	menu.add(main);
    	menu.add(Box.createHorizontalGlue());
    	menu.add(help);
    	
    	main.add(exit);
    	help.add(info);
    	frame.setJMenuBar(menu);
    	
    	JPanel call  = new JPanel();
    	JLabel hostl = new JLabel("host");
    	host         = new JTextField("192.168.123.188", 15);
    	JLabel portl = new JLabel("Port");
    	port         = new JTextField("5555", 6);
    	caller       = new JButton("Call");
    	hangup       = new JButton("Hang up");
    	respond      = new JButton("Respond");
    	spectrum     = new JCheckBoxMenuItem("Spectrum");
    	spectrum.setState(false);

    	ActionListener callerListener = new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			//TcpBroadcast tcp = new TcpBroadcast();
    			caller.setEnabled(false);
    			try {
    				ClientTransmission(host.getText(), Integer.parseInt(port.getText()),true, 2);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
    			caller.setEnabled(true);
    		}
    	};
    	
    	ActionListener respondListener = new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			Getoptions opt = new Getoptions();
    			opt.setTransmission(true);
    			tone.cancel(true);
    			try {
					Thread.sleep(40);
				} catch(InterruptedException e1) {} 
    			// opt.setTransmission(false);
    		}
    	};
    	
    	ActionListener hangupListener = new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			Getoptions opt = new Getoptions();
    			opt.setHangup(true);
    			opt.setTransmission(false);
    			if(opt.getHandshake())
    				tone.cancel(true);
    			try {
    				Thread.sleep(800);
    			} catch(InterruptedException e1) { }	
    			opt.setHangup(false);
    		}
    	};
    	
    	ActionListener spectrumListener = new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			Getoptions opt = new Getoptions();
    			if(spectrum.getState())
    				opt.setSpectrum(true);
    			else
    				opt.setSpectrum(false);
    		}
    	};
    	
    	KeyListener PortControlInput = new KeyListener() {
    		@Override
    		public void keyPressed(KeyEvent e) { }
    		
    		@Override
    		public void keyReleased(KeyEvent e) { }
    		
    		@Override
    		public void keyTyped(KeyEvent e) {
    			char c = e.getKeyChar();
    			if(!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
    				e.consume();
    			}
    		}
    	};
    	
    	KeyListener IpControlInput = new KeyListener() {
    		@Override
    		public void keyPressed(KeyEvent e) { }
    		
    		@Override 
    		public void keyReleased(KeyEvent e) { }
    		
    		@Override
    		public void keyTyped(KeyEvent e) {
    			char c = e.getKeyChar();
    			if(!((c >= '0') && (c <= '9') || (c == '.') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
    				e.consume();
    			}
    			//System.out.println(e.getKeyCode());
    		}
     	};
     	
     	host.addKeyListener(IpControlInput);
    	port.addKeyListener(PortControlInput);
    	caller.addActionListener(callerListener);
    	respond.addActionListener(respondListener);
    	hangup.addActionListener(hangupListener);
    	spectrum.addActionListener(spectrumListener);
    	
    	call.add(hostl);
    	call.add(host);
    	call.add(portl);
    	call.add(port);
    	call.add(caller);
    	call.add(hangup);
    	call.add(respond);
    	call.add(spectrum);

        spectrum1 = new CallerSpectrum();
        spectrum2 = new RecieverSpectrum();
        spectrum1.setBorder(BorderFactory.createTitledBorder(" Audio Spectrum Tx "));
        spectrum2.setBorder(BorderFactory.createTitledBorder(" Audio Spectrum Rx "));

        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sp.setResizeWeight(0.5);
        sp.setEnabled(false);
        sp.setDividerSize(0);
        sp.add(spectrum1);
        sp.add(spectrum2);	
        
    	frame.add(call, "North");
    	frame.add(sp);
    	
    	JSplitPane sp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	sp2.setResizeWeight(0.5);
        sp2.setEnabled(false);
        sp2.setDividerSize(0);
        
        JPanel shell = new JPanel(new BorderLayout());
        output = new JTextArea();
	    output.setForeground(Color.lightGray);
	    output.setBackground(Color.BLACK);
	    output.setFont(new Font("Arial", Font.TRUETYPE_FONT, 12));
	    output.setMinimumSize(new Dimension(200, 320));
	    DefaultCaret caret = (DefaultCaret)output.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    shell.setBorder(new EmptyBorder(new Insets(20, 20, 5, 20)));
	    shell.setPreferredSize(new Dimension(740, 100));
	    shell.add(new JScrollPane(output));
    	
        JPanel test = new JPanel();
    	play = new JButton("Play");
    	stop = new JButton("Stop");
        record = new JButton ("Record");
    	record.setEnabled(true);
    	play.setEnabled(false);
    	stop.setEnabled(false);
    	
    	sp2.add(shell);
    	sp2.add(test);
    	
    	ActionListener captureListener =  new ActionListener() {
    		AudioRecord ar = new AudioRecord();
    		
    		public void actionPerformed(ActionEvent e) {
    			//Getoption.setRecord(true);
    			record.setEnabled(false);
    		    stop.setEnabled(true);
    		    play.setEnabled(false);
    		    ar.capture();
    		}
    	};
    	
    	ActionListener stopListener = new ActionListener() {
    		
    		public void actionPerformed(ActionEvent e) {
    			Getoptions opt = new Getoptions();
    			record.setEnabled(true);
    			stop.setEnabled(false);
    		    play.setEnabled(true);
    		    opt.setRecord(false);
    		}
    	};
    	
    	ActionListener playListener = new ActionListener() {
    		AudioPlay ap = new AudioPlay();
    		
    		public void actionPerformed(ActionEvent e) {
    			ap.reproduction();
    		}
    	};
    	
    	play.addActionListener(playListener);
    	record.addActionListener(captureListener);
    	stop.addActionListener(stopListener);
    
    	test.add(record);
    	test.add(stop);
    	test.add(play);
    	
    	frame.add(sp2, "South");
        frame.setTitle("Wuuii Voip");
        frame.setSize(750, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
    }   
    
    public static void main(String[] args) {
    	
    	/*
    	System.setProperty("sun.java2d.opengl", "true");
    	System.setProperty("sun.java2d.translaccel", "true");
    	System.setProperty("sun.java2d.ddforcevram", "true");
		*/
    	
    	Wuuii wuuii = new Wuuii();
    	wuuii.init();
    	
    	//TcpBroadcast tcp = new TcpBroadcast();
    	try {
    		wuuii.ServerTransmission();
		} catch (IOException e) { }
    }
}