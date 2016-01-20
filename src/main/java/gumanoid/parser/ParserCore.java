package gumanoid.parser;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiny DSL for defining regular expression based parsers
 *
 * Created by Gumanoid on 17.01.2016.
 */
class ParserCore {
    private ParserCore() {}

    @FunctionalInterface
    interface LineParser {
        Optional<String[]> parse(String line);

        default LineParser then(Consumer<String[]> consume) {
            return line -> {
                Optional<String[]> result = parse(line);
                result.ifPresent(consume);
                return result;
            };
        }
    }

    /**
     * Acts like an 'or' with short-circuiting
     *
     * @param first
     * @param others
     * @return
     */
    static LineParser firstOf(LineParser first, LineParser... others) {
        LineParser result = first;

        for (LineParser next : others) {
            LineParser chain = result;
            result = (line) -> {
                Optional<String[]> parseResult = chain.parse(line);
                return parseResult.isPresent()? parseResult : next.parse(line);
            };
        }

        return result;
    }

    /**
     * Second stage of GTest output classification.
     * <p/>
     * Constructs regexp-based LineParser. If input matches
     * <code>pattern</code>, then input line and <code>pattern</code> capture
     * groups are returned. Otherwise empty optional is returned
     *
     * @param pattern       regular expression to match input line against
     * @return LineParser which tells whether input matches <code>pattern</code>
     * and, if yes, returns also array of values (capture groups) parsed from input
     */
    static LineParser ifMatches(Pattern pattern) {
        return (line) -> {
            Matcher matcher = pattern.matcher(line);
            return matcher.find()? Optional.of(getArgs(matcher)) : Optional.empty();
        };
    }

    static LineParser ifMatches(String regexp) {
        return ifMatches(Pattern.compile(regexp));
    }

    private static String[] getArgs(Matcher matcher) {
        //group(0) is entire string, which is not included in groupCount()
        String[] arguments = new String[matcher.groupCount() + 1];
        for (int i = 0; i < arguments.length; ++i) {
            arguments[i] = matcher.group(i);
        }
        return arguments;
    }
}
