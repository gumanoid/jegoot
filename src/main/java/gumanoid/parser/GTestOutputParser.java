package gumanoid.parser;

import com.google.common.base.Preconditions;

import java.util.Optional;
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
    private final ClassifiedGTestOutputHandler listener;
    private final LineMatcher isSpecialLine;

    private enum SuiteState { NotStarted, Running, Finished}

    private SuiteState suiteState = SuiteState.NotStarted;
    private Optional<String> currentGroup = Optional.empty();
    private int testsInCurrentGroup;
    private Optional<String> currentTest = Optional.empty();

    public GTestOutputParser(ClassifiedGTestOutputHandler listener) {
        this.listener = listener;

        //@formatter: off
        isSpecialLine = firstOf(
                ifStartsWith("[==========]", firstOf(
                        ifMatches(" Running (\\d+) test[s]? from (\\d+) test case[s]?\\.$", this::suiteStart),
                        ifMatches(" (\\d+) test[s]? from (\\d+) test case[s]? ran\\.(?: \\((\\d+) ms total\\))?$", this::suiteEnd)
                )),
                ifStartsWith("[----------]", firstOf(
                        ifMatches(" Global test environment set-up\\.$", this::envSetUp),
                        ifMatches(" Global test environment tear-down$", this::envTearDown),
                        ifMatches(" (\\d+) test[s]? from (\\w+)(?: \\((\\d+) ms total\\))?$", this::groupBoundary)
                )),
                ifStartsWith("[ RUN      ]", firstOf(
                        ifMatches(" (\\w+)\\.(\\w+)$", this::testStart)
                )),
                ifStartsWith("[       OK ]", firstOf(
                        ifRunningAnd(ifMatches(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$", this::testPassed))
                )),
                ifStartsWith("[  PASSED  ]", firstOf(
                        ifFinishedAnd(ifMatches(" (\\d+) test[s]?\\.$", this::passedTestsSummary))
                )),
                ifStartsWith("[  FAILED  ]", firstOf(
                        ifRunningAnd(ifMatches(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$", this::testFailed)),
                        ifFinishedAnd(ifMatches(" (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$", this::failedTestsSummary)),
                        ifFinishedAnd(ifMatches(" (\\d+) test[s]?, listed below:$", this::failedSummary))
                ))
        );
        //@formatter: on
    }

    public void onNextLine(String line) {
        if (!isSpecialLine.test(line, 0)) {
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
    }

    @FunctionalInterface
    private interface LineMatcher {
        boolean test(String line, int startIndex);
    }

    /**
     * Second stage of GTest output classification.
     * <p/>
     * Constructs regexp-based LineMatcher. If input matches
     * <code>regex</code>, then input line and <code>regex</code> capture
     * groups are passed to <code>onMatch</code> consumer, and LineMatcher returns true
     *
     * @param regex       regular expression to match <code>line</code> against
     * @param onMatch       what to do if <code>line</code> matches <code>pattern</code>
     * @return LineMatcher which tells whether input matches <code>regex</code>
     */
    LineMatcher ifMatches(String regex, Consumer<String[]> onMatch) {
        Pattern pattern = Pattern.compile(regex);

        return (line, startIndex) -> {
            Matcher matcher = pattern.matcher(line);
            boolean matches = matcher.find(startIndex);
            if (matches) {
                onMatch.accept(getArgs(line, matcher));
            }
            return matches;
        };
    }

    /**
     * First stage of GTest output classification.
     * <p/>
     * Constructs string prefix based LineMatcher. If input starts with <code>prefix</code>, then input
     * line (with startIndex increased by <code>prefix</code> length) is passed to <code>onMatch</code>
     * continuation predicate, and that predicate's return value is returned
     *
     * @param prefix     string which <code>line</code> should start from for the <code>onMatch</code>
     *                   continuation predicate to be invoked
     * @param onMatch    continuation predicate to invoke if <code>line</code> is started with
     *                   <code>prefix</code>
     * @return LineMatcher which tells if input line starts with <code>prefix</code> <b>and</b>
     * continuation LineMather returns true for the rest of the input line (after prefix)
     */
    LineMatcher ifStartsWith(String prefix, LineMatcher onMatch) {
        return (line, startIndex) -> line.startsWith(prefix, startIndex) && onMatch.test(line, prefix.length());
    }

    /**
     * Acts like an 'or' with short-circuiting
     *
     * @param first
     * @param others
     * @return
     */
    LineMatcher firstOf(LineMatcher first, LineMatcher... others) {
        LineMatcher result = first;

        for (LineMatcher predicate : others) {
            LineMatcher chain = result;
            result = (line, index) -> chain.test(line, index) || predicate.test(line, index);
        }

        return result;
    }

    private void suiteStart(String[] args) {
        Preconditions.checkState(suiteState == SuiteState.NotStarted);

        suiteState = SuiteState.Running;
        listener.suiteStart(args[0], parseInt(args[1]), parseInt(args[2]));
    }

    private void suiteEnd(String[] args) {
        Preconditions.checkState(running());

        ensureGroupEnded();

        suiteState = SuiteState.Finished;
        listener.suiteEnd(args[0], parseInt(args[1]), parseInt(args[2]));
    }

    private void envSetUp(String[] args) {
        Preconditions.checkState(running());

        listener.testOutput(args[0], Optional.empty(), Optional.empty());
    }

    private void envTearDown(String[] args) {
        Preconditions.checkState(running());

        ensureGroupEnded();

        listener.testOutput(args[0], Optional.empty(), Optional.empty());
    }

    private void groupBoundary(String[] args) {
        Preconditions.checkState(running());

        int testCount = parseInt(args[1]);

        if (currentGroup.isPresent() && currentGroup.get().equals(args[2])) {
            currentGroup = Optional.empty();
            listener.groupEnd(args[0], args[2], testCount);
        } else {
            ensureGroupEnded();

            currentGroup = Optional.of(args[2]);
            testsInCurrentGroup = testCount;
            listener.groupStart(args[0], args[2], testCount);
        }
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

    private void testStart(String[] args) {
        Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
        Preconditions.checkState(!currentTest.isPresent());

        currentTest = Optional.of(args[2]);
        listener.testStart(args[0], args[1], args[2]);
    }

    private void testPassed(String[] args) {
        Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
        Preconditions.checkState(currentTest.isPresent() && currentTest.get().equals(args[2]));

        currentTest = Optional.empty();
        listener.testPassed(args[0], args[1], args[2]);
    }

    private void testFailed(String[] args) {
        Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
        Preconditions.checkState(currentTest.isPresent() && currentTest.get().equals(args[2]));

        currentTest = Optional.empty();
        listener.testFailed(args[0], args[1], args[2]);
    }

    private void passedTestsSummary(String[] args) {
        listener.passedTestsSummary(args[0], parseInt(args[1]));
    }

    private void failedTestsSummary(String[] args) {
        listener.failedTestSummary(args[0], args[1], args[2]);
    }

    private void failedSummary(String[] args) {
        listener.failedTestsSummary(args[0], parseInt(args[1]));
    }

    private LineMatcher ifRunningAnd(LineMatcher continuation) {
        return (line, startIndex) -> running() && continuation.test(line, startIndex);
    }

    private LineMatcher ifFinishedAnd(LineMatcher continuation) {
        return (line, startIndex) -> finished() && continuation.test(line, startIndex);
    }

    private boolean running() {
        return suiteState == SuiteState.Running;
    }

    private boolean finished() {
        return suiteState == SuiteState.Finished;
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
