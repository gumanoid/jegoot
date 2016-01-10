package gumanoid.ui.gtest.output;

import org.fest.swing.fixture.FrameFixture;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.swing.*;

import static org.testng.Assert.*;

/**
 * Created by Gumanoid on 09.01.2016.
 */
@Test
public class GTestOutputViewUTest {
    FrameFixture window;

    @BeforeMethod void initFestRobot() {
        JFrame windowUI = new JFrame("Title");
        windowUI.setLayout(new BoxLayout(windowUI.getContentPane(), BoxLayout.PAGE_AXIS));
//        windowUI.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        GTestOutputView view = new GTestOutputView();
        view.append("some output");
        windowUI.getContentPane().add(view);

        window = new FrameFixture(windowUI);
        window.show();
    }

    @AfterMethod void cleanUpFestRobot() {
        window.cleanUp();
    }

    @Test void singleRootBranch() throws Exception {

    }
}