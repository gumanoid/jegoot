package gumanoid;

import gumanoid.parser.GTestOutputParser;
import gumanoid.ui.gtest.output.GTestOutputView;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

//done Run external process and display it's output in GUI
//done Support external process interruption
//done Parse GTest output
//done Check more GTest output cases (singular/plural, time measuring on/off etc)
//done Implement basic UI for displaying test output
//done Checkpoint 1: Select test executable, run it and show it's output in UI
//todo Make use of FEST for UI testing
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
        JFrame mainWindow = new JFrame("Title");
        JButton selectTestExe = new JButton("Select test executable...");
        GTestOutputView testOutputView = new GTestOutputView();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(Paths.get("").toAbsolutePath().toFile(), "test_samples"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Executable files", "exe")); //todo platform-specific extension

        selectTestExe.addActionListener(e -> {
            if (fileChooser.showOpenDialog(mainWindow) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            selectTestExe.setEnabled(false);

            String testExePath = fileChooser.getSelectedFile().toString();

            new SwingWorker<Void, String>() {
                GTestOutputParser parser = new GTestOutputParser(new GTestOutputParser.EventListener() {
                    @Override
                    public void outputBeforeSuiteStarted(String outputLine) {
                        testOutputView.append(outputLine);
                    }

                    @Override
                    public void suiteStart(int testCount, int testGroupCount) {
                        testOutputView.appendCollapsible("Suite", "suite");
                    }

                    @Override
                    public void suiteEnd(int testCount, int testGroupCount) {
                        testOutputView.appendCollapsible("Summary", "summary");
                    }

                    @Override
                    public void groupStart(String groupName, int testsInGroup) {
                        testOutputView.appendCollapsible(groupName + " with " + testsInGroup + " test(s)", "suite", groupName);
                    }

                    @Override
                    public void groupEnd(String groupName, int testsInGroup) {

                    }

                    @Override
                    public void testStart(String groupName, String testName) {
                        testOutputView.appendCollapsible(testName, "suite", groupName, testName);
                    }

                    @Override
                    public void testOutput(Optional<String> groupName, Optional<String> testName, String outputLine) {
                        if (groupName.isPresent()) {
                            if (testName.isPresent()) {
                                testOutputView.append(outputLine, "suite", groupName.get(), testName.get());
                            } else {
                                testOutputView.append(outputLine, "suite", groupName.get());
                            }
                        } else {
                            testOutputView.append(outputLine, "suite");
                        }
                    }

                    @Override
                    public void testPassed(String groupName, String testName) {
                        testOutputView.append("Passed", "suite", groupName, testName);
                    }

                    @Override
                    public void testFailed(String groupName, String testName) {
                        testOutputView.append("Failed", "suite", groupName, testName);
                    }

                    @Override
                    public void passedTestsSummary(int passedTestCount) {
                        testOutputView.append("Passed test(s): " + passedTestCount, "summary");
                    }

                    @Override
                    public void failedTestsSummary(int failedTestCount) {
                        testOutputView.append("Failed test(s): " + failedTestCount, "summary");
                    }

                    @Override
                    public void failedTestSummary(String groupName, String failedTest) {
                        testOutputView.append("Failed test: " + failedTest + " in group " + groupName, "summary");
                    }

                    @Override
                    public void summaryOutput(String outputLine) {
                        testOutputView.append(outputLine, "summary");
                    }
                });

                @Override
                protected Void doInBackground() throws Exception {
                    Process testProcess = new ProcessBuilder(testExePath).redirectErrorStream(true).start();

                    String charset = "CP866"; //todo platform-specific encoding

                    try (BufferedReader echoOutput = new BufferedReader(new InputStreamReader(testProcess.getInputStream(), charset))) {
                        echoOutput.lines().forEach(this::publish);
                    }

                    System.out.println(testExePath + " finished with exit code " + testProcess.waitFor());

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    chunks.forEach(parser::onNextLine);
                }

                @Override
                protected void done() {
                    selectTestExe.setEnabled(true);
                }
            }.execute();
        });

        mainWindow.setLayout(new BoxLayout(mainWindow.getContentPane(), BoxLayout.PAGE_AXIS));
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainWindow.getContentPane().add(selectTestExe, BorderLayout.PAGE_START);
        mainWindow.getContentPane().add(testOutputView, BorderLayout.CENTER);
        mainWindow.getContentPane().add(Box.createRigidArea(new Dimension(0, 10)));
        mainWindow.pack();
        mainWindow.setVisible(true);
    }
}
