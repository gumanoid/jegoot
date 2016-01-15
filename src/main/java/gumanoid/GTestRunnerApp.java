package gumanoid;

import gumanoid.ui.gtest.GTestView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

//fixme Elapsed time is different each time GTestRunnerIT starts test samples, fix test

//done Run external process and display it's output in GUI
//done Support external process interruption
//done Parse GTest output
//done Check more GTest output cases (singular/plural, time measuring on/off etc)
//done Implement basic UI for displaying test output
//done Checkpoint 1: Select test executable, run it and show it's output in UI
//done Keep all output lines, don't skip
//todo Parser tests: re-check binary output; test that 'env set up / tear down' lines are passed to handler
//todo UI polishing: status icons
//todo UI polishing: expanded/collapsed icons
//done UI polishing: colors
//todo UI polishing: two-section progress bar, to show failed/passed ratio
//todo Make use of FEST for UI testing
//todo Tests summary
//todo Invariant: "Kill process".enabled == process.isRunning
//done Re-run only failed tests
//done Check Linux platform
//todo TEST, TEST_F, TEST_P?
//todo Check Mac platform

/**
 * Created by Gumanoid on 05.01.2016.
 */
public class GTestRunnerApp {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(Paths.get("").toAbsolutePath().toFile(), "test_samples"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Executable files", "exe")); //todo platform-specific extension

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String testExePath = fileChooser.getSelectedFile().toString();

                JFrame mainWindow = new JFrame("Title");
                mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                mainWindow.getContentPane().add(new GTestView(testExePath), BorderLayout.CENTER);
                mainWindow.pack();
                mainWindow.setVisible(true);
            }
        });
    }
}
