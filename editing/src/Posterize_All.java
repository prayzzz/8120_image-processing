import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 07.05.2015.
 */
public class Posterize_All implements PlugInFilter
{
    public int setup(String arg, ImagePlus imp)
    {
        return DOES_8G;
    }

    public void run(ImageProcessor ip)
    {
        int h = ip.getHeight();
        int w = ip.getWidth();

        int colorRange = 255 / 4;

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int pix = ip.getPixel(x, y);
                int intensity = Math.round(pix / colorRange) * colorRange;

                if (intensity > 255)
                {
                    intensity = 255;
                }

                ip.putPixel(x, y, intensity);
            }
        }
    }
}