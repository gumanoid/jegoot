package gumanoid.ui;

import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Simple two-section progress bar
 *
 * Created by Gumanoid on 30.01.2016.
 */
public class DoubleProgressBar extends JProgressBar {
    private int value1, value2;
    private Color color1, color2;

    @Override
    public void paint(Graphics g) {
        boolean opaque = isOpaque();

        setOpaque(true);
        setForeground(color2);
        setValue(value2);
        super.paint(g);

        setOpaque(false);
        setForeground(color1);
        setValue(value1);
        super.paint(g);

        setOpaque(opaque);
    }

    public int getValue1() {
        return value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    public Color getColor1() {
        return color1;
    }

    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    public Color getColor2() {
        return color2;
    }

    public void setColor2(Color color2) {
        this.color2 = color2;
    }
}
