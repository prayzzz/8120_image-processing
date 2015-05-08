import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Created by prayzzz on 07.05.2015.
 */
public class Equalize_Intensities implements PlugInFilter
{
    public int setup(String s, ImagePlus imagePlus)
    {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip)
    {
        int h = ip.getHeight();
        int w = ip.getWidth();


        int[] histogram = ip.getHistogram();
        int totalPixelCount = h * w;
        int pixelsPerIntensity = Math.floorDiv(totalPixelCount, 7);

        int currentPixelCount = 0;
        int lastRange = 0;

        for (int i = 0; i < 256; i++)
        {
            currentPixelCount += histogram[i];

            if (currentPixelCount < pixelsPerIntensity)
            {
                continue;
            }

            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    int pix = ip.getPixel(x, y);

                    if (pix >= lastRange && pix < i)
                    {
                        int intensity = ((i - lastRange) / 2) + lastRange;
                        ip.putPixel(x, y, intensity);
                    }
                }
            }

            lastRange = i;
            currentPixelCount = 0;
        }

        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                int pix = ip.getPixel(x, y);

                if (pix >= lastRange)
                {
                    int intensity = ((255 - lastRange) / 2) + lastRange;
                    ip.putPixel(x, y, intensity);
                }
            }
        }
    }
}
