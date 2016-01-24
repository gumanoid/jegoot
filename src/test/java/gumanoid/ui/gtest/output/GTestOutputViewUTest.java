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
    GTestOutputTreeModel<GTestOutputRow> model = new GTestOutputTreeModel<>(null);

    @BeforeClass void initFrameFixture() {
        ui = new JFrame("Title");
        outputView = new GTestOutputView();
        outputView.getTree().setModel(model);
        ui.getContentPane().add(outputView, BorderLayout.CENTER);

        window = new FrameFixture(ui);
        window.show();
    }

    @BeforeMethod void clearOutputView() {
        model.clear();
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
        model.addOutput(model.rootNode(), new GTestOutputRow(outputLine));
        assertEquals(window.tree(GTestOutputView.TREE_NAME).valueAt(0), outputLine);
    }

    @Test(dataProvider = "simpleOutputData")
    void singleBranchUnderRoot(String outputLine) throws Exception {
        model.addSuite(new GTestOutputRow(outputLine));
        assertEquals(window.tree(GTestOutputView.TREE_NAME).valueAt(0), outputLine);
    }
}