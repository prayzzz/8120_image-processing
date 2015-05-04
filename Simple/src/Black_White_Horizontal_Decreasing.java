import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 02.05.2015.
 */
public class Black_White_Horizontal_Decreasing implements PlugIn
{
    public void run(String arg)
    {
        long start = System.currentTimeMillis();
        int w = 400, h = 400;
        ImageProcessor ip = new ColorProcessor(w, h);
        int[] pixels = (int[]) ip.getPixels();

        int targetWidth = 1;
        int currentWidth = 1;
        int color = 0;

        int i = (w * h) - 1;
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                pixels[i--] = color;
            }

            currentWidth--;

            if (currentWidth == 0)
            {
                targetWidth++;
                currentWidth = targetWidth;

                color = color == 0 ? (255 << 16) + (255 << 8) + 255 : 0;
            }
        }

        new ImagePlus("Black White Horizontal Decreasing", ip).show();
        IJ.showStatus("" + (System.currentTimeMillis() - start));
    }
}