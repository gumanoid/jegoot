package gumanoid.parser;

/**
 * Transforms sequence of strings (Google Test output lines)
 * into sequence of events. Each event has corresponding
 * method in {@link EventListener}
 *
 * Created by Gumanoid on 07.01.2016.
 */
public class GTestOutputParser {
    public interface EventListener {
        void suiteStart(int testCount, int testGroupCount);
        void groupStart(String groupName, int testsInGroup);
        void groupEnd(String groupName, int testsInGroup);
        void testStart(String groupName, String testName);
        void testOutput(String outputLine);
        void testPassed(String groupName, String testName);

        void testFailed(String groupName, String testName);

        void suiteEnd(int testCount, int testGroupCount);

        void passedTestsSummary(int passedTestCount);

        void failedTestsSummary(int failedTestCount);

        void failedTestSummary(String groupname, String failedTest);
    }

    private final EventListener listener;

    public GTestOutputParser(EventListener listener) {
        this.listener = listener;
    }

    public void onNextLine(String line) {

    }
}
