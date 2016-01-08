package gumanoid;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//done Run external process and display it's output in GUI
//done Support external process interruption
//done Parse GTest output
//todo Check more GTest output cases (singular/plural, time measuring on/off etc)
//todo Tests summary
//todo Invariant: "Kill process".enabled == process.isRunning
//todo Re-run only failed tests
//todo Check Linux platform
//todo TEST, TEST_F, TEST_P?

/**
 * Created by Gumanoid on 05.01.2016.
 */
public class GTestRunnerApp {
    public static void main(String[] args) throws Exception {
        JTextArea processOutput = new JTextArea(5, 20);
        processOutput.setFont(new Font("monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(processOutput);
        processOutput.setEditable(false);

        JButton killProcess = new JButton("Kill");

        JFrame mainWindow = new JFrame("Title");
        mainWindow.setLayout(new BoxLayout(mainWindow.getContentPane(), BoxLayout.PAGE_AXIS));
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.getContentPane().add(scrollPane, BorderLayout.CENTER);
        mainWindow.getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
        mainWindow.getContentPane().add(killProcess, BorderLayout.PAGE_END);
        mainWindow.pack();
        mainWindow.setVisible(true);

        Process testProcess = new ProcessBuilder("test_samples/test_samples.exe").redirectErrorStream(true).start();
        killProcess.addActionListener(e -> testProcess.destroyForcibly());

        System.out.println("started");

        String charset = "CP866"; //todo platform-specific encoding
//        String charset = "UTF-8";

        try (BufferedReader echoOutput = new BufferedReader(new InputStreamReader(testProcess.getInputStream(), charset))) {
            echoOutput.lines().forEach(line -> {
                System.out.println(line);
                processOutput.append(line + '\n');
            });
        }

        System.out.println("finished with exit code " + testProcess.waitFor());
    }
}
