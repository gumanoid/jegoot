package gumanoid.parser;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Classifies GTest output lines passed to {@link #onNextLine(String)} method,
 * and calls corresponding method of {@link ClassifiedGTestOutputHandler} instance
 * with appropriate arguments extracted from line passed in
 *
 * Created by Gumanoid on 07.01.2016.
 */
public class GTestOutputParser {
    private static final Pattern SUITE_START_PATTERN = Pattern.compile(" Running (\\d+) test[s]? from (\\d+) test case[s]?\\.$");
    private static final Pattern SUITE_END_PATTERN = Pattern.compile(" (\\d+) test[s]? from (\\d+) test case[s]? ran\\.(?: \\((\\d+) ms total\\))?$");

    private static final Pattern TEST_ENV_SETUP_PATTERN = Pattern.compile(" Global test environment set-up\\.$");
    private static final Pattern TEST_ENV_TEARDOWN_PATTERN = Pattern.compile(" Global test environment tear-down$");

    private static final Pattern GROUP_BOUNDARY = Pattern.compile(" (\\d+) test[s]? from (\\w+)(?: \\((\\d+) ms total\\))?$");

    private static final Pattern TEST_START_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)$");
    private static final Pattern TEST_PASSED_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$");
    private static final Pattern TEST_FAILED_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$");

    private static final Pattern PASSED_SUMMARY_PATTERN = Pattern.compile(" (\\d+) test[s]?\\.$");
    private static final Pattern FAILED_SUMMARY_PATTERN = Pattern.compile(" (\\d+) test[s]?, listed below:$");

    private final ClassifiedGTestOutputHandler listener;

    private enum SuiteState { NotStarted, Running, Finished}

    private SuiteState suiteState = SuiteState.NotStarted;
    private Optional<String> currentGroup = Optional.empty();
    private int testsInCurrentGroup;
    private Optional<String> currentTest = Optional.empty();

    public GTestOutputParser(ClassifiedGTestOutputHandler listener) {
        this.listener = listener;
    }

    public void onNextLine(String line) {
        boolean specialLine =
                //@formatter: off
                prefix(line, "[==========]", (str, index) ->
                        pattern(str, index, SUITE_START_PATTERN, args -> suiteStart(args[0], parseInt(args[1]), parseInt(args[2])))
                        ||
                        pattern(str, index, SUITE_END_PATTERN, args -> suiteEnd(args[0], parseInt(args[1]), parseInt(args[2])))
                ) ||
                prefix(line, "[----------]", (str, index) ->
                        pattern(str, index, TEST_ENV_SETUP_PATTERN, args -> envSetUp(args[0]))
                        ||
                        pattern(str, index, TEST_ENV_TEARDOWN_PATTERN, args -> envTearDown(args[0]))
                        ||
                        pattern(str, index, GROUP_BOUNDARY, args -> groupBoundary(args[0], args[2], parseInt(args[1])))
                ) ||
                prefix(line, "[ RUN      ]", (str, index) ->
                        pattern(str, index, TEST_START_PATTERN, args -> testStart(args[0], args[1], args[2]))
                ) ||
                prefix(line, "[       OK ]", (str, index) ->
                        running() && pattern(str, index, TEST_PASSED_PATTERN, args -> testPassed(args[0], args[1], args[2]))
                ) ||
                prefix(line, "[  PASSED  ]", (str, index) ->
                        finished() && pattern(str, index, PASSED_SUMMARY_PATTERN, args -> passedSummary(args[0], parseInt(args[1])))
                ) ||
                prefix(line, "[  FAILED  ]", (str, index) ->
                        (running() && pattern(str, index, TEST_FAILED_PATTERN, args -> testFailed(args[0], args[1], args[2])))
                        ||
                        (finished() && pattern(str, index, TEST_FAILED_PATTERN, args -> failedTestSummary(args[0], args[1], args[2])))
                        ||
                        (finished() && pattern(str, index, FAILED_SUMMARY_PATTERN, args -> failedSummary(args[0], parseInt(args[1]))))
                );
                //@formatter: on

        if (!specialLine) {
            regularOutput(line);
        }
    }

    private void regularOutput(String line) {
        switch (suiteState) {
            case NotStarted:
                listener.outputBeforeSuiteStarted(line);
                break;

            case Running:
                listener.testOutput(line, currentGroup, currentTest);
                break;

            case Finished:
                listener.summaryOutput(line);
                break;
        }
    }

    private void envSetUp(String outputLine) {
        assert running();

        listener.testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    private void envTearDown(String outputLine) {
        assert running();

        ensureGroupEnded();

        listener.testOutput(outputLine, Optional.empty(), Optional.empty());
    }

    private void suiteStart(String outputLine, int testCount, int groupCount) {
        assert suiteState == SuiteState.NotStarted;

        suiteState = SuiteState.Running;
        listener.suiteStart(outputLine, testCount, groupCount);
    }

    private void suiteEnd(String outputLine, int testCount, int groupCount) {
        assert running();

        ensureGroupEnded();

        suiteState = SuiteState.Finished;
        listener.suiteEnd(outputLine, testCount, groupCount);
    }

    private void ensureGroupEnded() {
        if (currentGroup.isPresent()) {
            //this can be the case when elapsed time measurement is turned off
            //todo sort out this corner-case with null output line
            //remember line printed in suiteStart? or just skip it if it's null
            //in default implementation? the latter seems to be more accurate
            listener.groupEnd(null, currentGroup.get(), testsInCurrentGroup);
            currentGroup = Optional.empty();
        }
    }

    private void groupBoundary(String outputLine, String groupName, int testCount) {
        assert running();

        if (currentGroup.isPresent() && currentGroup.get().equals(groupName)) {
            currentGroup = Optional.empty();
            listener.groupEnd(outputLine, groupName, testCount);
        } else {
            ensureGroupEnded();

            currentGroup = Optional.of(groupName);
            testsInCurrentGroup = testCount;
            listener.groupStart(outputLine, groupName, testCount);
        }
    }

    private void testStart(String outputLine, String groupName, String testName) {
        assert currentGroup.isPresent() && currentGroup.get().equals(groupName);
        assert !currentTest.isPresent();

        currentTest = Optional.of(testName);
        listener.testStart(outputLine, groupName, testName);
    }

    private void testPassed(String outputLine, String groupName, String testName) {
        assert currentGroup.isPresent() && currentGroup.get().equals(groupName);
        assert currentTest.isPresent() && currentTest.get().equals(testName);

        currentTest = Optional.empty();
        listener.testPassed(outputLine, groupName, testName);
    }

    private void testFailed(String outputLine, String groupName, String testName) {
        assert currentGroup.isPresent() && currentGroup.get().equals(groupName);
        assert currentTest.isPresent() && currentTest.get().equals(testName);

        currentTest = Optional.empty();
        listener.testFailed(outputLine, groupName, testName);
    }

    private void passedSummary(String outputLine, int passedTestCount) {
        listener.passedTestsSummary(outputLine, passedTestCount);
    }

    private void failedTestSummary(String outputLine, String groupName, String testName) {
        listener.failedTestSummary(outputLine, groupName, testName);
    }

    private void failedSummary(String outputLine, int failedTestCount) {
        listener.failedTestsSummary(outputLine, failedTestCount);
    }

    private boolean running() {
        return suiteState == SuiteState.Running;
    }

    private boolean finished() {
        return suiteState == SuiteState.Finished;
    }

    private static boolean prefix(String line, String prefix, BiPredicate<String, Integer> onMatch) {
        return line.startsWith(prefix) && onMatch.test(line, prefix.length());
    }

    private static boolean pattern(String line, int startIndex, Pattern pattern, Consumer<String[]> onMatch) {
        Matcher matcher = pattern.matcher(line);
        boolean matches = matcher.find(startIndex);
        if (matches) {
            onMatch.accept(getArgs(line, matcher));
        }
        return matches;
    }

    private static String[] getArgs(String line, Matcher matcher) {
        //group(0) is entire string, which is not included in groupCount()
        String[] arguments = new String[matcher.groupCount() + 1];
        arguments[0] = line; //matcher.group(0) doesn't include prefix
        for (int i = 1; i < arguments.length; ++i) {
            arguments[i] = matcher.group(i);
        }
        return arguments;
    }
}
