package gumanoid.parser;

import com.google.common.base.Preconditions;
import gumanoid.event.GTestOutputEvent;
import gumanoid.event.GTestOutputEvent.*;
import rx.Observable;
import rx.Subscriber;

import java.util.Optional;

import static gumanoid.parser.ParserCore.*;
import static java.lang.Integer.parseInt;

/**
 * Parses GTest output lines sequence and emits corresponding events
 * (subclasses of {@link GTestOutputEvent} with appropriate values
 * extracted from lines
 *
 * Created by Gumanoid on 07.01.2016.
 */
public class GTestOutputParser implements Observable.Operator<GTestOutputEvent, String> {
    @Override
    public Subscriber<? super String> call(Subscriber<? super GTestOutputEvent> subscriber) {
        return new Subscriber<String>() {
            /**
             * Handles metadata printed by GTestOutput
             */
            private final LineParser metaOutputParser = firstOf(
                    ifMatches("\\[==========\\] Running (\\d+) test[s]? from (\\d+) test case[s]?\\.$").then(this::suiteStart),
                    ifMatches("\\[==========\\] (\\d+) test[s]? from (\\d+) test case[s]? ran\\.(?: \\((\\d+) ms total\\))?$").then(this::suiteEnd),
                    ifMatches("\\[----------\\] Global test environment set-up\\.$").then(this::envSetUp),
                    ifMatches("\\[----------\\] Global test environment tear-down$").then(this::envTearDown),
                    ifMatches("\\[----------\\] (\\d+) test[s]? from (\\w+)(?: \\((\\d+) ms total\\))?$").then(this::groupBoundary),
                    ifMatches("\\[ RUN      \\] (\\w+)\\.(\\w+)$").then(this::testStart),
                    ifMatches("\\[       OK \\] (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$").then(this::testPassed),
                    ifMatches("\\[  PASSED  \\] (\\d+) test[s]?\\.$").then(this::passedTestsSummary),
                    ifMatches("\\[  FAILED  \\] (\\w+)\\.(\\w+)(?: \\((\\d+) ms\\))?$").then(this::testFailed),
                    ifMatches("\\[  FAILED  \\] (\\d+) test[s]?, listed below:$").then(this::failedSummary)
            );

            private SuiteState suiteState = SuiteState.NotStarted;
            private Optional<String> currentGroup = Optional.empty();
            private int testsInCurrentGroup;
            private Optional<String> currentTest = Optional.empty();

            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(String line) {
                if (!metaOutputParser.parse(line).isPresent()) {
                    subscriber.onNext(suiteState.createOutputEvent(line, currentGroup, currentTest));
                }
            }

            private void suiteStart(String[] args) {
                Preconditions.checkState(suiteState == SuiteState.NotStarted);

                suiteState = SuiteState.Running;
                subscriber.onNext(new SuiteStart(args[0], parseInt(args[1]), parseInt(args[2])));
            }

            private void suiteEnd(String[] args) {
                Preconditions.checkState(suiteState == SuiteState.Running);

                ensureGroupEnded();

                suiteState = SuiteState.Finished;
                subscriber.onNext(new SuiteEnd(args[0], parseInt(args[1]), parseInt(args[2])));
            }

            private void envSetUp(String[] args) {
                Preconditions.checkState(suiteState == SuiteState.Running);

                subscriber.onNext(new TestOutput(args[0], Optional.empty(), Optional.empty()));
            }

            private void envTearDown(String[] args) {
                Preconditions.checkState(suiteState == SuiteState.Running);

                ensureGroupEnded();

                subscriber.onNext(new TestOutput(args[0], Optional.empty(), Optional.empty()));
            }

            private void groupBoundary(String[] args) {
                Preconditions.checkState(suiteState == SuiteState.Running);

                int testCount = parseInt(args[1]);

                if (currentGroup.isPresent() && currentGroup.get().equals(args[2])) {
                    currentGroup = Optional.empty();
                    subscriber.onNext(new GroupEnd(args[0], args[2], testCount));
                } else {
                    ensureGroupEnded();

                    currentGroup = Optional.of(args[2]);
                    testsInCurrentGroup = testCount;
                    subscriber.onNext(new GroupStart(args[0], args[2], testCount));
                }
            }

            private void ensureGroupEnded() {
                if (currentGroup.isPresent()) {
                    //this can be the case when elapsed time measurement is turned off
                    //todo sort out this corner-case with null output line
                    //remember line printed in suiteStart? or just skip it if it's null
                    //in default implementation? the latter seems to be more accurate
                    subscriber.onNext(new GroupEnd(null, currentGroup.get(), testsInCurrentGroup));
                    currentGroup = Optional.empty();
                }
            }

            private void testStart(String[] args) {
                Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
                Preconditions.checkState(!currentTest.isPresent());

                currentTest = Optional.of(args[2]);
                subscriber.onNext(new TestStart(args[0], args[1], args[2]));
            }

            private void testPassed(String[] args) {
                Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
                Preconditions.checkState(currentTest.isPresent() && currentTest.get().equals(args[2]));

                currentTest = Optional.empty();
                subscriber.onNext(new TestPassed(args[0], args[1], args[2]));
            }

            private void testFailed(String[] args) {
                if (suiteState == SuiteState.Running) {
                    Preconditions.checkState(currentGroup.isPresent() && currentGroup.get().equals(args[1]));
                    Preconditions.checkState(currentTest.isPresent() && currentTest.get().equals(args[2]));

                    currentTest = Optional.empty();
                    subscriber.onNext(new TestFailed(args[0], args[1], args[2]));
                } else {
                    Preconditions.checkState(suiteState == SuiteState.Finished);
                    failedTestSummary(args);
                }
            }

            private void passedTestsSummary(String[] args) {
                subscriber.onNext(new PassedTestsSummary(args[0], parseInt(args[1])));
            }

            private void failedTestSummary(String[] args) {
                subscriber.onNext(new FailedTestSummary(args[0], args[1], args[2]));
            }

            private void failedSummary(String[] args) {
                subscriber.onNext(new FailedTestsSummary(args[0], parseInt(args[1])));
            }
        };
    }

    private enum SuiteState {
        NotStarted {
            @Override
            GTestOutputEvent createOutputEvent(String line, Optional<String> testGroupName, Optional<String> testName) {
                return new OutputBeforeSuiteStarted(line);
            }
        },
        Running {
            @Override
            GTestOutputEvent createOutputEvent(String line, Optional<String> groupName, Optional<String> testName) {
                return new TestOutput(line, groupName, testName);
            }
        },
        Finished {
            @Override
            GTestOutputEvent createOutputEvent(String line, Optional<String> testGroupName, Optional<String> testName) {
                return new SummaryOutput(line);
            }
        };

        abstract GTestOutputEvent createOutputEvent(String line, Optional<String> testGroupName, Optional<String> testName);
    }
}
