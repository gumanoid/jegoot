package gumanoid.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Gumanoid on 20.01.2016.
 */
public class Icons {
    public static Icon load(String iconName) {
        try {
            BufferedImage image = ImageIO.read(Icons.class.getResource(iconName));
            return new ImageIcon(image.getScaledInstance(-1, 16, Image.SCALE_SMOOTH));
        } catch (IOException e) { //todo unwrap exception
            throw new RuntimeException("Can not read icon " + iconName, e);
        }
    }
}
