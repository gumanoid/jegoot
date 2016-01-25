/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package gumanoid.parser;

import gumanoid.event.GTestListEvent;
import gumanoid.event.GTestListEvent.GroupAnnounce;
import gumanoid.event.GTestListEvent.TestAnnounce;
import rx.Observable;
import rx.Subscriber;

import static gumanoid.parser.ParserCore.firstOf;
import static gumanoid.parser.ParserCore.ifMatches;

/**
 * Parses test enumeration given by GTest executable when --gtest_list_tests flag is passed in
 *
 * @author Gumanoid (2016-01-25 19:24)
 */
public class GTestListParser implements Observable.Operator<GTestListEvent, String> {
    @Override
    public Subscriber<? super String> call(Subscriber<? super GTestListEvent> subscriber) {
        return new Subscriber<String>() {
            private final ParserCore.LineParser parser = firstOf(
                    ifMatches("^(.*)\\.$").then(this::group),
                    ifMatches("^  (.*)$").then(this::test)
            );

            private String currentGroup;

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
                parser.parse(line);
            }

            private void group(String[] args) {
                currentGroup = args[1];
                subscriber.onNext(new GroupAnnounce(currentGroup));
            }

            private void test(String[] args) {
                subscriber.onNext(new TestAnnounce(currentGroup, args[1]));
            }
        };
    }
}
