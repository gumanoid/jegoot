package gumanoid;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Gumanoid on 05.01.2016.
 */
public class GTestRunnerApp {
    public static void main(String[] args) {
        JLabel label = new JLabel("Label");

        JFrame mainWindow = new JFrame("Title");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.getContentPane().add(label, BorderLayout.CENTER);
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
}
