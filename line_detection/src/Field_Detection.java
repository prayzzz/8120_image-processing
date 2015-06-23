import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

/**
 * Created by Georg on 23.06.2015.
 */
public class Field_Detection implements PlugInFilter
{

    static File dir;

    int white = (255 << 16) | (255 << 8) | 255;
    int black = (0 << 16) | (0 << 8) | 0;
    int green = (0 << 16) | (255 << 8) | 0;

    int greenPercentTreshold = 45;
    int redPercentTreshold = 37;

    @Override
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    public void run(ImageProcessor sourceImageProcessor)
    {
        detectField(sourceImageProcessor);
    }

    public ImagePlus detectField(ImageProcessor ip)
    {
        ip = ip.convertToRGB();

        int h = ip.getHeight();
        int w = ip.getWidth();

        ImageProcessor ipNew = new ColorProcessor(w, h);

        //Sättigung erhöhen
        increaseSaturation(ip, h, w);

        // Grünes Histogramm errechnen
        int[] greenPercentHisto = calculateGreenPercentHisto(ip, h, w);

        // Maxwert des grünen Histogramms errechnen
        int maxGreenValue = getMaxValue(greenPercentHisto);

        // Alle grünen bereiche im Bild in weiße Bereiche umwandeln, alles andere in Schwarze
        turnGreenIntoWhite(ip, ipNew, h, w, maxGreenValue);

        // Medianfilter
        // ImageProcessor ipAfterMedian = medianFilter(ipNew, h, w, 4);

        // Alle Streuungen herausrechnen, das heißt vereinzelte Schwarze oder weiße Pixel entfernen
        ImageProcessor ipAfterEraseScattering = eraseScattering(ipNew, h, w, 10, 30, white);
        ImageProcessor ipAfterEraseScattering2 = eraseScattering(ipAfterEraseScattering, h, w, 30, 30, black);

        ImagePlus result = new ImagePlus("Spielfeld-Erkennung", ipAfterEraseScattering2.convertToByteProcessor());

        return result;
    }

