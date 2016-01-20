package gumanoid.parser;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.Optional;
import java.util.regex.Pattern;

import static gumanoid.parser.ParserCore.*;
import static org.testng.Assert.*;

/**
 * Created by Gumanoid on 20.01.2016.
 */
@Test
public class ParserCoreUTest {
    static final String[] A = new String[]{"a"};
    static final String[] D = new String[]{"d"};
    public static final String[] AA = new String[]{"aa"};

    @Test
    void testFirstOfWithOneArg() throws Exception {
        LineParser parser = firstOf(
                line -> line.startsWith("a") ? Optional.of(A) : Optional.empty()
        );

        assertEquals(parser.parse("abc"), Optional.of(A));
        assertEquals(parser.parse("def"), Optional.empty());
    }

    @Test
    void testFirstOfWithTwoArgs() throws Exception {
        LineParser parser = firstOf(
                line -> line.startsWith("a") ? Optional.of(A) : Optional.empty(),
                line -> line.startsWith("d") ? Optional.of(D) : Optional.empty()
        );

        assertEquals(parser.parse("abc"), Optional.of(A));
        assertEquals(parser.parse("def"), Optional.of(D));
        assertEquals(parser.parse("ghi"), Optional.empty());
    }

    @Test
    void testFirstOfArgsOrder() throws Exception {
        LineParser parser = firstOf(
                line -> line.startsWith("aa") ? Optional.of(AA) : Optional.empty(),
                line -> line.startsWith("a") ? Optional.of(A) : Optional.empty()
        );

        assertEquals(parser.parse("abc"), Optional.of(A));
        assertEquals(parser.parse("aaa"), Optional.of(AA));
    }

    @Test
    void testIfMatches() throws Exception {
        Pattern pattern = Pattern.compile("digit (\\d)");

        for (LineParser parser: new LineParser[] {
                ifMatches(pattern),
                ifMatches(pattern.pattern())
        }) {
            Optional<String[]> result;

            result = parser.parse("digit 1");
            assertTrue(result.isPresent());
            assertEquals(result.get(), new String[] {"digit 1", "1"});

            result = parser.parse("digit 7");
            assertTrue(result.isPresent());
            assertEquals(result.get(), new String[] {"digit 7", "7"});

            result = parser.parse("no digits here");
            assertFalse(result.isPresent());
        }
    }
}