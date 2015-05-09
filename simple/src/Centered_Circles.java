import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Centered_Circles implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Growing Circles");
        gd.addNumericField("Start Radius", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        long start = System.currentTimeMillis();
        int w = 400, h = 400;
        ImageProcessor ip = new ByteProcessor(w, h);
        byte[] pixels = (byte[]) ip.getPixels();
        int i = 0;

        int circleWidth = (int) gd.getNextNumber();

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int intensity = getIntensity(x - (w / 2), y - (h / 2), circleWidth);
                pixels[i++] = (byte) intensity;
            }
        }

        new ImagePlus("Checker Board", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }

    public int getIntensity(int x, int y, int circleWidth)
    {
        double r = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        return ((int) Math.round(r / circleWidth) % 2) * 255;
    }
}