package gumanoid;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//done Run external process and display it's output in GUI
//todo Support external process interruption
//todo Parse GTest output
//todo Tests summary
//todo Re-run only failed tests
//todo TEST, TEST_F, TEST_P?

/**
 * Created by Gumanoid on 05.01.2016.
 */
public class GTestRunnerApp {
    public static void main(String[] args) throws Exception {
        ProcessBuilder echo = new ProcessBuilder("cmd");

        JTextArea processOutput = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(processOutput);
        processOutput.setEditable(false);

        JFrame mainWindow = new JFrame("Title");
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.getContentPane().add(scrollPane, BorderLayout.CENTER);
        mainWindow.pack();
        mainWindow.setVisible(true);

        Process echoProcess = echo.start();

        try (BufferedReader echoOutput = new BufferedReader(new InputStreamReader(echoProcess.getInputStream(), "CP866"))) {
            echoOutput.lines().forEach(processOutput::append);
        }
    }
}
