package info;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;

public class Info {
	final JLabel AUTHOR = new JLabel("<html>Created by<br><br>Domenico Pinto</html>", SwingConstants.CENTER);
	final String IMAGE = System.getProperty("user.dir") + "/src/img.jpg";
    final String DESCRIPTION = "<p><b>Simple Voice Over Ip</b></p>";
    
    public void init() {
    	JFrame Info    = new JFrame();
		JPanel back    = new JPanel();
		JPanel top     = new JPanel(new BorderLayout(0, 0));
        JPanel dePanel = new JPanel(new BorderLayout());
        JPanel box     = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        JTextPane pane = new JTextPane();
        ImageIcon image = new ImageIcon(IMAGE);
        JLabel img   = new JLabel(image);
        JSeparator separator = new JSeparator();
        
        back.setLayout(new BoxLayout(back, BoxLayout.Y_AXIS));
        Info.add(back);
       
        top.setMaximumSize(new Dimension(450, 0));
        AUTHOR.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        top.add(AUTHOR);

        img.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(img, BorderLayout.EAST);
        separator.setForeground(Color.gray);

        top.add(separator, BorderLayout.SOUTH);
        top.add(separator, BorderLayout.SOUTH);
        back.add(top);
        dePanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        pane.setContentType("text/html");
        pane.setText(DESCRIPTION);
        pane.setEditable(false);
       
        dePanel.add(pane);
        back.add(dePanel);
        back.add(box);

		Info.setSize(450, 415);
		Info.setLocationRelativeTo(null);
		Info.setTitle("Info Wuuii Voip");
		Info.setVisible(true);
    }

}
