package gumanoid.ui.gtest.output;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Gumanoid on 24.01.2016.
 */
public class GTestOutputRow {
    private Icon icon;
    private Color textColor;
    private String displayName;

    public GTestOutputRow(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
