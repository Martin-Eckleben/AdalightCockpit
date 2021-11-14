package adalight.cockpit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class App 
{
    public static void main(String[] args) {

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                final Adalight adalight = new Adalight();
                
                Color background = new Color(255,255,255);
                
                //Create and set up the window.
                JFrame frame = new JFrame("Adalight Cockpit");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(800, 500));
                
                JPanel panel = new JPanel();
                BoxLayout bl = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
                panel.setLayout(bl);
                Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
                panel.setBorder(padding);
                frame.setContentPane(panel);
                frame.getContentPane().setBackground(background);
                
                // add ui components
                JButton off_btn = new JButton("Off");
                off_btn.setBackground(Color.black);
                off_btn.setForeground(Color.white);
                off_btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adalight.setMode(Mode.OFF);
                    }
                });
                off_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(off_btn);
                
                JButton adalight_btn = new JButton("Adalight");
                adalight_btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adalight.setMode(Mode.Adalight);
                    }
                });
                adalight_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(adalight_btn);
                
                JButton colorswirl_btn = new JButton("Colorswirl");
                colorswirl_btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        adalight.setMode(Mode.Colorswirl);
                    }
                });
                colorswirl_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(colorswirl_btn);
                
                final JColorChooser colorchooser = new JColorChooser();
                colorchooser.setBackground(background);
                colorchooser.setPreviewPanel(new JPanel());
                colorchooser.getSelectionModel().addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        adalight.setMode(colorchooser.getColor());
                    }
                });
                panel.add(colorchooser);
                
                //Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
