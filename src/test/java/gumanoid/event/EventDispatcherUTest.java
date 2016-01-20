package gumanoid.event;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.testng.Assert.assertEquals;

@Test
public class EventDispatcherUTest {
    @Test void onlyDefaultHandler() throws Exception {
        List<GTestOutputEvent> handledByDefaultHandler = new ArrayList<>();

        EventDispatcher<GTestOutputEvent> dispatcher = new EventDispatcher<>(handledByDefaultHandler::add);

        GTestOutputEvent e1 = new GTestOutputEvent.SuiteStart("start", 1, 1);
        GTestOutputEvent e2 = new GTestOutputEvent.TestOutput("output", Optional.empty(), Optional.empty());
        GTestOutputEvent e3 = new GTestOutputEvent.SuiteEnd("end", 1, 1);

        dispatcher.accept(e1);
        dispatcher.accept(e2);
        dispatcher.accept(e3);

        assertEquals(handledByDefaultHandler, ImmutableList.of(e1, e2, e3));
    }

    @Test void oneHandler() throws Exception {
        List<GTestOutputEvent> handledByDefaultHandler = new ArrayList<>();
        List<GTestOutputEvent.TestOutput> handledByTestOutputHandler = new ArrayList<>();

        EventDispatcher<GTestOutputEvent> dispatcher = new EventDispatcher<>(handledByDefaultHandler::add);
        dispatcher.addHandler(GTestOutputEvent.TestOutput.class, handledByTestOutputHandler::add);

        GTestOutputEvent e1 = new GTestOutputEvent.SuiteStart("start", 1, 1);
        GTestOutputEvent e2 = new GTestOutputEvent.TestOutput("output", Optional.empty(), Optional.empty());
        GTestOutputEvent e3 = new GTestOutputEvent.SuiteEnd("end", 1, 1);

        dispatcher.accept(e1);
        dispatcher.accept(e2);
        dispatcher.accept(e3);

        assertEquals(handledByDefaultHandler, ImmutableList.of(e1, e3));
        assertEquals(handledByTestOutputHandler, ImmutableList.of(e2));
    }

    @Test void twoHandlersForDifferentClasses() throws Exception {
        List<GTestOutputEvent> handledByDefaultHandler = new ArrayList<>();
        List<GTestOutputEvent.SuiteStart> handledBySuiteStartHandler = new ArrayList<>();
        List<GTestOutputEvent.SuiteEnd> handledBySuiteEndHandler = new ArrayList<>();

        EventDispatcher<GTestOutputEvent> dispatcher = new EventDispatcher<>(handledByDefaultHandler::add);
        dispatcher.addHandler(GTestOutputEvent.SuiteStart.class, handledBySuiteStartHandler::add);
        dispatcher.addHandler(GTestOutputEvent.SuiteEnd.class, handledBySuiteEndHandler::add);

        GTestOutputEvent e1 = new GTestOutputEvent.SuiteStart("start", 1, 1);
        GTestOutputEvent e2 = new GTestOutputEvent.TestOutput("output", Optional.empty(), Optional.empty());
        GTestOutputEvent e3 = new GTestOutputEvent.SuiteEnd("end", 1, 1);

        dispatcher.accept(e1);
        dispatcher.accept(e2);
        dispatcher.accept(e3);

        assertEquals(handledByDefaultHandler, ImmutableList.of(e2));
        assertEquals(handledBySuiteStartHandler, ImmutableList.of(e1));
        assertEquals(handledBySuiteEndHandler, ImmutableList.of(e3));
    }

    @Test void twoHandlersForTheSameClass() throws Exception {
        List<GTestOutputEvent> handledByFirst = new ArrayList<>();
        List<GTestOutputEvent> handledBySecond = new ArrayList<>();

        EventDispatcher<GTestOutputEvent> dispatcher = new EventDispatcher<>(e -> {});
        dispatcher.addHandler(GTestOutputEvent.TestOutput.class, handledByFirst::add);
        dispatcher.addHandler(GTestOutputEvent.TestOutput.class, handledBySecond::add);

        GTestOutputEvent e1 = new GTestOutputEvent.TestOutput("output 1", Optional.empty(), Optional.empty());
        GTestOutputEvent e2 = new GTestOutputEvent.TestOutput("output 2", Optional.empty(), Optional.empty());

        dispatcher.accept(e1);
        dispatcher.accept(e2);

        assertEquals(handledByFirst, ImmutableList.of(e1, e2));
        assertEquals(handledBySecond, ImmutableList.of(e1, e2));
    }

    @Test void oneHandlerForTwoClasses() throws Exception {
        List<GTestOutputEvent> handled = new ArrayList<>();

        EventDispatcher<GTestOutputEvent> dispatcher = new EventDispatcher<>(e -> {});
        Consumer<GTestOutputEvent> handler = handled::add;
        dispatcher.addHandler(GTestOutputEvent.SuiteStart.class, handler);
        dispatcher.addHandler(GTestOutputEvent.SuiteEnd.class, handler);

        GTestOutputEvent e1 = new GTestOutputEvent.SuiteStart("start", 1, 1);
        GTestOutputEvent e2 = new GTestOutputEvent.SuiteEnd("end", 1, 1);

        dispatcher.accept(e1);
        dispatcher.accept(e2);

        assertEquals(handled, ImmutableList.of(e1, e2));
    }
}