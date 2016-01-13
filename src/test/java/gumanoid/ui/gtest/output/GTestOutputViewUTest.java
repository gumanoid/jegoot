package gumanoid.ui.gtest.output;

import org.fest.swing.fixture.FrameFixture;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.swing.*;

import java.awt.*;

import static org.testng.Assert.*;

/**
 * Created by Gumanoid on 09.01.2016.
 */
@Test
public class GTestOutputViewUTest {
    FrameFixture window;
    JFrame ui;
    GTestOutputView outputView;

    @BeforeClass void initFrameFixture() {
        ui = new JFrame("Title");
        outputView = new GTestOutputView();
        ui.getContentPane().add(outputView, BorderLayout.CENTER);

        window = new FrameFixture(ui);
        window.show();
    }

    @BeforeMethod void clearOutputView() {
        outputView.clear();
    }

    @AfterClass void cleanUpFrameFixture() {
        window.cleanUp();
    }

    @DataProvider(name = "simpleOutputData")
    Object[][] simpleOutputData() {
        return new Object[][] {
                { "" },
                { "foobar" },
                { "some output line" },
        };
    }

    @Test(dataProvider = "simpleOutputData")
    void singleLeafUnderRoot(String outputLine) throws Exception {
        outputView.atRoot().addOutputLine(outputLine);
        assertEquals(window.tree(GTestOutputView.TREE_NAME).valueAt(0), outputLine);
    }

    @Test(dataProvider = "simpleOutputData")
    void singleBranchUnderRoot(String outputLine) throws Exception {
        outputView.atRoot().addCollapsible("suite", outputLine);
        assertEquals(window.tree(GTestOutputView.TREE_NAME).valueAt(0), outputLine);
    }
}