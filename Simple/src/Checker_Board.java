import ij.IJ;
import ij.ImagePlus;
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
        long start = System.currentTimeMillis();
        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();
        int i = 0;

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int xAxis = Math.floorDiv(x, 20) % 2;
                int yAxis = Math.floorDiv(y, 20) % 2;

                int color = xAxis == 1 ^ yAxis == 1 ? (255 << 16) + (255 << 8) + 255 : 0;

                pixels[i++] = color;
            }
        }

        new ImagePlus("Checker Board", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }

}
