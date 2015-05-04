import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_Yellow_Horizontal implements PlugIn
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
            int c = Math.floorDiv(y, 20) % 2;
            int color = c == 0 ? 0 : (255 << 16) + (255 << 8);

            for (int x = 0; x < w; x++)
            {
                pixels[i++] = color;
            }
        }

        new ImagePlus("Black Yellow Horizontal", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }

}