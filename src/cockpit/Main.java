package cockpit;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

	public static void main(String[] args) {

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	//Create and set up the window.
    	        JFrame frame = new JFrame("Adalight Cockpit");
    	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	        frame.setPreferredSize(new Dimension(500, 500));
    	        frame.getContentPane().setBackground( new Color(255, 255, 255) );
    	        
    	        //Add the ubiquitous "Hello World" label.
    	        JLabel label = new JLabel("Adalight Cockpit");
    	        frame.getContentPane().add(label);

    	        //Display the window.
    	        frame.pack();
    	        frame.setVisible(true);   
            }
        });
	}

}
