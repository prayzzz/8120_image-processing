import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Checker_Board implements PlugIn
{
    public void run(String arg)
    {
        GenericDialog gd = new GenericDialog("Checkerboard");
        gd.addNumericField("Size", 33, 0);
        gd.showDialog();
        if (gd.wasCanceled())
        {
            return;
        }

        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();
        int i = 0;

        int size = (int) gd.getNextNumber();
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int xAxis = Math.floorDiv(x, size) % 2;
                int yAxis = Math.floorDiv(y, size) % 2;

                int color = xAxis == 1 ^ yAxis == 1 ? 255 : 0;

                pixels[i++] = (byte) color;
            }
        }

        new ImagePlus("Checker Board", ip).show();
    }

}
