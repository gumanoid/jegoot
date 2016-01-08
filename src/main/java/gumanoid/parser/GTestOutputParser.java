package gumanoid.parser;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Transforms sequence of strings (Google Test output lines)
 * into sequence of events. Each event has corresponding
 * method in {@link EventListener}
 *
 * Created by Gumanoid on 07.01.2016.
 */
public class GTestOutputParser {
    public interface EventListener {
        void outputBeforeSuiteStarted(String outputLine);
        void suiteStart(int testCount, int testGroupCount);
        void suiteEnd(int testCount, int testGroupCount);

        void groupStart(String groupName, int testsInGroup);
        void groupEnd(String groupName, int testsInGroup);

        void testStart(String groupName, String testName);
        void testOutput(String outputLine);
        void testPassed(String groupName, String testName);
        void testFailed(String groupName, String testName);

        void passedTestsSummary(int passedTestCount);
        void failedTestsSummary(int failedTestCount);
        void failedTestSummary(String groupName, String failedTest);
        void summaryOutput(String outputLine);
    }

    private static final Pattern SUITE_START_PATTERN = Pattern.compile(" Running (\\d+) tests from (\\d+) test cases\\.$");
    private static final Pattern SUITE_END_PATTERN = Pattern.compile(" (\\d+) tests from (\\d+) test cases ran\\. (?:\\((\\d+) ms total\\))?$");

    private static final Pattern TEST_ENV_SETUP_PATTERN = Pattern.compile(" Global test environment set-up\\.$");
    private static final Pattern TEST_ENV_TEARDOWN_PATTERN = Pattern.compile(" Global test environment tear-down$");

    private static final Pattern GROUP_BOUNDARY = Pattern.compile(" (\\d+) tests from (\\w+)(?: \\((\\d+) ms total\\))?$");

    private static final Pattern TEST_START_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)$");
    private static final Pattern TEST_PASSED_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$");
    private static final Pattern TEST_FAILED_PATTERN = Pattern.compile(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$");

    private static final Pattern PASSED_SUMMARY_PATTERN = Pattern.compile(" (\\d+) test\\.$");
    private static final Pattern FAILED_SUMMARY_PATTERN = Pattern.compile(" (\\d+) tests, listed below:$");

    private final EventListener listener;

    private enum SuiteState { NotStarted, Running, Finished}

    private SuiteState suiteState = SuiteState.NotStarted;
    private String currentGroup = null;

    public GTestOutputParser(EventListener listener) {
        this.listener = listener;
    }

    public void onNextLine(String line) {
        boolean specialLine =
                prefix(line, "[==========]", (str, index) ->
                        pattern(str, index, SUITE_START_PATTERN, args -> suiteStart(parseInt(args[0]), parseInt(args[1])))
                        ||
                        pattern(str, index, SUITE_END_PATTERN, args -> suiteEnd(parseInt(args[0]), parseInt(args[1])))
                ) ||
                prefix(line, "[----------]", (str, index) ->
                        pattern(str, index, TEST_ENV_SETUP_PATTERN, args -> {})
                        ||
                        pattern(str, index, TEST_ENV_TEARDOWN_PATTERN, args -> {})
                        ||
                        pattern(str, index, GROUP_BOUNDARY, args -> groupBoundary(args[1], parseInt(args[0])))
                ) ||
                prefix(line, "[ RUN      ]", (str, index) ->
                        pattern(str, index, TEST_START_PATTERN, args -> testStart(args[0], args[1]))
                ) ||
                prefix(line, "[       OK ]", (str, index) ->
                        running() && pattern(str, index, TEST_PASSED_PATTERN, args -> testPassed(args[0], args[1]))
                ) ||
                prefix(line, "[  PASSED  ]", (str, index) ->
                        finished() && pattern(str, index, PASSED_SUMMARY_PATTERN, args -> passedSummary(parseInt(args[0])))
                ) ||
                prefix(line, "[  FAILED  ]", (str, index) ->
                        (running() && pattern(str, index, TEST_FAILED_PATTERN, args -> testFailed(args[0], args[1])))
                        ||
                        (finished() && pattern(str, index, TEST_FAILED_PATTERN, args -> failedTestSummary(args[0], args[1])))
                        ||
                        (finished() && pattern(str, index, FAILED_SUMMARY_PATTERN, args -> failedSummary(parseInt(args[0]))))
                );

        if (!specialLine) {
            switch (suiteState) {
                case NotStarted:
                    listener.outputBeforeSuiteStarted(line);
                    break;

                case Running:
                    listener.testOutput(line);
                    break;

                case Finished:
                    listener.summaryOutput(line);
                    break;
            }
        }
    }

    private void suiteStart(int testCount, int groupCount) {
        assert suiteState == SuiteState.NotStarted;

        suiteState = SuiteState.Running;
        listener.suiteStart(testCount, groupCount);
    }

    private void suiteEnd(int testCount, int groupCount) {
        assert running();

        suiteState = SuiteState.Finished;
        listener.suiteEnd(testCount, groupCount);
    }

    private void groupBoundary(String groupName, int testCount) {
        assert running();

        if (currentGroup == null) {
            currentGroup = groupName;
            listener.groupStart(groupName, testCount);
        } else {
            currentGroup = null;
            listener.groupEnd(groupName, testCount);
        }
    }

    private void testStart(String groupName, String testName) {
        listener.testStart(groupName, testName);
    }

    private void testPassed(String groupName, String testName) {
        listener.testPassed(groupName, testName);
    }

    private void passedSummary(int passedTestCount) {
        listener.passedTestsSummary(passedTestCount);
    }

    private void testFailed(String groupName, String testName) {
        listener.testFailed(groupName, testName);
    }

    private void failedTestSummary(String groupName, String testName) {
        listener.failedTestSummary(groupName, testName);
    }

    private void failedSummary(int failedTestCount) {
        listener.failedTestsSummary(failedTestCount);
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
            onMatch.accept(getArgs(matcher));
        }
        return matches;
    }

    private static String[] getArgs(Matcher matcher) {
        String[] arguments = new String[matcher.groupCount()];
        for (int i = 0; i < arguments.length; ++i) {
            arguments[i] = matcher.group(i + 1); //group(0) is entire matched string
        }
        return arguments;
    }
}