    private void increaseSaturation(ImageProcessor ip, int h, int w)
    {

        double Pr = 0.299;
        double Pg = 0.587;
        double Pb = 0.114;
        double change = 1.2;

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {

                int[] rgb = new int[3];
                ip.getPixel(x, y, rgb);

                double R = rgb[0];
                double G = rgb[1];
                double B = rgb[2];

                double P = Math.sqrt((R) * (R) * Pr + (G) * (G) * Pg + (B)
                        * (B) * Pb);

                rgb[0] = (int) (P + ((R) - P) * change);
                rgb[1] = (int) (P + ((G) - P) * change);
                rgb[2] = (int) (P + ((B) - P) * change);

                ip.putPixel(x, y, rgb);

            }
        }
    }

    private ImageProcessor medianFilter(ImageProcessor ip, int h, int w,
                                        int windowSize)
    {
        ImageProcessor ipAfterMedian = new ColorProcessor(w, h);

        int windowHalf = windowSize / 2;
        int medianPosition = (windowSize * windowSize) / 2;
        int arraySize = windowSize * windowSize;

        int start = windowHalf;
        int endX = w - windowHalf;
        int endY = h - windowHalf;

        for (int x = start; x < endX; x++)
        {
            for (int y = start; y < endY; y++)
            {

                int[] window = new int[arraySize];
                int counter = 0;

                for (int x1 = -windowHalf; x1 < windowHalf; x1++)
                {
                    for (int y1 = -windowHalf; y1 < windowHalf; y1++)
                    {

                        int value = ip.getPixel(x + x1, y + y1);
                        window[counter] = value;
                        counter++;
                    }
                }
                Arrays.sort(window);
                int median = window[medianPosition];
                ipAfterMedian.putPixel(x, y, median);
            }

        }
        return ipAfterMedian;

    }

    private ImageProcessor eraseScattering(ImageProcessor ip, int h, int w, int surrounding, double tresshold, int colour)
    {

        ImageProcessor ipAfterEraseScattering = new ColorProcessor(w, h);

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {

                int value = ip.getPixel(x, y);
                if (value == colour)
                {
                    ipAfterEraseScattering.putPixel(x, y, colour);
                }
                else
                {
                    double pixelInUmgebung = 0;
                    double weissePixelInUmgebung = 0;

                    for (int x1 = -surrounding; x1 < surrounding; x1++)
                    {
                        for (int y1 = -surrounding; y1 < surrounding; y1++)
                        {
                            if ((x + x1 >= 0) && (x + x1 < w) && (y + y1 >= 0)
                                    && (y + y1 < h))
                            {
                                pixelInUmgebung++;
                                int adjacentPixelValue = ip.getPixel(x + x1, y
                                        + y1);
                                if (adjacentPixelValue == white)
                                {
                                    weissePixelInUmgebung++;
                                }
                            }
                        }
                    }

                    if ((weissePixelInUmgebung * 100 / pixelInUmgebung) > tresshold)
                    {
                        ipAfterEraseScattering.putPixel(x, y, white);
                    }
                    else
                    {
                        ipAfterEraseScattering.putPixel(x, y, black);
                    }
                }
            }
        }
        return ipAfterEraseScattering;
    }

    private ImageProcessor drawOnOriginal(ImageProcessor ip, ImageProcessor ipFeld, int h, int w)
    {

        ImageProcessor ipOnOriginal = new ColorProcessor(w, h);

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {

                int bild = ip.getPixel(x, y);
                int feld = ipFeld.getPixel(x, y);
                if (feld == white)
                {
                    ipOnOriginal.putPixel(x, y, green);
                }
                else
                {
                    ipOnOriginal.putPixel(x, y, bild);
                }
            }
        }
        return ipOnOriginal;
    }

    private void turnGreenIntoWhite(ImageProcessor ip, ImageProcessor ipNew,
                                    int h, int w, int maxGreenValue)
    {
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {

                int[] rgb = new int[3];
                ip.getPixel(x, y, rgb);

                int red = rgb[0];
                int green = rgb[1];
                int blue = rgb[2];

                int sum = red + green + blue;
                int greenPercent = (green * 100) / sum;

                int proximity = 8 - (4 * (sum / (3 * 255)));

                if ((greenPercent < maxGreenValue + proximity)
                        && (greenPercent > maxGreenValue - proximity)
                        && checkTresholds(rgb))
                {
                    ipNew.putPixel(x, y, white);
                }
                else
                {
                    ipNew.putPixel(x, y, black);
                }
            }
        }
    }

    private boolean checkTresholds(int[] rgb)
    {
        int red = rgb[0];
        int green = rgb[1];
        int blue = rgb[2];

        int sum = red + green + blue;
        int greenPercent = (green * 100) / sum;
        int redPercent = (red * 100) / sum;
        int bluePercent = (blue * 100) / sum;

        if ((green > red) && (green > blue)
                && (redPercent <= redPercentTreshold))
        {
            return true;
        }
        return false;
    }

    private int[] calculateGreenPercentHisto(ImageProcessor ip, int h, int w)
    {
        int[] greenPercentHisto = new int[101];

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                int[] rgb = new int[3];
                ip.getPixel(x, y, rgb);

                int red = rgb[0];
                int green = rgb[1];
                int blue = rgb[2];

                int sum = red + green + blue;
                int greenPercent = (green * 100) / sum;

                if (greenPercent >= greenPercentTreshold && checkTresholds(rgb))
                {
                    greenPercentHisto[greenPercent]++;
                }
            }
        }
        return greenPercentHisto;
    }

    // getting the maximum value
    public static int getMaxValue(int[] array)
    {
        int maxValue = array[0];
        int aktuellesI = 0;
        for (int i = 1; i < array.length; i++)
        {
            if (array[i] > maxValue)
            {
                maxValue = array[i];
                aktuellesI = i;
            }
        }
        return aktuellesI;
    }

    private int[] calculateHistogram(ImageProcessor ip, int h, int w, int color)
    {
        int[] histo = new int[256];

        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                int[] rgb = new int[3];
                ip.getPixel(x, y, rgb);

                int colorValue = rgb[color];
                histo[colorValue]++;
            }
        }
        return histo;
    }

    private ImagePlus getImage()
    {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null)
        {
            IJ.beep();
            IJ.showStatus("No image");
            return null;
        }
        return imp;
    }
}
