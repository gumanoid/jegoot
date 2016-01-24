package gumanoid.ui.gtest.output;

import gumanoid.ui.Icons;
import rx.Observable;
import rx.schedulers.SwingScheduler;

import javax.swing.*;

import java.awt.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Style elements (icons, animations, colors) for GTestOutputView & co
 *
 * Created by Gumanoid on 25.01.2016.
 */
public class GTestOutputRowStyle {
    static final Color COLOR_FAILED = Color.RED;
    static final Color COLOR_PASSED = Color.GREEN;
    static final Color COLOR_RUNNING = Color.BLUE;

    static final Icon TEST_PASSED_ICON = Icons.load("test_passed.png");
    static final Icon TEST_FAILED_ICON = Icons.load("test_failed.png");
    static final Icon GROUP_PASSED_ICON = Icons.load("group_passed.png");
    static final Icon GROUP_FAILED_ICON = Icons.load("group_failed.png");
    static final Icon SUITE_PASSED_ICON = Icons.load("suite_passed.png");
    static final Icon SUITE_FAILED_ICON = Icons.load("suite_failed.png");

    static final Observable<Icon> GRAY_SPINNER = Observable.range(1, 8)
            .map(i -> Icons.load("spinner_gray_" + i + ".png"))
            .toList()
            .flatMap(frames -> Observable.interval(125, MILLISECONDS, SwingScheduler.getInstance())
                    .map(index -> frames.get((int) (index % frames.size())))
            );

    static final Observable<Icon> RED_SPINNER = Observable.range(1, 8)
            .map(i -> Icons.load("spinner_red_" + i + ".png"))
            .toList()
            .flatMap(frames -> Observable.interval(125, MILLISECONDS, SwingScheduler.getInstance())
                    .map(index -> frames.get((int) (index % frames.size())))
            );
}
