package gumanoid;

import gumanoid.ui.gtest.GTestView;
import gumanoid.ui.gtest.GTestViewController;
import sun.awt.OSInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;

//fixed Elapsed time is different each time GTestRunnerIT starts test samples, fix test

//done Run external process and display it's output in GUI
//done Support external process interruption
//done Parse GTest output
//done Check more GTest output cases (singular/plural, time measuring on/off etc)
//done Implement basic UI for displaying test output
//done Checkpoint 1: Select test executable, run it and show it's output in UI
//done Keep all output lines, don't skip
//done Parser tests: re-check binary output; test that 'env set up / tear down' lines are passed to handler
//done UI polishing: status icons
//done UI polishing: expanded/collapsed icons
//done UI polishing: icons animation
//done UI polishing: colors
//todo UI polishing: two-section progress bar, to show failed/passed ratio
//todo UI polishing: breadcrumbs
//todo Passing in test flags
//todo Query test lists before execution?
//done Make use of FEST for UI testing
//todo Tests summary
//done Invariant: "Kill process".enabled == process.isRunning
//done Re-run only failed tests
//done Check Linux platform
//todo TEST, TEST_F, TEST_P?
//todo Check Mac platform
//todo Cancel running test on window closing
//todo Make use of RxSwing (see examples: https://github.com/Petikoch/Java_MVVM_with_Swing_and_RxJava_Examples)

/**
 * Created by Gumanoid on 05.01.2016.
 */
public class GTestRunnerApp {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(Paths.get("").toAbsolutePath().toFile(), "test_samples"));
            if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
                fileChooser.setFileFilter(new FileNameExtensionFilter("Executable files", "exe"));
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String testExePath = fileChooser.getSelectedFile().toString();

                GTestView testView = new GTestView();
                GTestViewController controller = new GTestViewController(testView, testExePath);

                JFrame mainWindow = new JFrame("Title");
                mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                mainWindow.getContentPane().add(testView, BorderLayout.CENTER);
                mainWindow.pack();
                mainWindow.setVisible(true);

                controller.runAllTests();
            }
        });
    }
}
