import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class All_Yellow implements PlugIn
{
    public void run(String arg)
    {
        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();
        int i = 0;

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pixels[i++] = (255 << 16) + (255 << 8);
            }
        }

        new ImagePlus("All Yellow!", ip).show();
    }
}

