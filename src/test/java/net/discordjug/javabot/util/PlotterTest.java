package net.discordjug.javabot.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class PlotterTest {
    private static final Color[] PALETTE = {
            new Color(0xF9C74F), new Color(0xF9506E), new Color(0x6C7280),
            new Color(0x5B8CFF), new Color(0x43D9AD), new Color(0xC084FC),
            new Color(0x22D3EE), new Color(0xFB923C)
    };

    @Test
    public void ImageDifferenceTest(){
        List<Pair<String, Plotter.Bar>> testData = testData();
        BufferedImage img1 = new Plotter(testData, "General helper statistics","subtitle").plot();
        BufferedImage img2 = readImage("src/test/resources/PlotterTest.png");

        assertTrue(compareImage(img1,img2));
    }

    public static BufferedImage readImage(String path){
        try {
            File inputFile = new File(path);
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                throw new IOException("Image is null.");
            }
            return image;
        } catch (IOException e) {
            System.err.println("Error reading the image file: " + e.getMessage());
            return null;
        }
    }

    public static boolean compareImage(BufferedImage img1, BufferedImage img2) {
        if (img1 == null || img2 == null) {
            return false;
        }

        if (img1.getWidth() != img2.getWidth() ||
                img1.getHeight() != img2.getHeight()) {
            return false;
        }

        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Pair<String, Plotter.Bar>> testData() {
        Random random = new Random(42);
        List<Pair<String, Plotter.Bar>> entries = new ArrayList<>();
        YearMonth month = YearMonth.of(2026,9);

        for (int i = 0; i < 13; i++) {
            int segments = random.nextInt(3,5);
            List<Pair<Color, Double>> parts = new ArrayList<>();

            for (int s = 0; s < segments; s++) {
                double value = random.nextDouble(50,1800);
                parts.add(new Pair<>(PALETTE[s % PALETTE.length], value));
            }

            String monthLabel = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(Locale.ROOT) + " " + month.getYear();
            entries.add(new Pair<>(monthLabel, new Plotter.Bar(parts)));
            month = month.plusMonths(1);
        }
        return entries;
    }
}
