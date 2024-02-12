package gr.aueb.dmst.dds.jmcqg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Convert text to an image file */
public class Imager {

    /** Return out the class's string as an image */
    public static BufferedImage getImage(String text) {
        // Create a dummy BufferedImage to calculate text dimensions
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dummy.createGraphics();

        // Set font and color
        Font font = new Font("Lucida Console", Font.PLAIN, 12);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
 
        // Split text into lines
        String[] lines = text.split("\n");
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, fm.stringWidth(line));
        }
        int lineHeight = fm.getHeight();
        int height = lineHeight * lines.length;

        // Create the actual image with the correct dimensions
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // Draw each line of text
        int y = fm.getAscent();
        for (String line : lines) {
            g2d.drawString(line, 0, y);
            y += lineHeight;
        }
        g2d.dispose();

        return image;
    }
}
