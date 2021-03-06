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
//fixed Run test_samples; press re-run -> java.lang.ArrayIndexOutOfBoundsException: -1 @ GTestOutputTreeModel$BranchNodeWithQueue.take(GTestOutputTreeModel.java:295)
//fixed 'Re-run' button sometimes isn't enabled
//fixed UI quirk: after 'rerun tests', there is a space between bread crumbs and output tree, which translates clicks into tree O_o
//It appears only if 'rerun' clicked when tree scrollbar is not shown
//fixed 'Re-run' and 'cancel' starts infinite sequence of test exe launches
//fixed list of failed tests is being kept incorrectly
//fixme bread crumbs jitter if there is output before suite (e. g. re-run failed)

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
//done Query tests list before execution
//done Tests summary
//todo Cover GTestOutputTreeModel with tests
//todo Cover GTestViewController with tests
//done UI, output tree: test group names are sometimes truncated
//(strange, but it's fixed with JTree#setLargeModel(true)
//done Use Guice's method interceptor to simplify event emitting/receiving
//another option is Guava EventBus https://github.com/google/guava/wiki/EventBusExplained
//done UI polishing: two-section progress bar, to show failed/passed ratio
//done UI polishing: breadcrumbs
//todo Passing in test flags
//done Make use of FEST for UI testing
//done Invariant: "Kill process".enabled == process.isRunning
//done Re-run only failed tests
//done Check Linux platform
//todo TEST, TEST_F, TEST_P?
//todo Check Mac platform
//todo Cancel running test on window closing
//todo Make use of RxSwing (see examples: https://github.com/Petikoch/Java_MVVM_with_Swing_and_RxJava_Examples)
//todo Tests shuffling (--gtest_shuffle) and repeating (--gtest_repeat=N)

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
